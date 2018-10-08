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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.ChartUtil;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.Task;
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

/**
 * Collects the final evaluation results in a cross validation setting.
 * 
 */
public class LearningCurveReport
    extends TcAbstractReport
    implements Constants
{
    private Map<String, String> taskMapping = new HashMap<>();
    private int maxId = 1;

    private static final String baselineFolder = "baselineResults";
    private static final String SEP = "\t";
    private static final String FILE_ENDING = ".tsv";

    private int numFolds = Integer.MIN_VALUE;
    
    
    public LearningCurveReport(int numFolds)
    {
        this.numFolds = numFolds;
    }

    @Override
    public void execute() throws Exception
    {

        StorageService store = getContext().getStorageService();
        Set<String> idPool = getTaskIdsFromMetaData(getSubtasks());
        String learningMode = determineLearningMode(store, idPool);

        writeId2DiscriminatorMapping(store, learningMode, idPool);

        writeOverallResults(learningMode, store, idPool);
        
        if(isSingleLabelMode(learningMode)) {
        	writeCategoricalResults(learningMode, store, idPool);
        }

    }
    
    // FIXME: Only a single ML run supported at the moment no multiple feature set or multiple classifier runs!

    private void writeOverallResults(String learningMode, StorageService store, Set<String> idPool)
        throws Exception
    {

        List<List<File>> stageId2outcomeFiles = new ArrayList<>();

        Set<String> collectSubtasks = null;
        for (String id : idPool) {

            if (!TcTaskTypeUtil.isCrossValidationTask(store, id)) {
                continue;
            }
            collectSubtasks = collectSubtasks(id);
            break;
        }

        List<File> stage = new ArrayList<>();
        List<String> sortedTasks = new ArrayList<>(collectSubtasks);
        Collections.sort(sortedTasks);
        for (String sId : sortedTasks) {
            if (!TcTaskTypeUtil.isMachineLearningAdapterTask(store, sId)) {
                continue;
            }
            File f = store.locateKey(sId, ID_OUTCOME_KEY);
            stage.add(f);
            
//            Map<String, String> values = getDiscriminatorsOfMlaSetup(sId);
//            values = ReportUtils.removeKeyRedundancy(values);
//            String string = values.get(Constants.DIM_NUM_TRAINING_FOLDS);

            //the number of folds decide how many MLA runs belong to the same stage
            if (stage.size() % numFolds == 0) {
                stageId2outcomeFiles.add(stage);
                stage = new ArrayList<>();
            }
        }

        List<Double> stageAveraged = new ArrayList<>();
        if (learningMode.equals(LM_SINGLE_LABEL)) {
            for (List<File> st : stageId2outcomeFiles) {
                EvaluationData<String> stageData = new EvaluationData<>();
                for (File f : st) {
                    EvaluationData<String> run = Tc2LtlabEvalConverter
                            .convertSingleLabelModeId2Outcome(f);
                    stageData.registerBulk(run);
                }
                Accuracy<String> acc = new Accuracy<>(stageData);
                stageAveraged.add(acc.getResult());
            }
            
            writePlot(stageAveraged);
            
            
                
            stageAveraged.forEach(v -> System.out.println(v));
        }

    }

    private void writePlot(List<Double> stageAveraged) throws Exception
    {
        double x [] = new double[stageAveraged.size()+1];
        double y [] = new double[stageAveraged.size()+1];
        for(int i=1; i<x.length; i++) {
            x[i]=i;
            y[i]=stageAveraged.get(i-1);
        }
        
        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] data = new double[2][x.length];
        data[0] = x;
        data[1] = y;
        dataset.addSeries("LearningCurve", data);
        
        
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Learning Curve",
                "number of training folds",
                "performance",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false
            );
            XYPlot plot = (XYPlot) chart.getPlot();
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setSeriesLinesVisible(0, true);
            renderer.setSeriesShapesVisible(0, false);
            plot.setRenderer(renderer);
            NumberAxis domain = (NumberAxis) plot.getDomainAxis();
            domain.setRange(0.00, numFolds);
            domain.setTickUnit(new NumberTickUnit(1.0));
            NumberAxis range = (NumberAxis) plot.getRangeAxis();
            range.setRange(0.0, 1.0);
            range.setTickUnit(new NumberTickUnit(0.1));
        
            File file = getContext().getFile("learningCurve.pdf", AccessMode.READWRITE);
            FileOutputStream fos = new FileOutputStream(file);
            ChartUtil.writeChartAsPDF(fos, chart, 400, 400);
            fos.close();        
    }

    private String getMLSetup(String id) throws Exception
    {

        Map<String, String> discriminatorsMap = getDiscriminatorsOfMlaSetup(id);
        discriminatorsMap = ReportUtils.removeKeyRedundancy(discriminatorsMap);

        String args = discriminatorsMap.get(Constants.DIM_CLASSIFICATION_ARGS);

        if (args == null || args.isEmpty()) {
            return "";
        }

        args = args.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "_");

        return args + "_";
    }

    private void writeId2DiscriminatorMapping(StorageService store, String learningMode,
            Set<String> idPool)
        throws Exception
    {
        StringBuilder sb = new StringBuilder();

        for (String id : idPool) {

            if (!TcTaskTypeUtil.isCrossValidationTask(store, id)) {
                continue;
            }

            Map<String, String> values = getDiscriminatorsOfMlaSetup(id);
            values.putAll(getDiscriminatorsForContext(store, id, Task.DISCRIMINATORS_KEY));
            values = ReportUtils.removeKeyRedundancy(values);
            values = ReportUtils.replaceKeyWithConstant(values, DIM_FILES_ROOT, "<OMITTED>");
            values = ReportUtils.replaceKeyWithConstant(values, DIM_FILES_TRAINING, "<OMITTED>");
            values = ReportUtils.replaceKeyWithConstant(values, DIM_FILES_VALIDATION, "<OMITTED>");

            // add keys and values sorted by keys
            List<String> mapKeys = new ArrayList<String>(values.keySet());
            Collections.sort(mapKeys);
            sb.append(registerGetMapping(id) + SEP + getContextLabel(id));
            for (String k : mapKeys) {
                sb.append(SEP + k + "=" + values.get(k));
            }
            sb.append("\n");

            Set<String> collectSubtasks = collectSubtasks(id);
            for (String sid : collectSubtasks) {
                if (!TcTaskTypeUtil.isMachineLearningAdapterTask(store, sid)) {
                    continue;
                }
                values = getDiscriminatorsOfMlaSetup(sid);
                values.putAll(getDiscriminatorsForContext(store, sid, Task.DISCRIMINATORS_KEY));
                values = ReportUtils.removeKeyRedundancy(values);
                values = ReportUtils.replaceKeyWithConstant(values, DIM_FILES_ROOT, "<OMITTED>");
                values = ReportUtils.replaceKeyWithConstant(values, DIM_FILES_TRAINING,
                        "<OMITTED>");
                values = ReportUtils.replaceKeyWithConstant(values, DIM_FILES_VALIDATION,
                        "<OMITTED>");

                mapKeys = new ArrayList<String>(values.keySet());
                Collections.sort(mapKeys);

                sb.append(registerGetMapping(sid) + SEP + getContextLabel(sid));
                for (String k : mapKeys) {
                    sb.append(SEP + k + "=" + values.get(k));
                }
                sb.append("\n");
            }
        }

        FileUtils.writeStringToFile(
                getContext().getFile(FILE_CONFIGURATION_MAPPING, AccessMode.READWRITE),
                sb.toString(), "utf-8");

    }

    private String registerGetMapping(String id)
    {

        String value = taskMapping.get(id);
        if (value == null) {
            value = maxId < 100 ? (maxId < 10 ? "00" + maxId : "0" + maxId) : "" + maxId;
            taskMapping.put(id, value);
            maxId++;
        }

        return value;
    }

    private Map<String, String> getDiscriminatorsOfMlaSetup(String id) throws Exception
    {
        Map<String, String> discriminatorsMap = new HashMap<>();

        // get the details of the configuration from a MLA - any will do
        Set<String> collectSubtasks = collectSubtasks(id);
        for (String subid : collectSubtasks) {
            if (TcTaskTypeUtil.isMachineLearningAdapterTask(getContext().getStorageService(),
                    subid)) {
                discriminatorsMap.putAll(getDiscriminatorsForContext(
                        getContext().getStorageService(), subid, Task.DISCRIMINATORS_KEY));
                break;
            }
        }
        return discriminatorsMap;
    }

    private boolean isSingleLabelMode(String learningMode)
    {
        return learningMode.equals(Constants.LM_SINGLE_LABEL);
    }

    private void writeCategoricalResults(String learningMode, StorageService store, Set<String> idPool)
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

    private static Map<String, String> getDiscriminatorsForContext(StorageService store,
            String contextId, String discriminatorsKey)
    {
        return store.retrieveBinary(contextId, discriminatorsKey, new PropertiesAdapter()).getMap();
    }

}
