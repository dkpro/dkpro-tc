/*******************************************************************************
 * Copyright 2019
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

package org.dkpro.tc.ml.crfsuite.reports;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.ml.crfsuite.CrfSuiteTestTask;
import org.dkpro.tc.ml.crfsuite.writer.LabelSubstitutor;
import org.dkpro.tc.ml.report.TcAbstractReport;
import org.dkpro.tc.ml.report.util.SortedKeyProperties;

/**
 * Writes a instanceId / outcome pair for each classification instance.
 */
public class CrfSuiteOutcomeIDReport
    extends TcAbstractReport
{

    /**
     * Character that is used for separating fields in the output file
     */
    public static final String SEPARATOR_CHAR = ";";

    private static final String ID_CONSTANT_VALUE = ID_FEATURE_NAME + "=";

    private static final String THRESHOLD_DUMMY_CONSTANT = "-1";

    public CrfSuiteOutcomeIDReport()
    {
        // requried by groovy
    }

    @Override
    public void execute() throws Exception
    {
        prepareBaseline();

        List<String> labelGoldVsActual = getGoldAndPredictions();

        Map<String, Integer> mapping = createMappingLabel2Number();

        List<String> testData = getTestData();

        Properties prop = generateProperties(mapping, labelGoldVsActual, testData);

        // add "#labels' line with all labels
        StringBuilder sb = new StringBuilder();
        sb.append("labels");
        for (Entry<String, Integer> e : mapping.entrySet()) {
            sb.append(" " + e.getValue() + "=" + URLEncoder.encode(e.getKey(), "UTF-8"));
        }

        File id2o = getTargetFile();

        String header = "ID=PREDICTION" + SEPARATOR_CHAR + "GOLDSTANDARD" + SEPARATOR_CHAR
                + "THRESHOLD" + "\n" + "#" + sb.toString();

        try(OutputStreamWriter osw =  new OutputStreamWriter(new FileOutputStream(id2o), UTF_8)){
            prop.store(osw, header);
        }
    }

    protected void prepareBaseline() throws Exception
    {
        // overwritten by baseline reports
    }

    protected File getTargetFile()
    {
        return getContext().getFile(ID_OUTCOME_KEY, AccessMode.READWRITE);
    }

    private Map<String, Integer> createMappingLabel2Number() throws Exception
    {
        File outcomeFolder = getContext().getFolder(OUTCOMES_INPUT_KEY, AccessMode.READONLY);
        File outcomeFiles = new File(outcomeFolder, FILENAME_OUTCOMES);
        List<String> outcomes = FileUtils.readLines(outcomeFiles, UTF_8);

        // Crfsuite might output a null label in rare cases
        outcomes.add("(null)"); 
        
        Map<String, Integer> map = new HashMap<String, Integer>();
        int i = 0;
        for (String label : outcomes) {
            if (label.isEmpty()) {
                continue;
            }
            map.put(label, i);
            i++;
        }

        return map;
    }

    private List<String> getTestData() throws Exception
    {
        File storage = getContext().getFolder(CrfSuiteTestTask.TEST_TASK_INPUT_KEY_TEST_DATA,
                AccessMode.READONLY);

        File testFile = new File(
                storage.getAbsolutePath() + "/" + FILENAME_DATA_IN_CLASSIFIER_FORMAT);

        List<String> readLines = FileUtils.readLines(testFile, UTF_8);

        return readLines;
    }

    private List<String> getGoldAndPredictions() throws Exception
    {
        File predictionFile = getContext().getFile(FILENAME_PREDICTIONS, AccessMode.READONLY);
        List<String> readLines = FileUtils.readLines(predictionFile, UTF_8);

        return readLines;
    }

    protected Properties generateProperties(Map<String, Integer> aMapping, List<String> predictions,
            List<String> testFeatures)
        throws Exception
    {
        Properties p = new SortedKeyProperties();

        int maxLines = predictions.size();

        for (int idx = 1; idx < maxLines; idx++) {
            String entry = predictions.get(idx);
            String[] split = entry.split("\t");
            if (split.length != 2) {
                continue;
            }
            String featureEntry = testFeatures.get(idx - 1);
            String id = extractTCId(featureEntry);
            String[] idsplit = id.split("_");
            // make ids sortable by enforcing zero-prefixing
            String zeroPaddedId = String.format("%04d_%04d_%04d%s", 
                                                Integer.valueOf(idsplit[0]),
                                                Integer.valueOf(idsplit[1]), 
                                                Integer.valueOf(idsplit[2]),
                                                idsplit.length > 3 ? "_" + idsplit[3] : "");
            
            int numGold = aMapping.get(LabelSubstitutor.undoLabelReplacement(split[0]));
            String numPred = getPrediction(aMapping, LabelSubstitutor.undoLabelReplacement(split[1]));
            p.setProperty(zeroPaddedId,
                    numPred + SEPARATOR_CHAR + numGold + SEPARATOR_CHAR + THRESHOLD_DUMMY_CONSTANT);
        }

        return p;
    }

    protected String getPrediction(Map<String, Integer> map, String s)
    {
        // overwritten by baseline report
        return map.get(s).toString();
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