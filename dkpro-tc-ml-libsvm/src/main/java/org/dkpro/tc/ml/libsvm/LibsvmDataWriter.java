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
package org.dkpro.tc.ml.libsvm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;

/**
 * Format is outcome TAB index:value TAB index:value TAB ...
 * 
 * Zeros are omitted. Indexes need to be sorted.
 * 
 * For example: 1 1:1 3:1 4:1 6:1 2 2:1 3:1 5:1 7:1 1 3:1 5:1
 */
public class LibsvmDataWriter
    implements DataWriter
{
    private Map<String, Integer> featName2id = new HashMap<>();

    @Override
    public void write(File outputDirectory, FeatureStore featureStore, boolean useDenseInstances,
            String learningMode, boolean applyWeighting)
                throws Exception
    {

        writeOutcomes(featureStore, outputDirectory);
        createAndPersistFeatureNameMap(featureStore, outputDirectory);

        String fileName = LibsvmAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(outputDirectory, fileName)), "utf-8"));

        for (Instance i : featureStore.getInstances()) {
            String outcome = i.getOutcome();
            bw.write(outcome);
            for (Feature f : i.getFeatures()) {
                if(!sanityCheckValue(f)){
                    continue;
                }
                bw.write("\t");
                bw.write(featName2id.get(f.getName()) + ":" + f.getValue());
            }
            bw.write("\n");
        }
        bw.close();
    }

    private boolean sanityCheckValue(Feature f)
    {
        if (f.getValue() instanceof Number) {
            return true;
        }
        if(f.getName().equals(Constants.ID_FEATURE_NAME)){
            return false;
        }
        
        try {
            Double.valueOf((String) f.getValue());
        }
        catch (Exception e) {
            throw new IllegalArgumentException(
                    "Feature [" + f.getName() + "] has a non-numeric value [" + f.getValue() + "]",
                    e);
        }
        return false;
    }

    private void createAndPersistFeatureNameMap(FeatureStore featureStore, File outputDirectory)
        throws IOException
    {
        int i = 0;
        for (String n : featureStore.getFeatureNames()) {
            featName2id.put(n, i++);
        }

        String s = map2String(featName2id);
        File outcomeMap = new File(outputDirectory, LibsvmAdapter.getFeaturenameMappingFilename());
        FileUtils.write(outcomeMap, s, "utf-8");
    }

    private void writeOutcomes(FeatureStore featureStore, File outputDirectory)
        throws IOException
    {
        StringBuilder sb = new StringBuilder();
        for (String o : featureStore.getUniqueOutcomes()) {
            sb.append(o + "\n");
        }

        File outcomeMap = new File(outputDirectory, LibsvmAdapter.getOutcomes());
        FileUtils.write(outcomeMap, sb.toString(), "utf-8");
    }

    private String map2String(Map<String, Integer> map)
    {
        StringBuilder sb = new StringBuilder();
        for (String k : map.keySet()) {
            sb.append(k + "\t" + map.get(k) + "\n");
        }

        return sb.toString();
    }
}