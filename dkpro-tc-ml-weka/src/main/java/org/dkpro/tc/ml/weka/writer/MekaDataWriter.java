/**
 * Copyright 2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.tc.ml.weka.writer;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.ml.weka.util.AttributeStore;

import com.google.gson.Gson;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;

/**
 * Datawriter for the Weka machine learning tool.
 */
public class MekaDataWriter
    implements DataWriter, Constants
{
    public static final String RELATION_NAME = "dkpro-tc-generated";
    
    protected Logger logger = Logger.getLogger(MekaDataWriter.class);

    BufferedWriter bw = null;
    Gson gson = new Gson();
    private boolean useSparse;
    private boolean applyWeighting;
    private File outputFolder;
    private File arffTarget;

    AttributeStore attributeStore;
    List<Attribute> outcomeAttributes;
    ArffSaver saver;
    Instances masterInstance;
    private String[] outcomes;

    @Override
    public void init(File outputFolder, boolean useSparse, String learningMode, String featureMode,
            boolean applyWeighting, String[] outcomes)
        throws Exception
    {
        this.outputFolder = outputFolder;
        this.useSparse = useSparse;
        this.applyWeighting = applyWeighting;
        this.outcomes = Arrays.copyOf(outcomes, outcomes.length);

        arffTarget = new File(outputFolder, Constants.FILENAME_DATA_IN_CLASSIFIER_FORMAT);

        // Caution: DKPro Lab imports (aka copies!) the data of the train task
        // as test task. We use
        // appending mode for streaming. We might errornously append the old
        // training file with
        // testing data!
        // Force delete the old training file to make sure we start with a
        // clean, empty file
        if (arffTarget.exists()) {
            FileUtils.forceDelete(arffTarget);
        }

        File genericOutputFile = new File(outputFolder, getGenericFileName());
        if (genericOutputFile.exists()) {
            FileUtils.forceDelete(genericOutputFile);
        }
    }

    @Override
    public void writeGenericFormat(List<Instance> instances) throws AnalysisEngineProcessException
    {
        try {
            initGeneric();

            bw.write(gson.toJson(instances.toArray(new Instance[0])) + "\n");

            bw.close();
            bw = null;
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void initGeneric() throws IOException
    {
        if (bw != null) {
            return;
        }
        bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(outputFolder, GENERIC_FEATURE_FILE), true), UTF_8));

    }

    @Override
    public void transformFromGeneric() throws Exception
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(new File(outputFolder, GENERIC_FEATURE_FILE)), UTF_8))) {

            String line = null;
            while ((line = reader.readLine()) != null) {
                Instance[] restoredInstance = gson.fromJson(line, Instance[].class);
                writeClassifierFormat(Arrays.asList(restoredInstance));
            }
        }

        FileUtils.deleteQuietly(new File(outputFolder, GENERIC_FEATURE_FILE));
    }

    private static List<Attribute> createOutcomeAttributes(List<String> outcomeValues)
    {
        // make the order of the attributes predictable
        Collections.sort(outcomeValues);
        List<Attribute> atts = new ArrayList<Attribute>();

        for (String outcome : outcomeValues) {
            String name = outcome.contains(CLASS_ATTRIBUTE_PREFIX) ? outcome
                    : CLASS_ATTRIBUTE_PREFIX + outcome;
            atts.add(new Attribute(name, Arrays.asList(new String[] { "0", "1" })));
        }
        return atts;
    }

    private double[] getFeatureValues(AttributeStore attributeStore, Instance instance)
    {
        double[] featureValues = new double[attributeStore.getAttributes().size()];

        for (Feature feature : instance.getFeatures()) {

            try {
                Attribute attribute = attributeStore.getAttribute(feature.getName());
                Object featureValue = feature.getValue();

                double attributeValue;
                if (featureValue instanceof Number) {
                    // numeric attribute
                    attributeValue = ((Number) feature.getValue()).doubleValue();
                }
                else if (featureValue instanceof Boolean) {
                    // boolean attribute
                    attributeValue = (Boolean) featureValue ? 1.0d : 0.0d;
                }
                else if (featureValue == null) {
                    // null
                    throw new IllegalArgumentException(
                            "You have an instance which doesn't specify a value for the feature "
                                    + feature.getName());
                }
                else {
                    // nominal or string
                    Object stringValue = feature.getValue();
                    if (!attribute.isNominal() && !attribute.isString()) {
                        throw new IllegalArgumentException(
                                "Attribute neither nominal nor string: " + stringValue);
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
                int offset = attributeStore.getAttributeOffset(attribute.name());

                if (offset != -1) {
                    featureValues[offset] = attributeValue;
                }
            }
            catch (NullPointerException e) {
                logger.debug("Feature name [" + feature.getName() + "] unknown - ignored");
            }
        }
        return featureValues;
    }

    @Override
    public void writeClassifierFormat(List<Instance> instances)
        throws AnalysisEngineProcessException
    {

        try {
            Instances masterInstance = initalConfiguration(instances);

            for (Instance instance : instances) {
                double[] featureValues = getFeatureValues(attributeStore, instance);

                // set class label values
                List<String> instanceOutcome = instance.getOutcomes();
                for (Attribute label : outcomeAttributes) {
                    String labelname = label.name();
                    featureValues[attributeStore.getAttributeOffset(labelname)] = instanceOutcome
                            .contains(labelname.split(CLASS_ATTRIBUTE_PREFIX)[1]) ? 1.0d : 0.0d;
                }

                weka.core.Instance wekaInstance;

                if (useSparse) {
                    wekaInstance = new SparseInstance(1.0, featureValues);
                }
                else {
                    wekaInstance = new DenseInstance(1.0, featureValues);
                }

                wekaInstance.setDataset(masterInstance);

                Double instanceWeight = instance.getWeight();
                if (applyWeighting) {
                    wekaInstance.setWeight(instanceWeight);
                }

                saver.writeIncremental(wekaInstance);
            }
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    private Instances initalConfiguration(Collection<Instance> instances)
        throws TextClassificationException, IOException
    {
        if (saver != null) {
            return masterInstance;
        }
        saver = new ArffSaver();
        saver.setRetrieval(Saver.INCREMENTAL);
        saver.setFile(arffTarget);
        saver.setCompressOutput(false);

        attributeStore = new AttributeStore();

        List<String> lines = FileUtils
                .readLines(new File(outputFolder, Constants.FILENAME_FEATURES_DESCRIPTION), UTF_8);

        for (String l : lines) {
            String[] split = l.split("\t");
            String featureName = split[0];

            if (!attributeStore.containsAttributeName(featureName)) {
                FeatureType type = FeatureType.valueOf(split[1]);
                String enumType = null;
                if (type == FeatureType.NOMINAL) {
                    enumType = split[2];
                }
                Attribute attribute = WekaFeatureEncoder
                        .featureToAttributeUsingFeatureDescription(featureName, type, enumType);
                attributeStore.addAttribute(featureName, attribute);
            }

        }

        // Make sure "outcome" is not the name of an attribute
        List<String> outcomeList = Arrays.asList(outcomes);
        outcomeAttributes = createOutcomeAttributes(outcomeList);
        // in Meka, class label attributes have to go on top
        for (Attribute attribute : outcomeAttributes) {
            attributeStore.addAttributeAtBegin(attribute.name(), attribute);
        }

        // for Meka-internal use
        masterInstance = new Instances(RELATION_NAME + ": -C " + outcomeAttributes.size() + " ",
                attributeStore.getAttributes(), instances.size());
        masterInstance.setClassIndex(outcomeAttributes.size());
        saver.setInstances(masterInstance);

        return masterInstance;

    }

    @Override
    public boolean canStream()
    {
        return true;
    }

    @Override
    public String getGenericFileName()
    {
        return GENERIC_FEATURE_FILE;
    }

    @Override
    public void close() throws Exception
    {
        saver.writeIncremental(null);
    }
}
