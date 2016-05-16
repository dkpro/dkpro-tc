/*******************************************************************************
 * Copyright 2016
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
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.mallet.MalletAdapter;

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
        String frameworkFilename = MalletAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        File outputFile = new File(outputFolder, frameworkFilename);

        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));

        int currentSequenceId = -1;
        Iterator<Instance> iterator = featureStore.getInstances().iterator();
        while (iterator.hasNext()) {
            Instance instance = iterator.next();
            if (currentSequenceId != instance.getSequenceId()) {
                bw.write("\n");
                currentSequenceId = instance.getSequenceId();
            }

            String outcome = instance.getOutcome();
            List<Feature> features = new ArrayList<>(instance.getFeatures());
            for (int i = 0; i < features.size(); i++) {
                Feature feature = features.get(i);
                String featureName = feature.getName();
                Object value = feature.getValue();
                bw.write(featureName + "=" + value.toString());
                bw.write(" ");
            }

            bw.write(outcome);
            bw.write("\n");
        }
        bw.close();
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

}
