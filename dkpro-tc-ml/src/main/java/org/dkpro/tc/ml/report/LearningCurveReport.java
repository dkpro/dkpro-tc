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
package org.dkpro.tc.ml.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.ChartUtil;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.impl.TaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;
import org.dkpro.tc.core.util.ReportUtils;
import org.dkpro.tc.ml.report.util.Tc2LtlabEvalConverter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.Accuracy;
import de.unidue.ltl.evaluation.measures.EvaluationMeasure;
import de.unidue.ltl.evaluation.measures.correlation.SpearmanCorrelation;

/**
 * Collects the final evaluation results in a cross validation setting.
 * 
 */
public class LearningCurveReport
    extends TcAbstractReport
    implements Constants
{
    @Override
    public void execute() throws Exception
    {
        StorageService store = getContext().getStorageService();
        Set<String> idPool = getTaskIdsFromMetaData(getSubtasks());
        String learningMode = determineLearningMode(store, idPool);

        writeOverallResults(learningMode, store, idPool);

        if (isSingleLabelMode(learningMode)) {
            writeCategoricalResults(learningMode, store, idPool);
        }

    }

    private void writeOverallResults(String learningMode, StorageService store, Set<String> idPool)
        throws Exception
    {

        Map<RunIdentifier, Map<Integer, List<File>>> dataMap = new HashMap<>();

        Set<String> collectSubtasks = null;
        for (String id : idPool) {

            if (!TcTaskTypeUtil.isCrossValidationTask(store, id)) {
                continue;
            }
            collectSubtasks = collectSubtasks(id);

            Map<RunIdentifier, Map<Integer, List<File>>> run = collectRuns(store, collectSubtasks);
            dataMap.putAll(run);
        }

        @SuppressWarnings("rawtypes")
        Class<? extends EvaluationMeasure> metric = null;
        if (learningMode.equals(LM_SINGLE_LABEL)) {
            metric = Accuracy.class;
            for (RunIdentifier configId : dataMap.keySet()) {
                List<Double> stageAveraged = averagePerStage(dataMap.get(configId), metric);
                writePlot(configId.md5, stageAveraged, getMaxValue(dataMap.get(configId)), metric.getSimpleName());
                stageAveraged.forEach(v -> System.out.println(v));
            }
        } else if (learningMode.equals(LM_REGRESSION)) {
            metric = SpearmanCorrelation.class;
            for (RunIdentifier configId : dataMap.keySet()) {
                List<Double> stageAveraged = averagePerStage(dataMap.get(configId), metric);
                writePlot(configId.md5, stageAveraged, getMaxValue(dataMap.get(configId)), metric.getSimpleName());
                stageAveraged.forEach(v -> System.out.println(v));
            }
        }

        StringBuilder sb = new StringBuilder();
        for (RunIdentifier configId : dataMap.keySet()) {
            sb.append(configId.md5 + "\t" + configId.classification + configId.featureset + "\n");
        }

        FileUtils.writeStringToFile(getContext().getFile("md5Mapping.txt", AccessMode.READWRITE),
                sb.toString(), "utf-8");
    }

    private int getMaxValue(Map<Integer, List<File>> map)
    {
        int maxFolds = -1;
        for (List<File> f : map.values()) {
            if (f.size() > maxFolds) {
                maxFolds = f.size();
            }
        }

        return maxFolds;
    }

    @SuppressWarnings("rawtypes")
    private List<Double> averagePerStage(Map<Integer, List<File>> map,
            Class<? extends EvaluationMeasure> class1)
        throws Exception
    {
        List<Double> stageAveraged = new ArrayList<>();

        List<Integer> keys = new ArrayList<Integer>(map.keySet());
        Collections.sort(keys);
        for (Integer numFolds : keys) {
            List<File> st = map.get(numFolds);
            EvaluationData<String> stageData = new EvaluationData<>();
            for (File f : st) {
                EvaluationData<String> run = Tc2LtlabEvalConverter
                        .convertSingleLabelModeId2Outcome(f);
                stageData.registerBulk(run);
            }
            EvaluationMeasure measure = class1.getDeclaredConstructor(EvaluationData.class).newInstance(stageData);
            stageAveraged.add(measure.getResult());
        }
        return stageAveraged;
    }

    private Map<RunIdentifier, Map<Integer, List<File>>> collectRuns(StorageService store,
            Set<String> collectSubtasks)
        throws Exception
    {
        Map<RunIdentifier, Map<Integer, List<File>>> dataMap = new HashMap<>();
        List<String> sortedTasks = new ArrayList<>(collectSubtasks);
        Collections.sort(sortedTasks);
        for (String sId : sortedTasks) {
            if (!TcTaskTypeUtil.isMachineLearningAdapterTask(store, sId)) {
                continue;
            }

            int numberOfTrainFolds = getNumberOfTrainingFolds(store, sId);

            RunIdentifier configurationId = generateId(store, sId);
            Map<Integer, List<File>> idRun = dataMap.get(configurationId);

            if (idRun == null) {
                idRun = new HashMap<>();
            }

            List<File> stage = idRun.get(numberOfTrainFolds);
            if (stage == null) {
                stage = new ArrayList<>();
            }
            File f = store.locateKey(sId, ID_OUTCOME_KEY);
            stage.add(f);
            idRun.put(numberOfTrainFolds, stage);
            dataMap.put(configurationId, idRun);
        }
        return dataMap;
    }

    private RunIdentifier generateId(StorageService store, String sId) throws Exception
    {
        Properties p = new Properties();
        File locateKey = store.locateKey(sId, TaskBase.DISCRIMINATORS_KEY);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(locateKey);
            p.load(fis);
        }
        finally {
            IOUtils.closeQuietly(fis);
        }

        Map<String, String> m = new HashMap<>();
        for (Entry<Object, Object> e : p.entrySet()) {
            m.put(e.getKey().toString(), e.getValue().toString());
        }

        m = ReportUtils.removeKeyRedundancy(m);

        String classification = m.get(DIM_CLASSIFICATION_ARGS);
        String featureSet = m.get(DIM_FEATURE_SET);

        return new RunIdentifier(classification, featureSet);
    }

    private int getNumberOfTrainingFolds(StorageService store, String sId) throws Exception
    {

        Properties p = new Properties();
        File locateKey = store.locateKey(sId, CONFIGURATION_DKPRO_LAB);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(locateKey);
            p.load(fis);
            String foldValue = p.getProperty(DIM_NUM_TRAINING_FOLDS);

            if (foldValue == null) {
                throw new IllegalArgumentException(
                        "Retrieved null when retrieving the discriminator ["
                                + DIM_NUM_TRAINING_FOLDS + "]");
            }

            if (foldValue.contains(",")) {
                String[] split = foldValue.split(",");
                return split.length;
            }

        }
        finally {
            IOUtils.closeQuietly(fis);
        }

        return 1;
    }

    private void writePlot(String id, List<Double> stageAveraged, int maxFolds, String metricName) throws Exception
    {
        double x[] = new double[stageAveraged.size() + 1];
        double y[] = new double[stageAveraged.size() + 1];
        for (int i = 1; i < x.length; i++) {
            x[i] = i;
            y[i] = stageAveraged.get(i - 1);
        }

        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] data = new double[2][x.length];
        data[0] = x;
        data[1] = y;
        dataset.addSeries(metricName, data);

        JFreeChart chart = ChartFactory.createXYLineChart("Learning Curve",
                "number of training folds", "performance", dataset, PlotOrientation.VERTICAL, true,
                false, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(renderer);
        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        domain.setRange(0.00, maxFolds);
        domain.setTickUnit(new NumberTickUnit(1.0));
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(0.0, 1.0);
        range.setTickUnit(new NumberTickUnit(0.1));

        File file = getContext().getFile(id + "_learningCurve_" + metricName +".pdf", AccessMode.READWRITE);
        FileOutputStream fos = new FileOutputStream(file);
        ChartUtil.writeChartAsPDF(fos, chart, 400, 400);
        fos.close();
    }

    private boolean isSingleLabelMode(String learningMode)
    {
        return learningMode.equals(Constants.LM_SINGLE_LABEL);
    }

    private void writeCategoricalResults(String learningMode, StorageService store,
            Set<String> idPool)
        throws Exception
    {

    }

    private String determineLearningMode(StorageService store, Set<String> idPool) throws Exception
    {
        String learningMode = getDiscriminator(store, idPool, DIM_LEARNING_MODE);
        if (learningMode == null) {
            for (String id : idPool) {
                Set<String> collectSubtasks = collectSubtasks(id);
                learningMode = getDiscriminator(store, collectSubtasks, DIM_LEARNING_MODE);
                if (learningMode != null) {
                    break;
                }
            }
        }
        return learningMode;
    }

    class RunIdentifier
    {
        String md5;
        String classification;
        String featureset;

        public RunIdentifier(String classification, String featureset)
            throws NoSuchAlgorithmException
        {
            this.classification = classification;
            this.featureset = featureset;

            byte[] digest = MessageDigest.getInstance("MD5")
                    .digest((classification + featureset).getBytes());
            BigInteger bigInt = new BigInteger(1, digest);
            String md5 = bigInt.toString(16);
            this.md5 = md5;
        }

        @Override
        public int hashCode()
        {
            int result = 17;
            result = 31 * result + md5.hashCode();
            return result;
        }

        public boolean equals(Object other)
        {
            if (other == null)
                return false;
            if (other == this)
                return true;
            if (!(other instanceof RunIdentifier))
                return false;
            RunIdentifier otherMyClass = (RunIdentifier) other;
            if (otherMyClass.md5.equals(md5)) {
                return true;
            }
            return false;
        }
    }
}
