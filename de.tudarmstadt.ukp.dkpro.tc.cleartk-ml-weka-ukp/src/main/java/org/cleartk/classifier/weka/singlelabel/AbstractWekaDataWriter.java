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

import java.io.File;
import java.io.IOException;
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
import org.cleartk.classifier.weka.WekaFeaturesEncoder;

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
 *         Torsten Zesch: - more efficient conversion to weka instance by first collecting all
 *         attribute values and later writing in one batch - ability to set the outcome values
 *         directly - ability to choose between dense or sparse vectors
 *
 *         Oliver Ferschke: improved attribute lookup in conversion to weka instance
 *
 */

public abstract class AbstractWekaDataWriter<OUTCOME>
    extends
    DataWriter_ImplBase<AbstractWekaClassifierBuilder<OUTCOME>, Iterable<Feature>, OUTCOME, String>
{

    public static final String classAttributePrefix = "__";
    public static final String classAttributeName = "outcome";

    private final String relationTag;

    List<Iterable<Feature>> instanceFeatures;

    List<String> instanceOutcomes;

    Set<String> outcomeValues;

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

    public AbstractWekaDataWriter(File outputDirectory, String relationTag)
        throws IOException
    {
        super(outputDirectory);
        this.setFeaturesEncoder(new WekaFeaturesEncoder());
        this.relationTag = relationTag;
        instanceFeatures = new ArrayList<Iterable<Feature>>();
        instanceOutcomes = new ArrayList<String>();
        outcomeValues = new HashSet<String>();
        useDenseInstances = false;
        this.trainingDataWriter.close(); // prevent file lock on training data
        this.trainingDataWriter = new PrintWriter(new PipedOutputStream(new PipedInputStream())); // avoid
                                                                                                  // exception
                                                                                                  // on
                                                                                                  // finish
    }

    public AbstractWekaDataWriter(File outputDirectory)
        throws IOException
    {
        this(outputDirectory, "cleartk-generated");
    }

    @Override
    public void writeEncoded(Iterable<Feature> features, String outcome)
    {
        this.instanceFeatures.add(features);
        instanceOutcomes.add(outcome);
        outcomeValues.add(outcome);
    }

    @Override
    public void finish()
        throws CleartkProcessingException
    {
        ArrayList<Attribute> attributes = ((WekaFeaturesEncoder) this.classifierBuilder
                .getFeaturesEncoder()).getWekaAttributes();
        Map<String, Attribute> attributeMap = ((WekaFeaturesEncoder) this.classifierBuilder
                .getFeaturesEncoder()).getWekaAttributeMap();

        // Make sure "outcome" is not the name of an attribute
        Attribute outcomeAttribute = createOutcomeAttribute();
        if (attributeMap.containsKey(classAttributeName)) {
            System.err
                    .println("A feature with name \"outcome\" was found. Renaming outcome attribute");
            outcomeAttribute = outcomeAttribute.copy(classAttributePrefix + classAttributeName);
        }
        attributes.add(outcomeAttribute);

        Map<Attribute, Integer> attIdxLookupMap = new HashMap<Attribute, Integer>(attributes.size());
        for (int idx = 0; idx < attributes.size(); idx++) {
            attIdxLookupMap.put(attributes.get(idx), idx);
        }

        Instances instances = new Instances(relationTag, attributes, instanceFeatures.size());
        instances.setClass(outcomeAttribute);

        ArffSaver saver = new ArffSaver();
        try {
            preprocessingFilter.setInputFormat(instances);
            saver.setRetrieval(Saver.INCREMENTAL);
            saver.setFile(trainingDataFile);
            saver.setInstances(instances);
        }
        catch (Exception e) {
            throw new CleartkProcessingException(e);
        }

        for (int i = 0; i < instanceFeatures.size(); i++) {
            Iterable<Feature> features = instanceFeatures.get(i);

            double[] featureValues = new double[attributes.size()];

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
                else { // this branch is unsafe - the code is copied from SparseInstance (can it be
                       // done safer?)
                    Object stringValue = feature.getValue();
                    if (!attribute.isNominal() && !attribute.isString()) {
                        throw new IllegalArgumentException("Attribute neither nominal nor string!");
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
            instance.setClassValue(instanceOutcomes.get(i));

            try {
                preprocessingFilter.input(instance);
                saver.writeIncremental(preprocessingFilter.output());
            }
            catch (Exception e) {
                throw new CleartkProcessingException(e);
            }
        }

        try {
            // finishes the incremental saving process
            saver.writeIncremental(null);
        }
        catch (IOException e) {
            throw new CleartkProcessingException(e);
        }

        super.finish();
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

    public void setUseDenseInstances(boolean useDenseInstances)
    {
        this.useDenseInstances = useDenseInstances;
    }

    private Attribute createOutcomeAttribute()
    {
        List<String> outcomeValues = new ArrayList<String>(this.outcomeValues);
        Collections.sort(outcomeValues); // make the order of the attributes predictable

        return new Attribute(classAttributeName, outcomeValues);
    }
}