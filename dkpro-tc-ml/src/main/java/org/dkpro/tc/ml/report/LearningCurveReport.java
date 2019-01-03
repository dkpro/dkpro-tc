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
package org.dkpro.tc.ml.report;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

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
import de.unidue.ltl.evaluation.measures.categorial.Fscore;
import de.unidue.ltl.evaluation.measures.categorial.Precision;
import de.unidue.ltl.evaluation.measures.categorial.Recall;
import de.unidue.ltl.evaluation.measures.correlation.PearsonCorrelation;
import de.unidue.ltl.evaluation.measures.correlation.SpearmanCorrelation;

/**
 * Collects the final evaluation results in a cross validation setting.
 */
public class LearningCurveReport
    extends TcAbstractReport
    implements Constants
{
    int maxNumberFolds = -1;
	public static final String MD5_MAPPING_FILE = "md5Mapping.txt";

    @Override
    public void execute() throws Exception
    {
        StorageService store = getContext().getStorageService();
        Set<String> idPool = getTaskIdsFromMetaData(getSubtasks());
        String learningMode = determineLearningMode(store, idPool);

        Map<LearningCurveRunIdentifier, Map<Integer, List<File>>> dataMap = writeOverallResults(learningMode,
                store, idPool);

        if (isSingleLabelMode(learningMode)) {
            writeCategoricalResults(dataMap);
        }

    }

    @SuppressWarnings("rawtypes")
    private Map<LearningCurveRunIdentifier, Map<Integer, List<File>>> writeOverallResults(String learningMode,
            StorageService store, Set<String> idPool)
        throws Exception
    {

        Map<LearningCurveRunIdentifier, Map<Integer, List<File>>> dataMap = new HashMap<>();

        Set<String> collectSubtasks = null;
        for (String id : idPool) {

            if (!TcTaskTypeUtil.isCrossValidationTask(store, id)) {
                continue;
            }
            collectSubtasks = collectSubtasks(id);

            Map<LearningCurveRunIdentifier, Map<Integer, List<File>>> run = collectRuns(store, collectSubtasks);
            dataMap.putAll(run);
        }

        if (learningMode.equals(LM_SINGLE_LABEL)) {
            for (Entry<LearningCurveRunIdentifier,Map<Integer, List<File>>> e : dataMap.entrySet()) {
                List<Double> stageAveraged = averagePerStage(e.getValue(), Accuracy.class, true);
                writePlot(e.getKey().md5, stageAveraged, maxNumberFolds,
                        Accuracy.class.getSimpleName());
            }

        }
        else if (learningMode.equals(LM_REGRESSION)) {
            List<Class<? extends EvaluationMeasure>> regMetrics = new ArrayList<>();
            regMetrics.add(PearsonCorrelation.class);
            regMetrics.add(SpearmanCorrelation.class);
            for (Class<? extends EvaluationMeasure> m : regMetrics) {

                for (Entry<LearningCurveRunIdentifier,Map<Integer, List<File>>> e : dataMap.entrySet()) {
                    List<Double> stageAveraged = averagePerStage(e.getValue(), m, false);
                    writePlot(e.getKey().md5, stageAveraged, maxNumberFolds, m.getSimpleName());
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (LearningCurveRunIdentifier configId : dataMap.keySet()) {
            sb.append(configId.md5 + "\t" + configId.configAsString + "\n");
        }

        FileUtils.writeStringToFile(getContext().getFile(MD5_MAPPING_FILE, AccessMode.READWRITE),
                sb.toString(), UTF_8);

        return dataMap;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private List<Double> averagePerStage(Map<Integer, List<File>> map,
            Class<? extends EvaluationMeasure> class1, boolean isSingleLabel)
        throws Exception
    {
        List<Double> stageAveraged = new ArrayList<>();

        List<Integer> keys = new ArrayList<Integer>(map.keySet());
        Collections.sort(keys);
        for (Integer numFolds : keys) {
            List<File> st = map.get(numFolds);
            EvaluationData stageData = new EvaluationData<>();
            for (File f : st) {
            	EvaluationData run=null;
            	if(isSingleLabel) {
                run = Tc2LtlabEvalConverter
                        .convertSingleLabelModeId2Outcome(f);
            	}else {
            		run = Tc2LtlabEvalConverter
                            .convertRegressionModeId2Outcome(f);
            	}
                stageData.registerBulk(run);
            }
            EvaluationMeasure measure = class1.getDeclaredConstructor(EvaluationData.class)
                    .newInstance(stageData);
            stageAveraged.add(measure.getResult());
        }
        return stageAveraged;
    }

    private Map<LearningCurveRunIdentifier, Map<Integer, List<File>>> collectRuns(StorageService store,
            Set<String> collectSubtasks)
        throws Exception
    {
        Map<LearningCurveRunIdentifier, Map<Integer, List<File>>> dataMap = new HashMap<>();
        List<String> sortedTasks = new ArrayList<>(collectSubtasks);
        Collections.sort(sortedTasks);
        for (String sId : sortedTasks) {
            if (!TcTaskTypeUtil.isMachineLearningAdapterTask(store, sId)) {
                continue;
            }

            int numberOfTrainFolds = getNumberOfTrainingFolds(store, sId);
            if (numberOfTrainFolds > maxNumberFolds) {
                maxNumberFolds = numberOfTrainFolds;
            }

            LearningCurveRunIdentifier configurationId = generateId(store, sId);
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

    private LearningCurveRunIdentifier generateId(StorageService store, String sId) throws Exception
    {
        Properties p = new Properties();
        File locateKey = store.locateKey(sId, TaskBase.DISCRIMINATORS_KEY);
        try(FileInputStream fis = new FileInputStream(locateKey)){
            p.load(fis);
        }

        Map<String, String> m = new HashMap<>();
        for (Entry<Object, Object> e : p.entrySet()) {
            m.put(e.getKey().toString(), e.getValue().toString());
        }

        m = ReportUtils.removeKeyRedundancy(m);

		// Remove these keys as they tend to change in every setup (which is normal) but
		// prevent us from grouping runs together that are the same (except for the data
		// they used)
        m = ReportUtils.replaceKeyWithConstant(m, DIM_READERS, "<REMOVED>");
        m = ReportUtils.replaceKeyWithConstant(m, DIM_READER_TEST, "<REMOVED>");
        m = ReportUtils.replaceKeyWithConstant(m, DIM_READER_TRAIN, "<REMOVED>");
        m = ReportUtils.replaceKeyWithConstant(m, DIM_FILES_ROOT, "<REMOVED>");
        m = ReportUtils.replaceKeyWithConstant(m, DIM_FILES_TRAINING, "<REMOVED>");
        m = ReportUtils.replaceKeyWithConstant(m, DIM_FILES_VALIDATION, "<REMOVED>");
        
        return new LearningCurveRunIdentifier(m);
    }

    private int getNumberOfTrainingFolds(StorageService store, String sId) throws Exception
    {

        Properties p = new Properties();
        File locateKey = store.locateKey(sId, CONFIGURATION_DKPRO_LAB);

        try (FileInputStream fis = new FileInputStream(locateKey)) {
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

        return 1;
    }

    private void writePlot(String id, List<Double> stageAveraged, int maxFolds, String metricName)
        throws Exception
    {
        StringBuilder sb = new StringBuilder();
        sb.append(metricName + "\n");

        double x[] = new double[stageAveraged.size() + 1];
        double y[] = new double[stageAveraged.size() + 1];
        for (int i = 1; i < x.length; i++) {
            x[i] = i;
            y[i] = stageAveraged.get(i - 1);
            sb.append("Stage " + i + ": " + y[i] + "\n");
        }

        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] data = new double[2][x.length];
        data[0] = x;
        data[1] = y;
        dataset.addSeries(metricName, data);

        JFreeChart chart = ChartFactory.createXYLineChart("Learning Curve",
                "number of training folds", metricName, dataset, PlotOrientation.VERTICAL, true,
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

        File pdfPlot = getContext().getFile(id + "/learningCurve_" + metricName + ".pdf",
                AccessMode.READWRITE);
        if(!pdfPlot.getParentFile().exists()) {
        	verifySuccess(pdfPlot.getParentFile().mkdirs(), pdfPlot);
        }
        FileOutputStream fos = new FileOutputStream(pdfPlot);
        ChartUtil.writeChartAsPDF(fos, chart, 400, 400);
        fos.close();

        File txtFile = new File(pdfPlot.getParentFile(), metricName + ".txt");
        FileUtils.writeStringToFile(txtFile, sb.toString(), "utf-8");
    }

    private void verifySuccess(boolean mkdirs, File folder)
    {
        if (!mkdirs && !folder.exists()) {
            throw new IllegalStateException(
                    "Could not create folders [" + folder.getAbsolutePath() + "]");
        }
    }

    private boolean isSingleLabelMode(String learningMode)
    {
        return learningMode.equals(Constants.LM_SINGLE_LABEL);
    }

    private void writeCategoricalResults(Map<LearningCurveRunIdentifier, Map<Integer, List<File>>> dataMap)
        throws Exception
    {
        for(Entry<LearningCurveRunIdentifier, Map<Integer, List<File>>> e : dataMap.entrySet()) {
            List<List<CategoricalPerformance>> stageAvg = averagePerStageCategorical(e.getValue());
            writeCategoricalPlots(e.getKey().md5, stageAvg, maxNumberFolds);
        }

    }

    private void writeCategoricalPlots(String md5, List<List<CategoricalPerformance>> allData,
            int maxValue)
        throws Exception
    {

        List<List<CategoricalPerformance>> allDataNormalized = ensureThatEachCategoryOccursAtEachStage(
                allData);

        for (int i = 0; i < allDataNormalized.get(0).size(); i++) {
            for (int j = 0; j < allData.size(); j++) {

            	StringBuilder sb = new StringBuilder();
            	sb.append(getHeader(allData.size()));
            	
                DefaultXYDataset dataset = new DefaultXYDataset();
                for (String key : new String[] { CategoricalPerformance.PRECISION,
                        CategoricalPerformance.RECALL, CategoricalPerformance.FSCORE }) {
                    double x[] = new double[allData.size() + 1];
                    double y[] = new double[allData.size() + 1];

                    int idx = 1;
                    for (List<CategoricalPerformance> currentStage : allDataNormalized) {
                        x[idx] = idx;
                        y[idx] = currentStage.get(i).getValue(key);
                        idx++;
                    }

                    double[][] data = new double[2][x.length];
                    data[0] = x;
                    data[1] = y;
                    dataset.addSeries(key, data);
                    
					sb.append(String.format("%10s\t", key));
                    for(int k=0; k<y.length; k++) {
						sb.append(String.format("%.4f", y[k]));
						if(k+1 < y.length) {
							sb.append("\t");
						}
                    }
                    sb.append("\n");
                }

                JFreeChart chart = ChartFactory.createXYLineChart(
                        allDataNormalized.get(j).get(i).categoryName + "-CategoricalCurve",
                        "number of training folds", "Performance", dataset,
                        PlotOrientation.VERTICAL, true, false, false);
                XYPlot plot = (XYPlot) chart.getPlot();
                XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
                renderer.setSeriesLinesVisible(0, true);
                renderer.setSeriesShapesVisible(0, false);
                plot.setRenderer(renderer);
                NumberAxis domain = (NumberAxis) plot.getDomainAxis();
                domain.setRange(0.00, maxValue);
                domain.setTickUnit(new NumberTickUnit(1.0));
                NumberAxis range = (NumberAxis) plot.getRangeAxis();
                range.setRange(0.0, 1.0);
                range.setTickUnit(new NumberTickUnit(0.1));

                String fileName =  allDataNormalized.get(j).get(i).categoryName.replaceAll(" ", "_");
                		
				File plotFile = getContext().getFile(md5 + "/categorical/" + fileName + ".pdf",
                        AccessMode.READWRITE);
                if (!plotFile.getParentFile().exists()) {
                    verifySuccess(plotFile.getParentFile().mkdir(), plotFile.getParentFile());
                }
                FileOutputStream fos = new FileOutputStream(plotFile);
                ChartUtil.writeChartAsPDF(fos, chart, 400, 400);
                fos.close();
                
                File textFile = new File(plotFile.getParentFile(), fileName + ".txt");
                FileUtils.writeStringToFile(textFile, sb.toString(), UTF_8);
            }
        }
    }

	private String getHeader(int size) {
		StringBuilder pattern = new StringBuilder("%10s");
		List<Object> vals = new ArrayList<>();
		vals.add("Cat/Stage");
		for (int i = 0; i <= size; i++) {
			vals.add(i);
			pattern.append("\t%6d");
		}
		pattern.append("\n");
		return String.format(Locale.getDefault(), pattern.toString(), vals.toArray());
	}

	/**
     * It might happen that some categories did not occur in the training set depending on the data
     * distribution and how they were assigned to folds. We check this condition and add
     * zero-entries to ensure that on each learning curve stage all categories occur.
     */
    private List<List<CategoricalPerformance>> ensureThatEachCategoryOccursAtEachStage(
            List<List<CategoricalPerformance>> allData)
    {

        Set<String> setOfNames = new HashSet<>();
        for (List<CategoricalPerformance> cp : allData) {
            for (CategoricalPerformance c : cp) {
                setOfNames.add(c.categoryName);
            }
        }
        List<String> catogryNames = new ArrayList<>(setOfNames);
        Collections.sort(catogryNames);

        boolean needNormalization = false;
        for (List<CategoricalPerformance> stage : allData) {
            if (stage.size() != catogryNames.size()) {
                needNormalization = true;
                break;
            }
        }

        if (!needNormalization) {
            return allData;
        }

        List<List<CategoricalPerformance>> allDataNormalized = new ArrayList<>();

        for (List<CategoricalPerformance> stage : allData) {
            List<CategoricalPerformance> stageNormalized = new ArrayList<>();
            for (String name : catogryNames) {
                boolean added = false;
                for (CategoricalPerformance p : stage) {
                    if (p.categoryName.equals(name)) {
                        stageNormalized.add(p);
                        added = true;
                        break;
                    }
                    if (added) {
                        break;
                    }
                }
                if (!added) {
                    stageNormalized.add(new CategoricalPerformance(name, 0, 0, 0));
                }
            }
            allDataNormalized.add(stageNormalized);
        }

        return allDataNormalized;
    }

    private List<List<CategoricalPerformance>> averagePerStageCategorical(
            Map<Integer, List<File>> map)
        throws Exception
    {
        List<List<CategoricalPerformance>> stageAveraged = new ArrayList<>();

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

            Set<String> categories = new HashSet<>();
            stageData.forEach(x -> {
                categories.add(x.getGold());
                categories.add(x.getPredicted());
            });

            Precision<String> precision = new Precision<>(stageData);
            Recall<String> recall = new Recall<>(stageData);
            Fscore<String> fscore = new Fscore<>(stageData);
            List<CategoricalPerformance> cp = new ArrayList<>();
            for (String c : categories) {
                cp.add(new CategoricalPerformance(c, precision.getPrecisionForLabel(c),
                        recall.getRecallForLabel(c), fscore.getScoreForLabel(c)));
            }

            Collections.sort(cp, new Comparator<CategoricalPerformance>()
            {

                @Override
                public int compare(CategoricalPerformance o1, CategoricalPerformance o2)
                {
                    return o1.categoryName.compareTo(o2.categoryName);
                }
            });

            stageAveraged.add(cp);
        }
        return stageAveraged;
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

}
