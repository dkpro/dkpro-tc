package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import static de.tudarmstadt.ukp.dkpro.tc.weka.report.ReportConstants.CORRECT;
import static de.tudarmstadt.ukp.dkpro.tc.weka.report.ReportConstants.FMEASURE;
import static de.tudarmstadt.ukp.dkpro.tc.weka.report.ReportConstants.INCORRECT;
import static de.tudarmstadt.ukp.dkpro.tc.weka.report.ReportConstants.PCT_CORRECT;
import static de.tudarmstadt.ukp.dkpro.tc.weka.report.ReportConstants.PCT_INCORRECT;
import static de.tudarmstadt.ukp.dkpro.tc.weka.report.ReportConstants.PCT_UNCLASSIFIED;
import static de.tudarmstadt.ukp.dkpro.tc.weka.report.ReportConstants.PRECISION;
import static de.tudarmstadt.ukp.dkpro.tc.weka.report.ReportConstants.RECALL;
import static de.tudarmstadt.ukp.dkpro.tc.weka.report.ReportConstants.WGT_FMEASURE;
import static de.tudarmstadt.ukp.dkpro.tc.weka.report.ReportConstants.WGT_PRECISION;
import static de.tudarmstadt.ukp.dkpro.tc.weka.report.ReportConstants.WGT_RECALL;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import meka.core.Result;
import mulan.evaluation.measure.Measure;
import weka.core.Instances;
import weka.core.SerializationHelper;
import de.tudarmstadt.ukp.dkpro.lab.reporting.FlexTable;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.weka.evaluation.MekaEvaluationUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.evaluation.MulanEvaluationWrapper;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.TestTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.ReportUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;

/**
 * Report that computes evaluation results given the classification results.
 */
public class ClassificationReport
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
        File storage = getContext().getStorageLocation(TestTask.TEST_TASK_OUTPUT_KEY,
                AccessMode.READONLY);
        boolean multiLabel = getDiscriminators().get(TestTask.class.getName() + "|learningMode")
                .equals(Constants.LM_MULTI_LABEL);

        Properties props = new Properties();
        // table to hold CM results
        FlexTable<String> cMTable = FlexTable.forClass(String.class);
        cMTable.setSortRows(false);
        // matrix to hold CM results
        double[][] confusionMatrix = null;

        File evaluationFile = new File(storage.getAbsolutePath() + "/"
                + TestTask.EVALUATION_DATA_FILENAME);

        if (multiLabel) {
            // ============= multi-label setup ======================
            Result r = Result.readResultFromFile(evaluationFile.getAbsolutePath());

            File dataFile = new File(storage.getAbsolutePath() + "/"
                    + TestTask.PREDICTIONS_FILENAME);
            Instances data = TaskUtils.getInstances(dataFile, true);
            String[] classNames = new String[data.classIndex()];

            for (int i = 0; i < data.classIndex(); i++) {
                classNames[i] = data.attribute(i).name().split(Constants.CLASS_ATTRIBUTE_PREFIX)[1];
            }

            String threshold = r.getInfo("Threshold");
            double[] t = TaskUtils.getMekaThreshold(threshold, r, data);

            // Mulan Evaluation
            boolean[][] actualsArray = MulanEvaluationWrapper.getBooleanArrayFromList(r.actuals);

            List<Measure> mulanResults = MulanEvaluationWrapper.getMulanEvals(r.predictions,
                    actualsArray, t[0]);

            for (Measure measure : mulanResults) {
                results.put(measure.getName(), measure.getValue());
            }

            // Confusion Matrix
            ReportUtils.updateTempMLConfusionMatrix(r, classNames, actualLabelsList,
                    predictedLabelsList, tempM);

            // Meka Evaluation
            Map<String, Double> mekaResults = MekaEvaluationUtils.calcMLStats(r.allPredictions(),
                    r.allActuals(), t, classNames);
            results.putAll(mekaResults);

            // average PR curve
            double[][] prc = ReportUtils.createPRData(actualsArray, r.predictions);
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

            List<String> classLabels = TaskUtils.getClassLabels(eval.getHeader(), multiLabel);
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
            for (String label : TaskUtils.getClassLabels(eval.getHeader(), multiLabel)) {
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
                    TestTask.TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE)
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
        getContext().storeBinary(TestTask.RESULTS_FILENAME, new PropertiesAdapter(props));
        getContext().storeBinary(ClassificationReport.CONFUSIONMATRIX_KEY, cMTable.getCsvWriter());

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