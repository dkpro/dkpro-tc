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

package org.dkpro.tc.crfsuite;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.crfsuite.task.CRFSuiteTestTask;

/**
 * Writes a instanceId / outcome pair for each classification instance.
 */
public class CRFSuiteOutcomeIDReport
    extends ReportBase
{

    /**
     * Character that is used for separating fields in the output file
     */
    public static final String SEPARATOR_CHAR = ";";

    private static final String ID_CONSTANT_VALUE = Constants.ID_FEATURE_NAME + "=";

    @Override
    public void execute()
        throws Exception
    {
        List<String> labelGoldVsActual = getGoldAndPredictions();

        HashMap<String, Integer> mapping = createMappingLabel2Number(labelGoldVsActual);

        List<String> testData = getTestData();

        String entries = generateProperties(mapping, labelGoldVsActual, testData);

        // add "#labels' line with all labels
        StringBuilder sb = new StringBuilder();
        sb.append("labels");
        for (String label : mapping.keySet()) {
            sb.append(" " + mapping.get(label) + "=" + URLEncoder.encode(label, "UTF-8"));
        }

        File id2o = getContext().getFile(Constants.ID_OUTCOME_KEY, AccessMode.READWRITE);

        String header = "#" + "ID=PREDICTION" + SEPARATOR_CHAR + "GOLDSTANDARD" + "\n" +"#"+ sb.toString();

        FileUtils.writeStringToFile(id2o, header + "\n" + entries, "utf-8");
    }

    private HashMap<String, Integer> createMappingLabel2Number(List<String> aLabelGoldVsActual)
    {
        HashSet<String> labels = new HashSet<String>();

        int maxLines = aLabelGoldVsActual.size();
        for (int i = 0; i < maxLines; i++) {
            if (i == 0) {
                // skip the headline row
                continue;
            }
            String line = aLabelGoldVsActual.get(i);
            String[] split = line.split("\t");
            if (split.length == 0) {
                continue;
            }
            if (split.length >= 1) {
                labels.add(split[0]);
            }
            if (split.length >= 2) {
                labels.add(split[1]);
            }
        }

        HashMap<String, Integer> map = new HashMap<String, Integer>();
        int i = 0;
        for (String label : labels) {
            if (label.isEmpty()) {
                continue;
            }
            map.put(label, i);
            i++;
        }

        return map;
    }

    private List<String> getTestData()
        throws Exception
    {
        File storage = getContext().getFolder(CRFSuiteTestTask.TEST_TASK_INPUT_KEY_TEST_DATA,
                AccessMode.READONLY);

        File testFile = new File(storage.getAbsolutePath()
                + "/"
                + CRFSuiteAdapter.getInstance().getFrameworkFilename(
                        AdapterNameEntries.featureVectorsFile));

        List<String> readLines = FileUtils.readLines(testFile, "UTF-8");

        return readLines;
    }

    private List<String> getGoldAndPredictions()
        throws Exception
    {
        File predictionFile = getContext().getFile(
                CRFSuiteAdapter.getInstance().getFrameworkFilename(
                        AdapterNameEntries.predictionsFile), AccessMode.READONLY);
        List<String> readLines = FileUtils.readLines(predictionFile, "UTF-8");

        return readLines;
    }

    protected static String generateProperties(HashMap<String, Integer> aMapping,
            List<String> predictions, List<String> testFeatures)
        throws Exception
    {
        List<String> entries = new ArrayList<String>();

        int maxLines = predictions.size();

        for (int idx = 1; idx < maxLines; idx++) {
            String entry = predictions.get(idx);
            String[] split = entry.split("\t");
            if (split.length != 2) {
                continue;
            }
            String featureEntry = testFeatures.get(idx - 1);
            String id = extractTCId(featureEntry);
            int numGold = aMapping.get(split[0]);
            int numPred = aMapping.get(split[1]);
            String e = id + "=" + numPred + SEPARATOR_CHAR + numGold;
            entries.add(e);
        }

        Collections.sort(entries);
        StringBuilder builder = new StringBuilder();
        for (String e : entries) {
            builder.append(e + "\n");
        }
        return builder.toString();
    }

    private static String extractTCId(String line)
    {
        int begin = line.indexOf(ID_CONSTANT_VALUE);
        int end = line.indexOf("\t", begin + ID_CONSTANT_VALUE.length());

        // Assuming the ID is at the end of the line:
        String id = line.substring(begin + ID_CONSTANT_VALUE.length(), line.length());
        // But in case it's not:
        if (end != -1) {
            id = line.substring(begin + ID_CONSTANT_VALUE.length(), end);
        }
        return id;
    }
}