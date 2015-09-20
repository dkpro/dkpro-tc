/**
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.tc.crfsuite.writer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteAdapter;

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

    public static File getFeatureFilename(File outputDirectory) {
        File outputFile = new File(outputDirectory, CRFSuiteAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile));
        outputFile.deleteOnExit();
        return outputFile;
    }
    
    public static void writeFeatureFile(FeatureStore featureStore, File outputFile)
        throws Exception
    {
        int totalCountOfInstances = featureStore.getNumberOfInstances();

        StringBuilder sb = new StringBuilder();
        
        int lastSeenSeqId = -1;
        boolean seqIdChanged = false;
        for (int ins = 0; ins < totalCountOfInstances; ins++) {
            Instance i = featureStore.getInstance(ins);

            if (i.getSequenceId() != lastSeenSeqId) {
                seqIdChanged = true;
                lastSeenSeqId = i.getSequenceId();
            }

            sb.append(LabelSubstitutor.labelReplacement(i.getOutcome()));
            sb.append("\t");

            int idx = 0;
            for (Feature f :  i.getFeatures()) {
                sb.append(f.getName() + "=" + f.getValue());
                if (idx + 1 < i.getFeatures().size()) {
                    sb.append("\t");
                }
                idx++;
            }

            // Mark first line of new sequence with an additional __BOS__
            if (seqIdChanged) {
                sb.append("\t");
                sb.append("__BOS__");
                seqIdChanged = false;
            }

            // Peak ahead - seqEnd reached?
            if (ins + 1 < totalCountOfInstances) {
                Instance next = featureStore.getInstance(ins + 1);
                if (next.getSequenceId() != lastSeenSeqId) {
                    appendEOS(sb);
                    continue;
                }
            }
            else if (ins + 1 == totalCountOfInstances) {
                appendEOS(sb);
            }

            sb.append("\n");
        }

        FileUtils.write(outputFile, sb.toString());
    }

    private static void appendEOS(StringBuilder sb)
        throws Exception
    {
        sb.append("\t");
        sb.append("__EOS__");
        sb.append("\n");
        sb.append("\n");
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
