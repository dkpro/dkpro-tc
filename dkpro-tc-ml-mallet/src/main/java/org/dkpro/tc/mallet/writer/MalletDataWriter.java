/*******************************************************************************
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.mallet.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.features.MissingValue;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.mallet.MalletAdapter;
import org.dkpro.tc.mallet.task.MalletTestTask;

/**
 * {@link DataWriter} for the Mallet machine learning tool.
 */
public class MalletDataWriter
    implements DataWriter, Constants
{

    @Override
    public void write(File outputFolder, FeatureStore featureStore, boolean useDenseInstances,
            String learningMode, boolean applyWeighting)
                throws Exception
    {
        String frameworkFilename = MalletAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        File outputFile = new File(outputFolder, frameworkFilename);

        // check for error conditions
        if (featureStore.getUniqueOutcomes().isEmpty()) {
            throw new IllegalArgumentException("List of instance outcomes is empty.");
        }

        Map<String, Integer> featureOffsetIndex = getFeatureOffsetIndex(featureStore);

//        writeFeatureNamesToFile(featureStore, outputFile);

        List<Instance> instanceArrayList = new ArrayList<Instance>();

        for (int i = 0; i < featureStore.getNumberOfInstances(); i++) {
            instanceArrayList.add(featureStore.getInstance(i));
        }

        // group based on instance sequence and sort based on instance position in file
        Collections.sort(instanceArrayList, new Comparator<Instance>()
        {
            @Override
            public int compare(Instance o1, Instance o2)
            {
                int instanceSequenceId1 = o1.getSequenceId();
                int instanceSequenceId2 = o2.getSequenceId();
                int instancePosition1 = o1.getSequencePosition();
                int instancePosition2 = o2.getSequencePosition();

                if (instanceSequenceId1 == instanceSequenceId2) {
                    if (instancePosition1 == instancePosition2) {
                        return 0;
                    }
                    return instancePosition1 < instancePosition2 ? -1 : 1;
                }

                return 0;
                // order of sequences doesn't matter
                // order of instances within a sequence does
            }
        });

        // List<Instance> normalizedInstanceArrayList = instanceArrayList;
        // ArrayList<Instance> normalizedInstanceArrayList =
        // normalizeNumericFeatureValues(instanceArrayList);

        int currentSequenceId = 1;
        for (int i = 0; i < instanceArrayList.size(); i++) {
            Instance instance = instanceArrayList.get(i);
            if (currentSequenceId != instance.getSequenceId()) {
                writeNewLineToFile(outputFile);
                currentSequenceId = instance.getSequenceId();
            }

            String outcome = instance.getOutcome();
            String featureValues[] = new String[featureOffsetIndex.size()];
            for (Feature feature : instance.getFeatures()) {
                String featureName = feature.getName();
                Object value = feature.getValue();
                double doubleFeatureValue = 0.0;
                String featureValue;
                if (value instanceof Number) {
                    doubleFeatureValue = ((Number) value).doubleValue();
                    featureValue = String.valueOf(doubleFeatureValue);
                }
                else if (value instanceof Boolean) {
                    doubleFeatureValue = (Boolean) value ? 1.0d : 0.0d;
                    featureValue = String.valueOf(doubleFeatureValue);
                }
                else if (value instanceof MissingValue) {
                    // missing value
                    featureValue = MalletFeatureEncoder.getMissingValueConversionMap()
                            .get(((MissingValue) value).getType());
                }
                else if (value == null) {
                    // null
                    throw new IllegalArgumentException(
                            "You have an instance which doesn't specify a value for the feature "
                                    + feature.getName());
                }
                else {
                    featureValue = value.toString();
                }
                if (featureOffsetIndex.containsKey(featureName)) {
                    featureValues[featureOffsetIndex.get(featureName)] = featureValue;
                }
            }
            writeFeatureValuesToFile(featureValues, outcome, outputFile);
        }

        // MalletUtils.instanceListToMalletFormatFile(new File(outputDirectory + "/"
        // +
        // MalletAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.featureVectorsFile)),
        // featureStore, useDenseInstances);
    }

    public HashMap<String, Integer> getFeatureOffsetIndex(FeatureStore instanceList)
    {
        HashMap<String, Integer> featureOffsetIndex = new HashMap<String, Integer>();
        for (int i = 0; i < instanceList.getNumberOfInstances(); i++) {
            Instance instance = instanceList.getInstance(i);
            for (Feature feature : instance.getFeatures()) {
                String featureName = feature.getName();
                if (!featureOffsetIndex.containsKey(featureName)) {
                    featureOffsetIndex.put(featureName, featureOffsetIndex.size());
                }
            }

        }
        return featureOffsetIndex;
    }

    public void writeFeatureNamesToFile(FeatureStore instanceList, File outputFile)
        throws IOException, TextClassificationException
    {
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
        HashMap<String, Integer> featureOffsetIndex = new HashMap<String, Integer>();
        for (int i = 0; i < instanceList.getNumberOfInstances(); i++) {
            Instance instance = instanceList.getInstance(i);
            for (Feature feature : instance.getFeatures()) {
                String featureName = feature.getName();
                if (!featureOffsetIndex.containsKey(featureName)) {
                    featureOffsetIndex.put(featureName, featureOffsetIndex.size());
                    bw.write(featureName + " ");
                }
            }
        }
        bw.write(MalletTestTask.OUTCOME_CLASS_LABEL_NAME);
        bw.close();
    }

    public void writeFeatureValuesToFile(String featureValues[], String outcome, File outputFile)
        throws IOException
    {
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile, true), "UTF-8"));
        bw.write("\n");
        for (String featureValue : featureValues) {
            bw.write(featureValue + " ");
        }
        bw.write(outcome);
        bw.flush();
        bw.close();
    }

    public void writeNewLineToFile(File outputFile)
        throws IOException
    {
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile, true), "UTF-8"));
        bw.write("\n");
        bw.flush();
        bw.close();
    }
}
