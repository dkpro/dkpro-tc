package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import mulan.evaluation.measure.Measure;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

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
import de.tudarmstadt.ukp.dkpro.tc.weka.util.ReportUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;

public class CVReport
    extends ReportBase
{

    private static final String CONFUSIONMATRIX_KEY = "confusionMatrix.csv";
    private static final String PR_CURVE_KEY = "PR_curve.svg";
    List<String> actualLabelsList = new ArrayList<String>();
    List<String> predictedLabelsList = new ArrayList<String>();
    // in ML mode, holds a map for building the Label Power Set over all label actuals/predictions
    HashMap<String, Map<String, Integer>> tempM = new HashMap<String, Map<String, Integer>>();
    // holds overall CV results
    Map<String, List<Double>> results = new HashMap<String, List<Double>>();
    // holds PR curve data
    List<double[][]> prcData = new ArrayList<double[][]>();
    // a list of measure to sum up over CV folds instead of averaging
    private static final List<String> excludeFromCVAverage = Arrays.asList(new String[] { "N",
            "incorrect", "correct" });

    @Override
    public void execute()
        throws Exception
    {
        File storage = getContext().getStorageLocation(CrossValidationTask.OUTPUT_KEY,
                AccessMode.READONLY);

        Properties props = new Properties();
        // table to hold CM results
        FlexTable<String> cMTable = FlexTable.forClass(String.class);
        cMTable.setSortRows(false);
        // matrix to hold CM results
        double[][] overallCM = null;

        for (int n = 0; n < CrossValidationTask.FOLDS; n++) {

            File evaluationFile = new File(storage.getAbsolutePath()
                    + "/"
                    + StringUtils.replace(CrossValidationTask.EVALUATION_DATA_KEY, "#",
                            String.valueOf(n)));

            if (CrossValidationTask.MULTILABEL) {
                // ============= multi-label setup ======================
                Result r = Result.readResultFromFile(evaluationFile.getAbsolutePath());

                File dataFile = new File(storage.getAbsolutePath()
                        + "/"
                        + StringUtils.replace(CrossValidationTask.PREDICTIONS_KEY, "#",
                                String.valueOf(n)));
                Instances data = TaskUtils.getInstances(dataFile, true);
                String[] classNames = new String[data.classIndex()];

                for (int i = 0; i < data.classIndex(); i++) {
                    classNames[i] = data.attribute(i).name()
                            .split(Constants.CLASS_ATTRIBUTE_PREFIX)[1];
                }

                String threshold = r.getInfo("Threshold");
                double[] t = TaskUtils.getMekaThreshold(threshold, r, data);

                // Mulan Evaluation
                boolean[][] actualsArray = MulanEvaluationWrapper
                        .getBooleanArrayFromList(r.actuals);
                List<Measure> mulanMeasures = MulanEvaluationWrapper.getMulanEvals(r.predictions,
                        actualsArray, t[0]);
                HashMap<String, Double> mulanResults = new HashMap<String, Double>();
                for (Measure measure : mulanMeasures) {
                    mulanResults.put(measure.getName(), measure.getValue());
                }
                ReportUtils.addToResults(mulanResults, results);

                // Meka Evaluation
                HashMap<String, Double> mekaResults = MekaEvaluationUtils.calcMLStats(
                        r.predictions, r.actuals, t, classNames);
                ReportUtils.addToResults(mekaResults, results);

                // Confusion Matrix
                ReportUtils.updateTempMLConfusionMatrix(r, classNames, actualLabelsList,
                        predictedLabelsList, tempM);

                // average PR curve
                double[][] prc = ReportUtils.createPRData(actualsArray, r.predictions);
                prcData.add(prc);

            }

            // ============= single-label setup ======================
            else {
                weka.classifiers.Evaluation eval = (weka.classifiers.Evaluation) SerializationHelper
                        .read(evaluationFile.getAbsolutePath());
                HashMap<String, Double> m = new HashMap<String, Double>();
                m.put("correct", eval.correct());
                m.put("incorrect", eval.incorrect());

                // class-wise recall, precision, f1
                for (String label : TaskUtils.getClassLabels(eval)) {
                    double precision = eval.precision(eval.getHeader()
                            .attribute(eval.getHeader().classIndex()).indexOfValue(label));
                    double recall = eval.recall(eval.getHeader()
                            .attribute(eval.getHeader().classIndex()).indexOfValue(label));
                    double fmeasure = eval.fMeasure(eval.getHeader()
                            .attribute(eval.getHeader().classIndex()).indexOfValue(label));

                    m.put("precision_" + label, precision);
                    m.put("recall_" + label, recall);
                    m.put("fmeasure_" + label, fmeasure);
                    // System.out.println("precision_" + label + ": " + precision);
                    // System.out.println("recall_" + label + ": " + recall);
                    // System.out.println("fmeasure_" + label + ": " + fmeasure);
                }
                m.put("weighted_fmeasure", eval.weightedFMeasure());
                m.put("unweighted_macro_fmeasure", eval.unweightedMacroFmeasure());
                m.put("unweighted_micro_fmeasure", eval.unweightedMicroFmeasure());

                ReportUtils.addToResults(m, results);

                // confusion matrix
                for (String label : TaskUtils.getClassLabels(eval)) {
                    // in single-label mode, we always have a square matrix
                    actualLabelsList.add(label);
                    predictedLabelsList.add(label);
                }
                double[][] tempCM = eval.confusionMatrix();
                if (overallCM == null) {
                    overallCM = new double[actualLabelsList.size()][predictedLabelsList.size()];
                }
                // add CV fold confusion matrix results to overall confusion matrix
                for (int r = 0; r < tempCM.length; r++) {
                    for (int c = 0; c < tempCM[0].length; c++) {
                        overallCM[r][c] += tempCM[r][c];
                    }
                }
            }
            // ================================================
        }

        if (CrossValidationTask.MULTILABEL) {
            // store ML confusion matrix
            overallCM = ReportUtils.createConfusionMatrix(tempM, actualLabelsList,
                    predictedLabelsList);
            // create PR curve diagram
            ReportUtils.PrecisionRecallDiagramRenderer renderer = new ReportUtils.PrecisionRecallDiagramRenderer(
                    ReportUtils.createXYDataset(prcData));
            FileOutputStream fos = new FileOutputStream(new File(getContext().getStorageLocation(
                    CrossValidationTask.OUTPUT_KEY, AccessMode.READWRITE)
                    + "/" + PR_CURVE_KEY));
            renderer.write(fos);
        }

        for (String s : results.keySet()) {
            DescriptiveStatistics stats = new DescriptiveStatistics(ArrayUtils.toPrimitive(results
                    .get(s).toArray(new Double[] {})));
            String result;
            if (excludeFromCVAverage.contains(s)) {
                double sum = stats.getSum();
                result = Double.toString(sum);
            }
            else {
                double mean = stats.getMean();
                double std = stats.getStandardDeviation();
                result = Double.toString(mean) + "\u00B1" + Double.toString(std);
            }

            props.setProperty(s, result);
        }

        for (int c = 0; c < overallCM.length; c++) {
            LinkedHashMap<String, String> row = new LinkedHashMap<String, String>();
            for (int r = 0; r < overallCM[0].length; r++) {
                row.put(predictedLabelsList.get(r) + " (pred.)", String.valueOf(overallCM[c][r]));
            }
            cMTable.addRow(actualLabelsList.get(c) + " (act.)", row);
        }

        // Write out properties
        getContext().storeBinary(CrossValidationTask.RESULTS_KEY, new PropertiesAdapter(props));
        getContext().storeBinary(CVReport.CONFUSIONMATRIX_KEY, cMTable.getCsvWriter());
    }
}