/**
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.CORRECT;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.FMEASURE;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.INCORRECT;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.PCT_CORRECT;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.PCT_INCORRECT;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.PCT_UNCLASSIFIED;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.PRECISION;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.RECALL;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.WGT_FMEASURE;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.WGT_PRECISION;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.WGT_RECALL;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import mulan.evaluation.measure.Measure;
import weka.core.Instances;
import weka.core.SerializationHelper;
import de.tudarmstadt.ukp.dkpro.lab.reporting.FlexTable;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.core.util.ReportUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.evaluation.MekaEvaluationUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.evaluation.MulanEvaluationWrapper;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.WekaTestTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.MultilabelResult;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaReportUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

/**
 * Report that computes evaluation results given the classification results.
 */
public class WekaClassificationReport
    extends ReportBase
    implements Constants
{

    List<String> actualLabelsList = new ArrayList<String>();
    List<String> predictedLabelsList = new ArrayList<String>();
    // in ML mode, holds a map for building the Label Power Set over all label actuals/predictions
    HashMap<String, Map<String, Integer>> tempM = new HashMap<String, Map<String, Integer>>();
    // holds overall CV results
    Map<String, Double> results = new HashMap<String, Double>();
    // holds PR curve data
    List<double[][]> prcData = new ArrayList<double[][]>();

    @Override
    public void execute()
        throws Exception
    {
        File storage = getContext().getStorageLocation(WekaTestTask.TEST_TASK_OUTPUT_KEY,
                AccessMode.READONLY);
        boolean multiLabel = getDiscriminators().get(WekaTestTask.class.getName() + "|learningMode")
                .equals(Constants.LM_MULTI_LABEL);

        Properties props = new Properties();
        // table to hold CM results
        FlexTable<String> cMTable = FlexTable.forClass(String.class);
        cMTable.setSortRows(false);
        // matrix to hold CM results
        double[][] confusionMatrix = null;

        File evaluationFile = new File(storage.getAbsolutePath() + "/"
                + WekaClassificationAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.evaluationFile));

        if (multiLabel) {
            // ============= multi-label setup ======================
        	MultilabelResult r = WekaUtils.readMlResultFromFile(evaluationFile); 

            File dataFile = new File(storage.getAbsolutePath() + "/"
                    + WekaClassificationAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.predictionsFile));
            Instances data = WekaUtils.getInstances(dataFile, true);
            String[] classNames = new String[data.classIndex()];

            for (int i = 0; i < data.classIndex(); i++) {
                classNames[i] = data.attribute(i).name().split(Constants.CLASS_ATTRIBUTE_PREFIX)[1];
            }

            double t = r.getBipartitionThreshold();
            double[] thresholdArray = new double[classNames.length];
            Arrays.fill(thresholdArray, t);

            // Mulan Evaluation
            boolean[][] actualsArray = MulanEvaluationWrapper.getBooleanMatrix(r.getGoldstandard());

            List<Measure> mulanResults = MulanEvaluationWrapper.getMulanEvals(r.getPredictions(),
                    actualsArray, t);

            for (Measure measure : mulanResults) {
                results.put(measure.getName(), measure.getValue());
            }

            // Confusion Matrix
            WekaReportUtils.updateTempMLConfusionMatrix(r, classNames, actualLabelsList,
                    predictedLabelsList, tempM);

            // Meka Evaluation
            Map<String, Double> mekaResults = MekaEvaluationUtils.calcMLStats(r.getPredictions(),
                    r.getGoldstandard(), thresholdArray, classNames);
            results.putAll(mekaResults);

            // average PR curve
            double[][] prc = WekaReportUtils.createPRData(actualsArray, r.getPredictions());
            prcData.add(prc);
        }

        // ============= single-label setup ======================
        else {
            weka.classifiers.Evaluation eval = (weka.classifiers.Evaluation) SerializationHelper
                    .read(evaluationFile.getAbsolutePath());

            results.put(CORRECT, eval.correct());
            results.put(INCORRECT, eval.incorrect());
            results.put(PCT_CORRECT, eval.pctCorrect());
            results.put(PCT_INCORRECT, eval.pctIncorrect());
            results.put(PCT_UNCLASSIFIED, eval.pctUnclassified());
            results.put(WGT_FMEASURE, eval.weightedFMeasure());
            results.put(WGT_PRECISION, eval.weightedPrecision());
            results.put(WGT_RECALL, eval.weightedRecall());

            List<String> classLabels = WekaUtils.getClassLabels(eval.getHeader(), multiLabel);
            // class-wise recall, precision, f1
            for (String label : classLabels) {
                double recall = eval.recall(eval.getHeader()
                        .attribute(eval.getHeader().classIndex()).indexOfValue(label));
                double precision = eval.precision(eval.getHeader()
                        .attribute(eval.getHeader().classIndex()).indexOfValue(label));
                double fmeasure = eval.fMeasure(eval.getHeader()
                        .attribute(eval.getHeader().classIndex()).indexOfValue(label));
                results.put(RECALL + "_" + label, recall);
                results.put(PRECISION + "_" + label, precision);
                results.put(FMEASURE + "_" + label, fmeasure);
            }

            // confusion matrix
            for (String label : WekaUtils.getClassLabels(eval.getHeader(), multiLabel)) {
                // in single-label mode, we always have a square matrix
                actualLabelsList.add(label);
                predictedLabelsList.add(label);
            }
            confusionMatrix = eval.confusionMatrix();

        }
        // ================================================

        if (multiLabel) {
            // store ML confusion matrix
            confusionMatrix = createConfusionMatrix(tempM);
            // create PR curve diagram
            ReportUtils.PrecisionRecallDiagramRenderer renderer = new ReportUtils.PrecisionRecallDiagramRenderer(
                    ReportUtils.createXYDataset(prcData));
            FileOutputStream fos = new FileOutputStream(new File(getContext().getStorageLocation(
                    WekaTestTask.TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE)
                    + "/" + PR_CURVE_KEY));
            renderer.write(fos);
        }

        for (String s : results.keySet()) {
            getContext().getLoggingService().message(getContextLabel(), s + " - " + results.get(s));
            props.setProperty(s, results.get(s).toString());
        }

        for (int c = 0; c < confusionMatrix.length; c++) {
            LinkedHashMap<String, String> row = new LinkedHashMap<String, String>();
            for (int r = 0; r < confusionMatrix[0].length; r++) {
                row.put(predictedLabelsList.get(r) + CM_PREDICTED,
                        String.valueOf(confusionMatrix[c][r]));
            }
            cMTable.addRow(actualLabelsList.get(c) + CM_ACTUAL, row);
        }

        // Write out properties
        getContext().storeBinary(WekaTestTask.RESULTS_FILENAME, new PropertiesAdapter(props));
        getContext().storeBinary(WekaClassificationReport.CONFUSIONMATRIX_KEY, cMTable.getCsvWriter());

    }

    private double[][] createConfusionMatrix(HashMap<String, Map<String, Integer>> tempM)
    {
        double[][] matrix = new double[actualLabelsList.size()][predictedLabelsList.size()];

        Iterator<String> actualsIter = tempM.keySet().iterator();
        while (actualsIter.hasNext()) {
            String actual = actualsIter.next();
            Iterator<String> predsIter = tempM.get(actual).keySet().iterator();
            while (predsIter.hasNext()) {
                String pred = predsIter.next();
                int a = actualLabelsList.indexOf(actual);
                int p = predictedLabelsList.indexOf(pred);
                matrix[a][p] = tempM.get(actual).get(pred);
            }
        }
        return matrix;
    }
}