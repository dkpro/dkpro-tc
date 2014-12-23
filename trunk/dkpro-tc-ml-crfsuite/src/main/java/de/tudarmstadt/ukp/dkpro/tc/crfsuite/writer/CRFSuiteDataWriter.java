/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.crfsuite.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteAdapter;

public class CRFSuiteDataWriter
    implements DataWriter
{
    private static Log logger = null;

    @Override
    public void write(File aOutputDirectory, FeatureStore aFeatureStore,
            boolean aUseDenseInstances, String aLearningMode)
        throws Exception
    {
        writeFeatureFile(aFeatureStore, aOutputDirectory);

        Map<String, Integer> outcomeMapping = getOutcomeMapping(aFeatureStore.getUniqueOutcomes());
        File mappingFile = new File(aOutputDirectory, CRFSuiteAdapter.getOutcomeMappingFilename());
        FileUtils.writeStringToFile(mappingFile, outcomeMap2String(outcomeMapping));
    }

    public static File writeFeatureFile(FeatureStore featureStore, File aOutputDirectory) throws Exception
    {
        int totalCountOfInstances = featureStore.getNumberOfInstances();

        File outputFile = new File(aOutputDirectory, CRFSuiteAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile));
        BufferedWriter bf = new BufferedWriter(new FileWriter(outputFile));
        log("Start writing features to file " + outputFile.getAbsolutePath());

        int lastSeenSeqId = -1;
        boolean seqIdChanged = false;
        for (int ins = 0; ins < totalCountOfInstances; ins++) {
            Instance i = featureStore.getInstance(ins);

            if (i.getSequenceId() != lastSeenSeqId) {
                seqIdChanged = true;
                lastSeenSeqId = i.getSequenceId();
            }

            bf.write(i.getOutcome());
            bf.write("\t");

            List<Feature> features = i.getFeatures();
            for (int idx = 0; idx < features.size(); idx++) {
                Feature f = features.get(idx);
                bf.write(f.getName() + "=" + f.getValue());
                if (idx + 1 < features.size()) {
                    bf.write("\t");
                }
            }

            // Mark first line of new sequence with an additional __BOS__
            if (seqIdChanged) {
                bf.write("\t");
                bf.write("__BOS__");
                seqIdChanged = false;
            }

            // Peak ahead - seqEnd reached?
            if (ins + 1 < totalCountOfInstances) {
                Instance next = featureStore.getInstance(ins + 1);
                if (next.getSequenceId() != lastSeenSeqId) {
                    appendEOS(bf);
                    continue;
                }
            }
            else if (ins + 1 == totalCountOfInstances) {
                appendEOS(bf);
            }

            bf.write("\n");
        }
        bf.close();
        log("Finished writing features to file " + outputFile.getAbsolutePath());
        
        return outputFile;
    }

    private static void appendEOS(BufferedWriter bf) throws Exception
    {
        bf.write("\t");
        bf.write("__EOS__");
        bf.write("\n");
        bf.write("\n");
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

    private static void log(String text)
    {
        if (logger == null) {
            logger = LogFactory.getLog(CRFSuiteDataWriter.class.getName());
        }
        logger.info(text);
    }

}
