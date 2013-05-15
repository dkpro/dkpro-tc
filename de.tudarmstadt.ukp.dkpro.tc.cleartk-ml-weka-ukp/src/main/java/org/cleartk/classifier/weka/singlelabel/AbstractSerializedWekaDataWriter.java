/**
 * Copyright (c) 2012, Regents of the University of Colorado
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For a complete copy of the license please see the file LICENSE distributed
 * with the cleartk-syntax-berkeley project or visit
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package org.cleartk.classifier.weka.singlelabel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.jar.DataWriter_ImplBase;
import org.cleartk.classifier.weka.ReplaceMissingValuesWithZeroFilter;
import org.cleartk.classifier.weka.WekaSerializedFeaturesEncoder;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;
import weka.filters.Filter;

/**
 *
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 *
 * @author Philip Ogren
 *
 *         http://weka.wikispaces.com/Creating+an+ARFF+file
 *
 *         Michael Kutschke: generalized to any OUTCOME, adapted to use ArffSaver, which is more
 *         appropriate for large data sets than Instances.toString()
 *
 *         Torsten Zesch: - more efficient conversion to weka instance by first collecting all
 *         attribute values and later writing in one batch - ability to set the outcome values
 *         directly - ability to choose between dense or sparse vectors
 *
 *         Oliver Ferschke: improved attribute lookup in conversion to weka instance
 *
 *         Oliver Ferschke, Johannes Daxenberger: fixing the memory problem
 *
 */

public abstract class AbstractSerializedWekaDataWriter<OUTCOME>
    extends
    DataWriter_ImplBase<AbstractWekaClassifierBuilder<OUTCOME>, Iterable<Feature>, OUTCOME, String>
{

    public static final String classAttributePrefix = "__";
    public static final String classAttributeName = "outcome";

    private final String relationTag;

    ObjectOutputStream tempOut;
    File tempFeaturesFile;

    int nrInstances;

    // TODO OF: do we need duplicate collections of outcome values? outcomeSet
    // doesn't have to be global
    List<String> instanceOutcomes;
    Set<String> outcomeSet;

    /**
     * This instances object will only contain header information for initializing the ArffSaver. It
     * won't contain the actual data.
     */
    Instances arffHeader;

    boolean useDenseInstances;

    /**
     * TODO The AbstractWekaClassifier should use the same filter as the AbstractWekaDataWriter.
     * Currently, the filters in both classes are set to the ReplaceMissingValuesWithZeroFilter,
     * because the filter in the AbstractWekaClassifier is not yet configurable and some settings
     * need this replacement. As soon as filter configuration is possible in the classifier, the
     * standard filter can be changed to the "AllFilter" (which does nothing). Then the filter will
     * only be effective when it has been set explicitly.
     */
    private Filter preprocessingFilter = new ReplaceMissingValuesWithZeroFilter();

    public void setPreprocessingFilter(Filter preprocessingFilter)
    {
        this.preprocessingFilter = preprocessingFilter;
    }

    public AbstractSerializedWekaDataWriter(File outputDirectory, String relationTag)
        throws IOException
    {
        super(outputDirectory);
        this.nrInstances = 0;
        this.outputDirectory = outputDirectory;
        this.setFeaturesEncoder(new WekaSerializedFeaturesEncoder());
        this.relationTag = relationTag;
        instanceOutcomes = new ArrayList<String>();
        outcomeSet = new HashSet<String>();
        useDenseInstances = false;
        this.trainingDataWriter.close(); // prevent file lock on training data
        this.trainingDataWriter = new PrintWriter(new PipedOutputStream(new PipedInputStream()));

        this.tempFeaturesFile = new File(outputDirectory, "features.tmp");
        this.tempOut = new ObjectOutputStream(new FileOutputStream(tempFeaturesFile));
    }

    public AbstractSerializedWekaDataWriter(File outputDirectory)
        throws IOException
    {
        this(outputDirectory, "cleartk-generated");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.cleartk.classifier.jar.DataWriter_ImplBase#writeEncoded(java.lang .Object,
     * java.lang.Object)
     */
    @Override
    public void writeEncoded(Iterable<Feature> features, String outcome)
        throws CleartkProcessingException
    {
        // This is called instance-wise
        // Does nothing than collecting all the features and outcomes
        // note that write() also called the featuresencode which create lists of attributes

        this.nrInstances++;

        // extract header info for arff saver
        try {
            this.tempOut.writeObject(features);
        }
        catch (IOException e) {
            System.err.println("Could not serialize features to temporary file.");
            throw new CleartkProcessingException(e);
        }

        instanceOutcomes.add(outcome);
        outcomeSet.add(outcome);
    }

    @Override
    public void finish()
        throws CleartkProcessingException
    {

        // close stream - we're done serializing
        try {
            this.tempOut.writeObject(null);
            this.tempOut.flush();
            this.tempOut.close();
        }
        catch (IOException e) {
            System.err.println("Could not close serialization stream.");
            throw new CleartkProcessingException(e);
        }

        // TODO get attributes from the serialized features
        ArrayList<Attribute> attributes = ((WekaSerializedFeaturesEncoder) this.classifierBuilder
                .getFeaturesEncoder()).getWekaAttributes();

        // TODO get attributeMap from the serialized features
        Map<String, Attribute> attributeMap = ((WekaSerializedFeaturesEncoder) this.classifierBuilder
                .getFeaturesEncoder()).getWekaAttributeMap();

        // Make sure "outcome" is not the name of an attribute
        Attribute outcomeAttribute = createOutcomeAttribute();
        if (attributeMap.containsKey(classAttributeName)) {
            System.err
                    .println("A feature with name \"outcome\" was found. Renaming outcome attribute");
            outcomeAttribute = outcomeAttribute.copy(classAttributePrefix + classAttributeName);
        }
        attributes.add(outcomeAttribute);

        // TODO this lookup should locate the serialized attribute values and no
        // longer the position in the attributeMap
        Map<Attribute, Integer> attIdxLookupMap = new HashMap<Attribute, Integer>(attributes.size());
        for (int idx = 0; idx < attributes.size(); idx++) {
            attIdxLookupMap.put(attributes.get(idx), idx);
        }

        // empty instances object with header information
        arffHeader = new Instances(relationTag, attributes, nrInstances);
        arffHeader.setClass(outcomeAttribute);

        // initialize arffsaver with header information
        ObjectInputStream inStream = null;
        try {
            ArffSaver saver = new ArffSaver();
            preprocessingFilter.setInputFormat(arffHeader);
            saver.setRetrieval(Saver.INCREMENTAL);
            saver.setFile(trainingDataFile);
            // saver.setInstances(instances); // removed
            saver.setStructure(arffHeader);

            // read serialized feature values
            FileInputStream fs = new FileInputStream(tempFeaturesFile);
//          inStream = new ObjectInputStream(fs);
            inStream = new ObjectInputStream(new BufferedInputStream(fs));
            Iterable<Feature> features;
            int curInstance = 0;

            while ((features = (Iterable<Feature>) inStream.readObject()) != null) {

                // array for holding feature values for current instance
                double[] featureValues = new double[attributes.size()];

                for (Feature feature : features) {

                    // TODO convert feature to attribute here instead of using the attributes list

                    Attribute attribute = attributeMap.get(feature.getName());
                    Object featureValue = feature.getValue();

                    double attributeValue;
                    if (featureValue instanceof Number) {
                        attributeValue = ((Number) feature.getValue()).doubleValue();
                    }
                    else if (featureValue instanceof Boolean) {
                        attributeValue = (Boolean) featureValue ? 1.0d : 0.0d;
                    }
                    else { // this branch is unsafe - the code is copied from
                           // SparseInstance (can it be
                           // done safer?)
                        Object stringValue = feature.getValue();
                        if (!attribute.isNominal() && !attribute.isString()) {
                            throw new IllegalArgumentException(
                                    "Attribute neither nominal nor string!");
                        }
                        int valIndex = attribute.indexOfValue(stringValue.toString());
                        if (valIndex == -1) {
                            if (attribute.isNominal()) {
                                throw new IllegalArgumentException(
                                        "Value not defined for given nominal attribute!");
                            }
                            else {
                                attribute.addStringValue(stringValue.toString());
                                valIndex = attribute.indexOfValue(stringValue.toString());
                            }
                        }
                        attributeValue = valIndex;
                    }
                    featureValues[attIdxLookupMap.get(attribute)] = attributeValue;
                }

                Instance instance;

                if (useDenseInstances) {
                    instance = new DenseInstance(1.0, featureValues);
                }
                else {
                    instance = new SparseInstance(1.0, featureValues);
                }

                instance.setDataset(arffHeader);
                instance.setClassValue(instanceOutcomes.get(curInstance++));              
                // TODO for regression, need to write double here instead of string
//                instance.setClassValue(Double.parseDouble(instanceOutcomes.get(curInstance++)));

                preprocessingFilter.input(instance);
                saver.writeIncremental(preprocessingFilter.output());
            }

            // finishes the incremental saving process
            saver.writeIncremental(null);
        }
        catch (Exception e) {
            throw new CleartkProcessingException(e);
        }
        finally {
            // close readers etc.
            try {
                inStream.close();
            }
            catch (IOException e) {
                throw new CleartkProcessingException(e);
            }
            super.finish();
        }

    }

    /**
     * Explicitly sets the outcome values to be used when writing the instances. Caution: Under
     * normal circumstances, the correct set of values to be used is directly inferred from the
     * instances. However, in special situations e.g. special train/test splits it might happen that
     * the outcome set is not the same. In those cases, this method can be used to overwrite the
     * inferred values.
     */
    public void setOutcomeValues(Set<String> outcomeValues)
    {
        this.outcomeSet = outcomeValues;
    }

    public void setUseDenseInstances(boolean useDenseInstances)
    {
        this.useDenseInstances = useDenseInstances;
    }

    private Attribute createOutcomeAttribute()
    {
        List<String> outcomeValues = new ArrayList<String>(this.outcomeSet);
        Collections.sort(outcomeValues); // make the order of the attributes
                                         // predictable
        
        // TODO for regression, need to write without outcome values here
//        return new Attribute(classAttributeName);
        return new Attribute(classAttributeName, outcomeValues);
    }
}