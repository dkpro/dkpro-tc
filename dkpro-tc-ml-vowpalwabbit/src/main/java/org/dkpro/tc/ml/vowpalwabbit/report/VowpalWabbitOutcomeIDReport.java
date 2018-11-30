/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.ml.vowpalwabbit.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.report.TcAbstractReport;
import org.dkpro.tc.ml.vowpalwabbit.writer.VowpalWabbitDataWriter;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Writes a instanceId / outcome data for each classification instance.
 */
public class VowpalWabbitOutcomeIDReport
    extends TcAbstractReport
{
    private static final String THRESHOLD_CONSTANT = "-1";

    public VowpalWabbitOutcomeIDReport()
    {
        // required by groovy
    }

    boolean isRegression;
    boolean isUnit;
    boolean isSequence;
    boolean isMultiLabel;

    protected void init()
    {
        isRegression = getDiscriminator(getContext(), DIM_LEARNING_MODE).equals(LM_REGRESSION);
        isUnit = getDiscriminator(getContext(), DIM_FEATURE_MODE).equals(FM_UNIT);
        isSequence = getDiscriminator(getContext(), DIM_FEATURE_MODE).equals(FM_SEQUENCE);
        isMultiLabel = getDiscriminator(getContext(), DIM_LEARNING_MODE).equals(LM_MULTI_LABEL);
    }

    @Override
    public void execute() throws Exception
    {
        init();
        baslinePreparation();

        Map<Integer, String> id2label = getId2LabelMapping(isRegression);
        String header = buildHeader(id2label, isRegression);

        List<String> predictionValues = readPredictions();
        List<String> goldValues = readGoldValuesFromTestFile();

        if (predictionValues.size() != goldValues.size()) {
            throw new IllegalStateException(
                    "Prediction and found gold values do not match, predictions ["
                            + predictionValues.size() + "] vs gold [" + goldValues.size() + "]");
        }

        Map<String, String> index2instanceIdMap = getMapping(isUnit || isSequence);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < goldValues.size(); i++) {
            String p = getPrediction(predictionValues.get(i));
            String g = goldValues.get(i);

            if (g.isEmpty()) {
                continue;
            }

            String key = index2instanceIdMap.get(i + "");

            if (isRegression) {
                sb.append(key + "=" + p + ";" + g + ";" + THRESHOLD_CONSTANT + "\n");
            }
            else {
                int pred = Double.valueOf(p).intValue();
                int gold = Double.valueOf(g).intValue();
                sb.append(key + "=" + pred + ";" + gold + ";" + THRESHOLD_CONSTANT + "\n");
            }
        }

        File targetFile = getTargetOutputFile();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String timeStamp = dateFormat.format(cal.getTime());

        String content = header + "\n#" + timeStamp + "\n" + sb.toString();
        FileUtils.writeStringToFile(targetFile, content, "utf-8");

    }

    protected void baslinePreparation() throws Exception
    {
        // This method is overloaded in a subclass for performing some
        // initialization for computing baseline values
    }

    private List<String> readGoldValuesFromTestFile() throws Exception
    {
        File f = new File(
                getContext().getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY),
                FILENAME_DATA_IN_CLASSIFIER_FORMAT);

        List<String> gold = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), UTF_8))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(" ");
                gold.add(split[0]);
            }
        }

        return gold;
    }

    protected String getPrediction(String string)
    {
        return string;
    }

    private Map<String, String> getMapping(boolean isUnit) throws IOException
    {

        File f;
        if (isUnit) {
            f = new File(getContext().getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY),
                    VowpalWabbitDataWriter.INDEX2INSTANCEID);
        }
        else {
            f = new File(getContext().getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY),
                    Constants.FILENAME_DOCUMENT_META_DATA_LOG);
        }

        Map<String, String> m = new HashMap<>();

        int idx = 0;
        for (String l : FileUtils.readLines(f, UTF_8)) {
            if (l.startsWith("#")) {
                continue;
            }
            if (l.trim().isEmpty()) {
                continue;
            }
            String[] split = l.split("\t");

            // not title set in the reader that could be retrieved
            String value = "";
            if (split.length >= 2) {
                value = split[1];
            }

            m.put(idx + "", value.isEmpty() ? idx + "" : value);
            idx++;
        }
        return m;
    }

    protected File getTargetOutputFile()
    {
        File evaluationFolder = getContext().getFolder("", AccessMode.READWRITE);
        return new File(evaluationFolder, ID_OUTCOME_KEY);
    }

    protected List<String> readPredictions() throws IOException
    {
        File predFolder = getContext().getFolder("", AccessMode.READWRITE);
        File predictionFile = new File(predFolder, Constants.FILENAME_PREDICTIONS);
        List<String> readLines = null;

        if (isSequence) {
            readLines = new ArrayList<>();
            List<String> tmp = FileUtils.readLines(predictionFile, UTF_8);
            for (String t : tmp) {
                readLines.addAll(Arrays.asList(t.split(" ")));
                readLines.add("");// empty line = end of sequence
            }
        }
        else {
            readLines = FileUtils.readLines(predictionFile, "utf-8");
        }
        return readLines;
    }

    private String buildHeader(Map<Integer, String> id2label, boolean isRegression)
        throws UnsupportedEncodingException
    {
        StringBuilder header = new StringBuilder();
        header.append("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD" + "\n#" + "labels" + " ");

        if (isRegression) {
            // no label mapping for regression so that is all we have to do
            return header.toString();
        }

        int numKeys = id2label.keySet().size();
        List<Integer> keys = new ArrayList<Integer>(id2label.keySet());
        for (int i = 0; i < numKeys; i++) {
            Integer key = keys.get(i);
            header.append(key + "=" + URLEncoder.encode(id2label.get(key), UTF_8.toString()));
            if (i + 1 < numKeys) {
                header.append(" ");
            }
        }
        return header.toString();
    }

    private Map<Integer, String> getId2LabelMapping(boolean isRegression) throws Exception
    {
        if (isRegression) {
            // no map for regression;
            return new HashMap<>();
        }

        File outcomeFolder = getContext().getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        File outcomeFiles = new File(outcomeFolder, VowpalWabbitDataWriter.OUTCOME_MAPPING);
        List<String> outcomes = FileUtils.readLines(outcomeFiles, UTF_8);

        Map<Integer, String> map = new HashMap<Integer, String>();
        for (String line : outcomes) {
            String[] split = line.split("\t");
            map.put(Integer.parseInt(split[1]), split[0]);
        }

        return map;
    }
}