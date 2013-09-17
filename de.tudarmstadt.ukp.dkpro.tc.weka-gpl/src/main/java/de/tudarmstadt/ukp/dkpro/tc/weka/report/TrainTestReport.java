package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import mulan.evaluation.measure.Measure;
import weka.core.Instances;
import weka.core.Result;
import weka.core.SerializationHelper;
import de.tudarmstadt.ukp.dkpro.lab.reporting.FlexTable;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.weka.evaluation.MekaEvaluationUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.evaluation.MulanEvaluationWrapper;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.CrossValidationTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.TestTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.ReportUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;

@Deprecated
public class TrainTestReport
    extends ReportBase
{

    private static final String CONFUSIONMATRIX_KEY = "confusionMatrix.csv";
    private static final String PR_CURVE_KEY = "PR_curve.svg";
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
        File storage = getContext().getStorageLocation(TestTask.OUTPUT_KEY, AccessMode.READONLY);

        Properties props = new Properties();
        // table to hold CM results
        FlexTable<String> cMTable = FlexTable.forClass(String.class);
        cMTable.setSortRows(false);
        // matrix to hold CM results
        double[][] confusionMatrix = null;

        File evaluationFile = new File(storage.getAbsolutePath() + "/"
                + TestTask.EVALUATION_DATA_KEY);

        if (TestTask.MULTILABEL) {
            // ============= multi-label setup ======================
            Result r = Result.readResultFromFile(evaluationFile.getAbsolutePath());

            File dataFile = new File(storage.getAbsolutePath() + "/" + TestTask.PREDICTIONS_KEY);
            Instances data = TaskUtils.getInstances(dataFile, true);
            String[] classNames = new String[data.classIndex()];

            for (int i = 0; i < data.classIndex(); i++) {
                classNames[i] = data.attribute(i).name()
                        .split(Constants.CLASS_ATTRIBUTE_PREFIX)[1];
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
            Map<String, Double> mekaResults = MekaEvaluationUtils.calcMLStats(r.predictions,
                    r.actuals, t, classNames);
            results.putAll(mekaResults);

            // average PR curve
            double[][] prc = ReportUtils.createPRData(actualsArray, r.predictions);
            prcData.add(prc);
        }

        // ============= single-label setup ======================
        else {
            weka.classifiers.Evaluation eval = (weka.classifiers.Evaluation) SerializationHelper
                    .read(evaluationFile.getAbsolutePath());
            results.put("correct", eval.correct());
            results.put("incorrect", eval.incorrect());
            results.put("pctCorrect", eval.pctCorrect());
            results.put("pctIncorrect", eval.pctIncorrect());
            results.put("pctUnclassified", eval.pctUnclassified());

            // class-wise recall, precision, f1
            for (String label : TaskUtils.getClassLabels(eval)) {
                results.put(
                        "recall_" + label,
                        eval.recall(eval.getHeader().attribute(eval.getHeader().classIndex())
                                .indexOfValue(label)));
                results.put(
                        "precision_" + label,
                        eval.precision(eval.getHeader().attribute(eval.getHeader().classIndex())
                                .indexOfValue(label)));
                results.put(
                        "fmeasure_" + label,
                        eval.fMeasure(eval.getHeader().attribute(eval.getHeader().classIndex())
                                .indexOfValue(label)));
            }

            // confusion matrix
            for (String label : TaskUtils.getClassLabels(eval)) {
                // in single-label mode, we always have a square matrix
                actualLabelsList.add(label);
                predictedLabelsList.add(label);
            }
            confusionMatrix = eval.confusionMatrix();

        }
        // ================================================

        if (TestTask.MULTILABEL) {
            // store ML confusion matrix
            confusionMatrix = createConfusionMatrix(tempM);
            // create PR curve diagram
            ReportUtils.PrecisionRecallDiagramRenderer renderer = new ReportUtils.PrecisionRecallDiagramRenderer(
                    ReportUtils.createXYDataset(prcData));
            FileOutputStream fos = new FileOutputStream(new File(getContext().getStorageLocation(
                    CrossValidationTask.OUTPUT_KEY, AccessMode.READWRITE)
                    + "/" + PR_CURVE_KEY));
            renderer.write(fos);
        }

        for (String s : results.keySet()) {
            System.out.println(s + " - " + results.get(s));
            props.setProperty(s, results.get(s).toString());
        }

        for (int c = 0; c < confusionMatrix.length; c++) {
            LinkedHashMap<String, String> row = new LinkedHashMap<String, String>();
            for (int r = 0; r < confusionMatrix[0].length; r++) {
                row.put(predictedLabelsList.get(r) + " (pred.)",
                        String.valueOf(confusionMatrix[c][r]));
            }
            cMTable.addRow(actualLabelsList.get(c) + " (act.)", row);
        }

        // Write out properties
        getContext().storeBinary(TestTask.RESULTS_KEY, new PropertiesAdapter(props));
        getContext().storeBinary(TrainTestReport.CONFUSIONMATRIX_KEY, cMTable.getCsvWriter());

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