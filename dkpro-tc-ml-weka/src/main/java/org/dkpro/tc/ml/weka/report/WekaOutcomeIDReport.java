/**
 * Copyright 2018
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
package org.dkpro.tc.ml.weka.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.ml.report.TcAbstractReport;
import org.dkpro.tc.ml.weka.core._eka;
import org.dkpro.tc.ml.weka.task.WekaOutcomeHarmonizer;
import org.dkpro.tc.ml.weka.task.WekaTestTask;
import org.dkpro.tc.ml.weka.util.MultilabelResult;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import static java.nio.charset.StandardCharsets.UTF_8;
/**
 * Writes a instanceId / outcome data for each classification instance.
 */
public class WekaOutcomeIDReport
    extends TcAbstractReport
{
    /**
     * Character that is used for separating fields in the output file
     */
    public static final String SEPARATOR_CHAR = ";";

    private File mlResults;

    public WekaOutcomeIDReport()
    {
        // required by groovy
    }

    boolean isRegression;
    boolean isUnit;
    boolean isMultiLabel;

    protected void init()
    {
        isRegression = getDiscriminator(getContext(), DIM_LEARNING_MODE).equals(LM_REGRESSION);
        isUnit = getDiscriminator(getContext(), DIM_FEATURE_MODE).equals(FM_UNIT);
        isMultiLabel = getDiscriminator(getContext(), DIM_LEARNING_MODE).equals(LM_MULTI_LABEL);
    }

    @Override
    public void execute() throws Exception
    {

        init();

        File arff = getFile(getContext(), "", FILENAME_PREDICTIONS, AccessMode.READONLY);
        mlResults = getFile(getContext(), "", WekaTestTask.evaluationBin, AccessMode.READONLY);

        Instances predictions = _eka.getInstances(arff, isMultiLabel);

        List<String> labels = getLabels(isMultiLabel, isRegression);

        String content;
        if (isMultiLabel) {
            MultilabelResult r = readMlResultFromFile(mlResults);
            content = generateMlProperties(predictions, labels, r);
        }
        else {
            Map<Integer, MetaData> documentIdMap = loadDocumentMap();
            content = generateSlProperties(predictions, isRegression, isUnit, documentIdMap, labels);
        }

        String header = generateHeader(labels);
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String timeStamp = dateFormat.format(cal.getTime());
        
        String data = header + "\n#" + timeStamp + "\n" + content;
        
        FileUtils.writeStringToFile(getTargetOutputFile(), data, UTF_8);
    }

    protected File getTargetOutputFile()
    {
        File evaluationFolder = getContext().getFolder("", AccessMode.READWRITE);
        return new File(evaluationFolder, ID_OUTCOME_KEY);
    }

    private List<String> getLabels(boolean multiLabel, boolean regression) throws IOException
    {
        if (regression) {
            return Collections.emptyList();
        }

        File outcomeFolder = getContext().getFolder(OUTCOMES_INPUT_KEY, AccessMode.READONLY);
        File outcomeFiles = new File(outcomeFolder, FILENAME_OUTCOMES);
        List<String> outcomes = FileUtils.readLines(outcomeFiles, UTF_8);
        Collections.sort(outcomes);
        return outcomes;
        // return WekaUtils.getClassLabels(predictions, multiLabel);
    }

    protected static String generateHeader(List<String> labels) throws UnsupportedEncodingException
    {
        StringBuilder comment = new StringBuilder();
        comment.append("#ID=PREDICTION" + SEPARATOR_CHAR + "GOLDSTANDARD" + SEPARATOR_CHAR
                + "THRESHOLD" + "\n#" + "labels");

        // add numbered indexing of labels: e.g. 0=NPg, 1=JJ
        for (int i = 0; i < labels.size(); i++) {
            comment.append(
                    " " + String.valueOf(i) + "=" + URLEncoder.encode(labels.get(i), "UTF-8"));
        }
        return comment.toString();
    }

    protected static String generateMlProperties(Instances predictions, List<String> labels,
            MultilabelResult r)
        throws ClassNotFoundException, IOException
    {
        StringBuilder sb = new StringBuilder();
        int attOffset = predictions.attribute(ID_FEATURE_NAME).index();

        Map<String, Integer> class2number = classNamesToMapping(labels);
        int[][] goldmatrix = r.getGoldstandard();
        double[][] predictionsmatrix = r.getPredictions();
        double bipartition = r.getBipartitionThreshold();

        for (int i = 0; i < goldmatrix.length; i++) {
            Double[] predList = new Double[labels.size()];
            Integer[] goldList = new Integer[labels.size()];
            for (int j = 0; j < goldmatrix[i].length; j++) {
                int classNo = class2number.get(labels.get(j));
                goldList[classNo] = goldmatrix[i][j];
                predList[classNo] = predictionsmatrix[i][j];
            }
            String s = (StringUtils.join(predList, ",") + SEPARATOR_CHAR
                    + StringUtils.join(goldList, ",") + SEPARATOR_CHAR + bipartition);
            String stringValue = predictions.get(i).stringValue(attOffset);
            sb.append(stringValue + "=" + s+"\n");
        }
        return sb.toString();
    }

    protected String generateSlProperties(Instances predictions, boolean isRegression,
            boolean isUnit, Map<Integer, MetaData> documentIdMap, List<String> labels)
        throws Exception
    {

        String[] classValues = new String[predictions.numClasses()];

        for (int i = 0; i < predictions.numClasses(); i++) {
            classValues[i] = predictions.classAttribute().value(i);
        }

        int attOffset = predictions.attribute(ID_FEATURE_NAME).index();

        prepareBaseline();

        StringBuilder sb = new StringBuilder();

        int idx = 0;
        for (Instance inst : predictions) {
            Double gold = getGold(inst, predictions);
           
            Attribute gsAtt = predictions.attribute(WekaTestTask.PREDICTION_CLASS_LABEL_NAME);
            Double prediction = new Double(inst.value(gsAtt));

            String key = inst.stringValue(attOffset);
            if (!isRegression) {
                String goldAsNumber = getIntValueOfGoldLabel(classValues, labels, gold);
                String predicitonAsNumber = getPrediction(prediction, labels, gsAtt);
                key = assignKeyFromMetaData(key, isUnit, documentIdMap, idx++);
                
                sb.append(
                        key + "=" + predicitonAsNumber + SEPARATOR_CHAR
                                + goldAsNumber + SEPARATOR_CHAR + String.valueOf(-1) + "\n");
            }
            else {
                sb.append(key + "=" + prediction + SEPARATOR_CHAR + gold + SEPARATOR_CHAR + "-1"
                        + "\n");
            }
        }
        return sb.toString();
    }

    private String getIntValueOfGoldLabel(String[] classValues, List<String> labels, Double gold)
    {
        Map<String, Integer> class2number = classNamesToMapping(labels);
        return class2number.get(classValues[gold.intValue()]).toString();
    }

    private Double getGold(Instance inst, Instances predictions)
    {
        Double gold;
        try {
            gold = new Double(inst.value(predictions.attribute(
                    CLASS_ATTRIBUTE_NAME + WekaOutcomeHarmonizer.COMPATIBLE_OUTCOME_CLASS)));
        }
        catch (NullPointerException e) {
            // if train and test data have not been balanced
            gold = new Double(inst.value(predictions.attribute(CLASS_ATTRIBUTE_NAME)));
        }
        
        return gold;
    }

    private String assignKeyFromMetaData(String key, boolean isUnit, Map<Integer, MetaData> documentIdMap,
            int mapIdx)
    {
        if (documentIdMap == null) {
            return key;
        }
        MetaData metaData = documentIdMap.get(mapIdx);
        if (!isUnit) {
            if (metaData.title != null && !metaData.title.isEmpty()) {
                key = metaData.title;
            }
            else {
                key = metaData.id;
            }
        }

        return key;
    }

    protected String getPrediction(Double prediction, List<String> labels,
            Attribute gsAtt)
    {
        Map<String, Integer> class2number = classNamesToMapping(labels);
        return class2number.get(gsAtt.value(prediction.intValue())).toString();
    }

    protected void prepareBaseline() throws Exception
    {
        // is overwritten in baseline reports
    }

    protected static Map<String, Integer> classNamesToMapping(List<String> labels)
    {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        for (int i = 0; i < labels.size(); i++) {
            mapping.put(labels.get(i), i);
        }
        return mapping;
    }

    private Map<Integer, MetaData> loadDocumentMap() throws IOException
    {

        Map<Integer, MetaData> documentIdMap = new HashMap<>();

        File f = new File(
                getContext().getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY),
                FILENAME_DOCUMENT_META_DATA_LOG);
        List<String> readLines = FileUtils.readLines(f, UTF_8);

        int idx = 0;
        for (String l : readLines) {
            if (l.startsWith("#")) {
                continue;
            }
            
            String[] split = l.split("\t");
			if (!l.contains("\t")) {
				split = new String[] { l, "" };
			}
            
            MetaData md = new MetaData(split[0], split[1]);
            documentIdMap.put(idx, md);
            idx++;
        }

        return documentIdMap;
    }

    private MultilabelResult readMlResultFromFile(File file)
        throws IOException, ClassNotFoundException
    {
        ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file));
        MultilabelResult result = (MultilabelResult) stream.readObject();
        stream.close();
        return result;
    }

    private File getFile(TaskContext aContext, String key, String entry, AccessMode mode)
    {
        String path = aContext.getFolder(key, mode).getPath();
        String pathToArff = path + "/" + entry;

        return new File(pathToArff);
    }
    
    class MetaData{
        String id;
        String title;

        public MetaData(String id, String title) {
            this.id = id;
            this.title = title;
            
        }
        
        @Override
        public String toString() {
            return "[" + id + "] = [" + title + "]";
        }
    }
}