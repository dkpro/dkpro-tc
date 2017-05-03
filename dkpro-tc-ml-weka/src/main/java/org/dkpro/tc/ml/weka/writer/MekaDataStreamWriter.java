/**
 * Copyright 2017
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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.features.MissingValue;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.DataStreamWriter;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.weka.MekaClassificationAdapter;
import org.dkpro.tc.ml.weka.util.AttributeStore;
import org.dkpro.tc.ml.weka.util.WekaUtils;

import com.google.gson.Gson;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;

/**
 * {@link DataWriter} for the Weka machine learning tool.
 */
public class MekaDataStreamWriter
    implements DataStreamWriter, Constants
{
    BufferedWriter bw = null;
    Gson gson;
    private boolean useSparse;
    private String learningMode;
    private boolean applyWeighting;
    private File outputFolder;

    @Override
    public void init(File outputFolder, boolean useSparse, String learningMode,
            boolean applyWeighting)
                throws Exception
    {
        this.outputFolder = outputFolder;
        this.useSparse = useSparse;
        this.learningMode = learningMode;
        this.applyWeighting = applyWeighting;
    }

    @Override
    public void writeGenericFormat(Collection<Instance> instances)
        throws Exception
    {
        initGeneric();

        Iterator<Instance> iterator = instances.iterator();
        while (iterator.hasNext()) {
            Instance next = iterator.next();
            bw.write(gson.toJson(next) + System.lineSeparator());
        }
    }

    private void initGeneric()
        throws IOException
    {
        if (bw != null) {
            return;
        }
        bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(outputFolder, GENERIC_FEATURE_FILE)), "utf-8"));

        gson = new Gson();
    }

    @Override
    public void transformFromGeneric()
        throws Exception
    {
        close();

        File arffTarget = new File(outputFolder, MekaClassificationAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile));
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(new File(outputFolder, GENERIC_FEATURE_FILE)), "utf-8"));

        AttributeStore attributeStore = new AttributeStore();
        Gson gson = new Gson();

        String line = null;
        int numInstances = 0;
        while ((line = reader.readLine()) != null) {
            Instance restoredInstance = gson.fromJson(line, Instance.class);
            for (Feature feature : restoredInstance.getFeatures()) {
                if (!attributeStore.containsAttributeName(feature.getName())) {
                    Attribute attribute = WekaFeatureEncoder.featureToAttribute(feature);
                    attributeStore.addAttribute(feature.getName(), attribute);
                }
            }
            numInstances++;
        }
        reader.close();
        
        // Make sure "outcome" is not the name of an attribute
        List<String> outcomeList = FileUtils
                .readLines(new File(outputFolder, Constants.FILENAME_OUTCOMES), "utf-8");

        List<Attribute> outcomeAttributes = createOutcomeAttributes(outcomeList);

        // in Meka, class label attributes have to go on top
        for (Attribute attribute : outcomeAttributes) {
            attributeStore.addAttributeAtBegin(attribute.name(), attribute);
        }
        
        // for Meka-internal use
        Instances wekaInstances = new Instances(
                WekaUtils.RELATION_NAME + ": -C " + outcomeAttributes.size() + " ",
                attributeStore.getAttributes(), numInstances);
        wekaInstances.setClassIndex(outcomeAttributes.size());

        writeArff(arffTarget, attributeStore, wekaInstances, outcomeAttributes);

        FileUtils.deleteQuietly(new File(outputFolder, GENERIC_FEATURE_FILE));
    }

    private void writeArff(File arffTarget, AttributeStore attributeStore, Instances wekaInstances,
            List<Attribute> outcomeAttributes)
                throws Exception
    {
        if (!arffTarget.exists()) {
            arffTarget.mkdirs();
            arffTarget.createNewFile();
        }

        ArffSaver saver = new ArffSaver();
        // preprocessingFilter.setInputFormat(wekaInstances);
        saver.setRetrieval(Saver.INCREMENTAL);
        saver.setFile(arffTarget);
        saver.setCompressOutput(classiferReadsCompressed());
        saver.setInstances(wekaInstances);

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(new File(outputFolder, GENERIC_FEATURE_FILE)), "utf-8"));
        String line = null;
        while ((line = reader.readLine()) != null) {
            Instance instance = gson.fromJson(line, Instance.class);

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

            wekaInstance.setDataset(wekaInstances);

            Double instanceWeight = instance.getWeight();
            if (applyWeighting) {
                wekaInstance.setWeight(instanceWeight);
            }

            saver.writeIncremental(wekaInstance);
        }

        saver.writeIncremental(null);
        reader.close();
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
                else if (featureValue instanceof MissingValue) {
                    // missing value
                    attributeValue = WekaFeatureEncoder.getMissingValueConversionMap()
                            .get(((MissingValue) featureValue).getType());
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
                // ignore unseen attributes
            }
        }
        return featureValues;
    }

    @Override
    public void close()
        throws IOException
    {
        if (bw != null) {
            bw.close();
            bw = null;
        }
    }

    @Override
    public void writeClassifierFormat(Collection<Instance> instances, boolean compress)
        throws Exception
    {
        throw new UnsupportedOperationException(
                "Weka/Meka cannot write directly into classifier format. "
                        + "The feature file has a header which requires knowing all feature names and outcomes"
                        + " before the feature file can be written.");
    }

    @Override
    public boolean canStream()
    {
        return false;
    }

    @Override
    public boolean classiferReadsCompressed()
    {
        return true;
    }

    @Override
    public String getGenericFileName()
    {
        return GENERIC_FEATURE_FILE;
    }
}
