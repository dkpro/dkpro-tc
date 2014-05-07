package de.tudarmstadt.ukp.dkpro.tc.weka.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mulan.classifier.MultiLabelOutput;
import mulan.evaluation.measure.AveragePrecision;
import mulan.evaluation.measure.Coverage;
import mulan.evaluation.measure.ErrorSetSize;
import mulan.evaluation.measure.ExampleBasedAccuracy;
import mulan.evaluation.measure.ExampleBasedFMeasure;
import mulan.evaluation.measure.ExampleBasedPrecision;
import mulan.evaluation.measure.ExampleBasedRecall;
import mulan.evaluation.measure.HammingLoss;
import mulan.evaluation.measure.IsError;
import mulan.evaluation.measure.MacroAUC;
import mulan.evaluation.measure.MacroFMeasure;
import mulan.evaluation.measure.MacroPrecision;
import mulan.evaluation.measure.MacroRecall;
import mulan.evaluation.measure.MeanAveragePrecision;
import mulan.evaluation.measure.Measure;
import mulan.evaluation.measure.MicroAUC;
import mulan.evaluation.measure.MicroFMeasure;
import mulan.evaluation.measure.MicroPrecision;
import mulan.evaluation.measure.MicroRecall;
import mulan.evaluation.measure.OneError;
import mulan.evaluation.measure.RankingLoss;
import mulan.evaluation.measure.SubsetAccuracy;

/**
 * @author Jinseok Nam
 * @author Johannes Daxenberger
 * 
 */
public class MulanEvaluationWrapper
{

    /**
     * TODO: ??
     * @param predictions
     * @param actuals
     * @param threshold
     * @return
     */
    public static List<Measure> getMulanEvals(ArrayList<double[]> predictions, boolean[][] actuals,
            double threshold)
    {

        MultiLabelOutput pre_prediction = new MultiLabelOutput(predictions.get(0), threshold);
        int numInstances = predictions.size();
        double[] thresholds = new double[numInstances];
        Arrays.fill(thresholds, threshold);
        int numOfLabels = actuals[0].length;

        List<Measure> measures = getMeasures(pre_prediction, numOfLabels, false);
        for (Measure m : measures) {
            m.reset();
        }

        Set<Measure> failed = new HashSet<Measure>();
        for (int instanceIndex = 0; instanceIndex < numInstances; instanceIndex++) {
            MultiLabelOutput prediction = new MultiLabelOutput(predictions.get(instanceIndex),
                    thresholds[instanceIndex]);

            Iterator<Measure> it = measures.iterator();
            while (it.hasNext()) {
                Measure m = it.next();
                if (!failed.contains(m)) {
                    try {
                        try {
                            m.update(prediction, actuals[instanceIndex]);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    catch (Exception ex) {
                        failed.add(m); // mulan ignores a measure completely if there was somewhere
                                       // an error, like division by zero
                    }
                }
            }
        }
        return measures;
    }

    private static List<Measure> getMeasures(MultiLabelOutput prediction, int numOfLabels,
            boolean strict)
    {

        List<Measure> measures = new ArrayList<Measure>();
        if (prediction.hasBipartition()) {
            // add example-based measures
            measures.add(new HammingLoss());
            measures.add(new SubsetAccuracy());
            measures.add(new ExampleBasedPrecision());
            measures.add(new ExampleBasedRecall());
            measures.add(new ExampleBasedFMeasure());
            measures.add(new ExampleBasedAccuracy());
            // measures.add(new ExampleBasedSpecificity(strict));
            // add label-based measures
            measures.add(new MicroPrecision(numOfLabels));
            measures.add(new MicroRecall(numOfLabels));
            measures.add(new MicroFMeasure(numOfLabels));
            // measures.add(new MicroSpecificity(numOfLabels));
            measures.add(new MacroPrecision(numOfLabels));
            measures.add(new MacroRecall(numOfLabels));
            measures.add(new MacroFMeasure(numOfLabels));
            // measures.add(new MacroSpecificity(numOfLabels, strict));
        }
        // add ranking-based measures if applicable
        if (prediction.hasRanking()) {
            // add ranking based measures
            measures.add(new AveragePrecision());
            measures.add(new Coverage());
            measures.add(new OneError());
            measures.add(new IsError());
            measures.add(new ErrorSetSize());
            measures.add(new RankingLoss());
        }
        // add confidence measures if applicable
        if (prediction.hasConfidences()) {
            measures.add(new MeanAveragePrecision(numOfLabels));
            measures.add(new MicroAUC(numOfLabels));
            measures.add(new MacroAUC(numOfLabels));
        }
        return measures;
    }

    /**
     * TODO: ??
     * @param actuals
     * @return
     */
    public static boolean[][] getBooleanArrayFromList(ArrayList<int[]> actuals)
    {
        boolean[][] booleanA = new boolean[actuals.size()][actuals.get(0).length];
        for (int i = 0; i < booleanA.length; i++) {
            for (int j = 0; j < booleanA[0].length; j++) {
                booleanA[i][j] = actuals.get(i)[j] == 1 ? true : false;
            }
        }
        return booleanA;
    }

    /**
     * For individual measures.
     * 
     * @param predictions
     * @param actuals
     * @param thresholds
     * @param m
     * @return
     */
    public static Measure getMulanMeasure(ArrayList<double[]> predictions, boolean[][] actuals,
            double[] thresholds, Measure m)
    {
        m.reset();
        try {
            for (int instanceIndex = 0; instanceIndex < predictions.size(); instanceIndex++) {
                MultiLabelOutput prediction = new MultiLabelOutput(predictions.get(instanceIndex),
                        thresholds[instanceIndex]);

                m.update(prediction, actuals[instanceIndex]);

            }
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return m;
    }
}
