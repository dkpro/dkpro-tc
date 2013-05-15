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
package org.cleartk.classifier.weka.multilabel;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.outcome.StringArrayToStringArrayEncoder;
import org.cleartk.classifier.jar.DataWriter_ImplBase;
import org.cleartk.classifier.weka.ReplaceMissingValuesWithZeroFilter;
import org.cleartk.classifier.weka.WekaFeaturesEncoder;
import org.cleartk.classifier.weka.singlelabel.AbstractWekaDataWriter;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;
import weka.filters.Filter;

/**
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
 *         Torsten Zesch: more efficient conversion to weka instance by first collecting all
 *         attribute values and later writing in one batch
 * 
 *         Oliver Ferschke: improved attribute lookup in conversion to weka instance
 * 
 *         Johannes Daxenberger: enabled Meka use for multi-labeled data
 * 
 */

public class DefaultSerializedMekaDataWriter
    extends
    DataWriter_ImplBase<DefaultMekaClassifierBuilder, Iterable<Feature>, String[], String[]>
{

    public static final String relationTag = "cleartk-generated";

    List<String[]> instanceOutcomes;

    Set<String> outcomeValues;

    boolean useDenseInstances;

    ObjectOutputStream tempOut;
    File tempFeaturesFile;

    int nrInstances;

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

    public DefaultSerializedMekaDataWriter(File outputDirectory)
        throws IOException
    {

        super(outputDirectory);
        this.nrInstances = 0;
        this.setFeaturesEncoder(new WekaFeaturesEncoder());
        this.setOutcomeEncoder(new StringArrayToStringArrayEncoder());
        instanceOutcomes = new ArrayList<String[]>();
        useDenseInstances = false;
        outcomeValues = new HashSet<String>();
        this.trainingDataWriter.close(); // prevent file lock on training data
        this.trainingDataWriter = new PrintWriter(new PipedOutputStream(new PipedInputStream())); // avoid
                                                                                                  // exception
                                                                                                  // on
                                                                                                  // finish
        this.tempFeaturesFile = new File(outputDirectory, "features.tmp");
        this.tempOut = new ObjectOutputStream(new FileOutputStream(tempFeaturesFile));
    }

    @Override
    public void writeEncoded(Iterable<Feature> features, String[] outcome)
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
        // sets the class labels
        instanceOutcomes.add(outcome);
        // adds all class label values to the label set
        outcomeValues.addAll(Arrays.asList(outcome));
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

        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        List<Attribute> outcomeAttributes = createOutcomeAttributes(new ArrayList<String>(
                this.outcomeValues));
        // in Meka, class label attributes have to go on top
        attributes.addAll(outcomeAttributes);

        attributes.addAll(((WekaFeaturesEncoder) this.classifierBuilder.getFeaturesEncoder())
                .getWekaAttributes());
        Map<String, Attribute> attributeMap = ((WekaFeaturesEncoder) this.classifierBuilder
                .getFeaturesEncoder()).getWekaAttributeMap();

        Map<Attribute, Integer> attIdxLookupMap = new HashMap<Attribute, Integer>(attributes.size());
        for (int idx = 0; idx < attributes.size(); idx++) {
            attIdxLookupMap.put(attributes.get(idx), idx);
        }

        // for Meka-internal use
        Instances instances = new Instances(relationTag + ": -C " + outcomeAttributes.size() + " ",
                attributes, nrInstances);
        instances.setClassIndex(outcomeAttributes.size());

        ObjectInputStream inStream = null;

        ArffSaver saver = new ArffSaver();
        try {
            preprocessingFilter.setInputFormat(instances);
            saver.setRetrieval(Saver.INCREMENTAL);
            saver.setFile(trainingDataFile);
            // saver.setInstances(instances);
            saver.setStructure(instances);

            // read serialized feature values
            FileInputStream fs = new FileInputStream(tempFeaturesFile);
            inStream = new ObjectInputStream(fs);
            Iterable<Feature> features;
            int curInstance = 0;

            while ((features = (Iterable<Feature>) inStream.readObject()) != null) {

                double[] featureValues = new double[attributes.size()];

                // set class label values
                List<String> instanceOutcome = Arrays.asList(instanceOutcomes.get(curInstance++));
                for (Attribute label : outcomeAttributes) {
                    featureValues[attIdxLookupMap.get(label)] = instanceOutcome.contains(label
                            .name().substring(2)) ? 1.0d : 0.0d;
                }

                // set feature values
                for (Feature feature : features) {
                    Attribute attribute = attributeMap.get(feature.getName());
                    Object featureValue = feature.getValue();

                    double attributeValue;
                    if (featureValue instanceof Number) {
                        attributeValue = ((Number) feature.getValue()).doubleValue();
                    }
                    else if (featureValue instanceof Boolean) {
                        attributeValue = (Boolean) featureValue ? 1.0d : 0.0d;
                    }
                    else { // this branch is unsafe - the code is copied from SparseInstance (can it
                           // be
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

                instance.setDataset(instances);

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

    @Override
    protected DefaultMekaClassifierBuilder newClassifierBuilder()
    {
        return new DefaultMekaClassifierBuilder();
    }

    /**
     * Creates a list of nominal class label attributes with the values {0,1}.
     * 
     * @see {@link http://meka.sourceforge.net/}
     * @return a list of class label attributes
     */
    public static List<Attribute> createOutcomeAttributes(List<String> outcomeValues)
    {
        List<Attribute> outcomeAttributes = new ArrayList<Attribute>();
        for (String label : outcomeValues) {
            outcomeAttributes.add(new Attribute(
                    AbstractWekaDataWriter.classAttributePrefix + label, Arrays
                            .asList(new String[] { "0", "1" })));
        }
        return outcomeAttributes;
    }

    @Override
    public void write(org.cleartk.classifier.Instance<String[]> instance)
        throws CleartkProcessingException
    {
        super.write(instance);
    }

    public void setUseDenseInstances(boolean useDenseInstances)
    {
        this.useDenseInstances = useDenseInstances;
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
        this.outcomeValues = outcomeValues;
    }
}