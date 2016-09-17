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

package org.dkpro.tc.ml.crfsuite.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.crfsuite.CRFSuiteAdapter;

public class CRFSuiteDataWriter
    implements DataWriter
{

    @Override
    public void write(File aOutputDirectory, FeatureStore aFeatureStore,
            boolean aUseDenseInstances, String aLearningMode, boolean applyWeighting)
        throws Exception
    {
        writeFeatureFile(aFeatureStore, getFeatureFilename(aOutputDirectory));

        Map<String, Integer> outcomeMapping = getOutcomeMapping(aFeatureStore.getUniqueOutcomes());
        File mappingFile = new File(aOutputDirectory, CRFSuiteAdapter.getOutcomeMappingFilename());
        FileUtils.writeStringToFile(mappingFile, outcomeMap2String(outcomeMapping));
    }

    public static File getFeatureFilename(File outputDirectory)
    {
        File outputFile = new File(outputDirectory, CRFSuiteAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile));
        return outputFile;
    }

    public static void writeFeatureFile(FeatureStore featureStore, File outputFile)
        throws Exception
    {
        Iterator<StringBuilder> sequenceIterator = new CRFSuiteFeatureStoreSequenceIterator(featureStore);

        BufferedWriter bf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                outputFile), "utf-8"));

        while(sequenceIterator.hasNext()){
            String features = sequenceIterator.next().toString();
            bf.write(features);
            bf.write("\n");
        }

        bf.close();
    }

    public static String outcomeMap2String(Map<String, Integer> map)
    {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, Integer> entry : map.entrySet()) {
            sb.append(entry.getKey());
            sb.append("\t");
            sb.append(entry.getValue());
            sb.append("\n");
        }

        return sb.toString();
    }

    private Map<String, Integer> getOutcomeMapping(Set<String> outcomes)
    {
        Map<String, Integer> outcomeMapping = new HashMap<String, Integer>();
        int i = 1;
        for (String outcome : outcomes) {
            outcomeMapping.put(outcome, i);
            i++;
        }
        return outcomeMapping;
    }

}
