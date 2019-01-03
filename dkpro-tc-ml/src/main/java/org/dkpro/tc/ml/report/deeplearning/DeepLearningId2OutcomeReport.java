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

package org.dkpro.tc.ml.report.deeplearning;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.core.ml.TcDeepLearningAdapter;
import org.dkpro.tc.ml.report.TcAbstractReport;

public class DeepLearningId2OutcomeReport
    extends TcAbstractReport
    implements Constants, DeepLearningConstants
{

    /**
     * Character that is used for separating fields in the output file
     */
    public static final String SEPARATOR_CHAR = ";";

    private String THRESHOLD = "-1";

    int counter = 0;

    boolean isMultiLabel;
    boolean isRegression;
    boolean isIntegerMode;

    @Override
    public void execute() throws Exception
    {

        init();

        baselinePreparation();

        File file = getContext().getFile(FILENAME_PREDICTION_OUT, AccessMode.READONLY);
        List<String> predictions = getPredictions(file);

        predictions = update(predictions);

        Map<String, String> map = loadMap(isIntegerMode);
        Map<String, String> inverseMap = inverseMap(map);

        StringBuilder header = new StringBuilder();
        header.append("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD\n#labels ");

        List<String> k = new ArrayList<>(map.keySet());
        for (Integer i = 0; i < map.keySet().size(); i++) {
            if (!isRegression) {
                if (!isIntegerMode) {
                    header.append(i + "=" + inverseMap.get(i + ""));
                }
                else {
                    header.append(i + "=" + map.get(i + ""));
                }
                if (i + 1 < k.size()) {
                    header.append(" ");
                }
            }
        }

        List<String> nameOfTargets = getNameOfTargets();
        StringBuilder sb = new StringBuilder();
        
        int shift = 0;
        for (int i = 0; i < predictions.size(); i++) {

            String p = predictions.get(i);
            if (p.startsWith("#Gold")) {
                // header line exists in the prediction file and in the name of
                // targets files
                continue;
            }
            if (p.trim().isEmpty()) {
                shift++;
                continue;
            }

            String id = determineId(nameOfTargets, i, shift);

            String[] split = p.split("\t");

            if (isMultiLabel) {
                sb = multilabelReport(id, split, isIntegerMode, sb, map);
                continue;
            }

            String gold = null;
            String prediction = null;

            if (isRegression) {
                gold = split[0];
                prediction = split[1];
            }
            else {
                if (isIntegerMode) {
                    gold = split[0];
                    prediction = split[1];
                }
                else {
                    gold = map.get(split[0]).toString();
                    prediction = map.get(split[1]).toString();
                }
            }
            sb.append(id + "=" + prediction + SEPARATOR_CHAR + gold + SEPARATOR_CHAR + THRESHOLD + "\n");
        }
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String timeStamp = dateFormat.format(cal.getTime());

        String data = header.toString() + "\n#" + timeStamp + "\n" + sb.toString();
        File id2o = getTargetFile();
        FileUtils.writeStringToFile(id2o, data, "utf-8");
    }

    private String determineId(List<String> nameOfTargets, int i, int shift)
    {
        return !nameOfTargets.isEmpty() && nameOfTargets.size() > (i - shift)
                ? nameOfTargets.get(i - shift)
                : "" + (counter++);
    }

    protected List<String> update(List<String> predictions)
    {
        // is overwritten in baseline reports
        return predictions;
    }

    protected File getTargetFile()
    {
        return getContext().getFile(Constants.ID_OUTCOME_KEY, AccessMode.READWRITE);
    }

    protected void baselinePreparation() throws Exception
    {
        // is overwritten in baseline reports
    }

    protected void init()
    {
        isMultiLabel = getDiscriminator(getContext(), DIM_LEARNING_MODE).equals(LM_MULTI_LABEL);
        isRegression = getDiscriminator(getContext(), DIM_LEARNING_MODE).equals(LM_REGRESSION);

        if (isMultiLabel) {
            THRESHOLD = getDiscriminator(getContext(), DIM_BIPARTITION_THRESHOLD);
        }

        isIntegerMode = Boolean.valueOf(getDiscriminator(getContext(), DIM_VECTORIZE_TO_INTEGER));
    }

    private Map<String, String> inverseMap(Map<String, String> map)
    {
        HashMap<String, String> inverseMap = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            inverseMap.put(entry.getValue(), entry.getKey());
        }
        return inverseMap;
    }

    private StringBuilder multilabelReport(String id, String[] split, boolean isIntegerMode, StringBuilder sb,
            Map<String, String> map)
    {

        String gold = null;
        String prediction = null;
        if (isIntegerMode) {
            String[] s = split[0].split(" ");
            gold = StringUtils.join(s, ",");

            s = split[1].split(" ");
            prediction = StringUtils.join(s, ",");
        }
        else {
            String[] s = split[0].split(" ");
            gold = label2String(s, map);

            s = split[1].split(" ");
            prediction = label2String(s, map);
        }
        sb.append(id + "=" +  prediction + SEPARATOR_CHAR + gold + SEPARATOR_CHAR + THRESHOLD + "\n");
        return sb;
    }

    private String label2String(String[] val, Map<String, String> map)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < val.length; i++) {
            String e = val[i];
            sb.append(map.get(e.toString()));
            if (i + 1 < val.length) {
                sb.append(",");
            }
        }
        return sb.toString().trim();
    }

    private Map<String, String> loadMap(boolean isIntegerMode) throws IOException
    {

        Map<String, String> m = new HashMap<>();

        if (isIntegerMode) {

            File prepFolder = getContext().getFolder(TcDeepLearningAdapter.PREPARATION_FOLDER,
                    AccessMode.READONLY);
            File mapping = new File(prepFolder, DeepLearningConstants.FILENAME_OUTCOME_MAPPING);

            List<String> outcomeMappings = FileUtils.readLines(mapping, "utf-8");
            for (String s : outcomeMappings) {
                String[] split = s.split("\t");
                m.put(split[1], split[0]);
            }
            return m;
        }

        File prepFolder = getContext().getFolder(TcDeepLearningAdapter.PREPARATION_FOLDER,
                AccessMode.READONLY);
        File file = new File(prepFolder, DeepLearningConstants.FILENAME_OUTCOMES);
        List<String> readLines = FileUtils.readLines(file, "utf-8");

        Set<String> keys = new HashSet<>();

        int mapIdx = 0;
        for (int i = 0; i < readLines.size(); i++) {
            String l = readLines.get(i);
            if (l.isEmpty()) {
                continue;
            }

            keys.add(l);
        }

        List<String> sortedKeys = new ArrayList<String>(keys);
        Collections.sort(sortedKeys);

        for (String k : sortedKeys) {
            String string = m.get(k);
            if (string == null) {
                m.put(k, "" + (mapIdx++));
            }
        }

        return m;
    }

    protected List<String> getPredictions(File file) throws IOException
    {
        List<String> readLines = FileUtils.readLines(file, "utf-8");
        return readLines.subList(1, readLines.size());// ignore first-line with
                                                      // comments
    }

    private List<String> getNameOfTargets() throws IOException
    {
        File targetIdMappingFolder = getContext()
                .getFolder(TcDeepLearningAdapter.TARGET_ID_MAPPING_TEST, AccessMode.READONLY);
        File targetIdMappingFile = new File(targetIdMappingFolder,
                DeepLearningConstants.FILENAME_TARGET_ID_TO_INDEX);

        List<String> t = new ArrayList<>();

        List<String> readLines = FileUtils.readLines(targetIdMappingFile, "utf-8");
        for (String s : readLines) {
            if (s.startsWith("#")) {
                continue;
            }
            if (s.isEmpty()) {
                t.add("");
                continue;
            }

            String[] split = s.split("\t");
            if (split[0].contains("_")) {
                t.add(s.replaceAll("\t", "_"));
            }
            else {
                t.add(split[1]);
            }
        }

        return t;
    }

}