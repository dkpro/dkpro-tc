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
package org.dkpro.tc.io.libsvm.reports;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.AdapterFormat;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatWriter;
import org.dkpro.tc.ml.report.TcBatchReportBase;

public class LibsvmDataFormatOutcomeIdReport
    extends TcBatchReportBase
    implements Constants
{

    private String THRESHOLD_CONSTANT = "-1";

    public LibsvmDataFormatOutcomeIdReport()
    {

    }

    boolean isRegression;
    boolean isUnit;
    boolean isSequence;

    @Override
    public void execute() throws Exception
    {

        init();

        baslinePreparation();

        Map<Integer, String> id2label = getId2LabelMapping(isRegression);
        String header = buildHeader(id2label, isRegression);

        List<String> predictions = readPredictions();
        Map<String, String> index2instanceIdMap = getMapping(isUnit || isSequence);

        int lineCounter = 0;
        StringBuilder sb = new StringBuilder();
        for (String line : predictions) {
            if (line.startsWith("#")) {
                continue;
            }
            String[] split = line.split(";");
            String key = index2instanceIdMap.get(lineCounter + "");

            String predictionString = getPrediction(split[0]);
            String goldString = split[1];

            if (isRegression) {
                sb.append(key + "=" + predictionString + ";" + goldString + ";" + THRESHOLD_CONSTANT
                        + "\n");
            }
            else {
                int pred = Double.valueOf(predictionString).intValue();
                int gold = Double.valueOf(goldString).intValue();
                sb.append(key + "=" + pred + ";" + gold + ";" + THRESHOLD_CONSTANT
                        + "\n");
            }
            lineCounter++;
        }

        File targetFile = getTargetOutputFile();
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String timeStamp = dateFormat.format(cal.getTime());
        
        String content = header + "\n#" + timeStamp + "\n" + sb.toString();
        FileUtils.writeStringToFile(targetFile, content, "utf-8");

    }

    protected void init()
    {
        String threshold = getDiscriminator(getContext(), Constants.DIM_BIPARTITION_THRESHOLD);
        if (threshold != null) {
            THRESHOLD_CONSTANT = threshold;
        }

        isRegression = getDiscriminator(getContext(), Constants.DIM_LEARNING_MODE)
                .equals(Constants.LM_REGRESSION);
        isUnit = getDiscriminator(getContext(), Constants.DIM_FEATURE_MODE)
                .equals(Constants.FM_UNIT);
        isSequence = getDiscriminator(getContext(), Constants.DIM_FEATURE_MODE)
                .equals(Constants.FM_SEQUENCE);
    }

    protected String getPrediction(String string)
    {
        // This method is overloaded in the baseline reports to return an
        // appropriate baseline value here
        return string;
    }

    protected void baslinePreparation() throws Exception
    {
        // This method is overloaded in a subclass for performing some
        // initialization for computing baseline values
    }

    private Map<String, String> getMapping(boolean isUnit) throws IOException
    {

        File f;
        if (isUnit) {
            f = new File(getContext().getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY),
                    LibsvmDataFormatWriter.INDEX2INSTANCEID);
        }
        else {
            f = new File(getContext().getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY),
                    Constants.FILENAME_DOCUMENT_META_DATA_LOG);
        }

        Map<String, String> m = new HashMap<>();

        int idx = 0;
        for (String l : FileUtils.readLines(f, "utf-8")) {
            if (l.startsWith("#")) {
                continue;
            }
            if (l.trim().isEmpty()) {
                continue;
            }
            String[] split = l.split("\t");
            
            //not title set in the reader that could be retrieved
            String value="";
            if(split.length >= 2) {
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
        return FileUtils.readLines(new File(predFolder, Constants.FILENAME_PREDICTIONS), "utf-8");
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
            header.append(key + "=" + URLEncoder.encode(id2label.get(key), "UTF-8"));
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
        File outcomeFiles = new File(outcomeFolder, AdapterFormat.getOutcomeMappingFilename());
        List<String> outcomes = FileUtils.readLines(outcomeFiles, "utf-8");

        Map<Integer, String> map = new HashMap<Integer, String>();
        for (String line : outcomes) {
            String[] split = line.split("\t");
            map.put(Integer.parseInt(split[1]), split[0]);
        }

        return map;
    }

}