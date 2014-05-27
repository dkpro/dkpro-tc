package de.tudarmstadt.ukp.dkpro.tc.weka.evaluation;

import java.util.HashMap;
import java.util.LinkedHashMap;

import meka.core.MLEvalUtils;
import meka.core.MLUtils;
import meka.core.Metrics;
import meka.core.ThresholdUtils;

import org.apache.commons.math.stat.descriptive.moment.Mean;

import de.tudarmstadt.ukp.dkpro.tc.weka.report.ReportConstants;

/**
 * Originally written by Jesse Read. Small adaptions by Johannes Daxenberger.
 * 
 * @author Jesse Read (jmr30@cs.waikato.ac.nz)
 * @author Johannes Daxenberger
 * 
 */
public class MekaEvaluationUtils
    extends MLEvalUtils
    implements ReportConstants
{

    /**
     * Calculates a number of evaluation measures for multi-label classification.
     * 
     * @param predictions
     *            predictions by the classifier (ranking)
     * @param goldStandard
     *            gold standard (bipartition)
     * @param t
     *            a threshold to create bipartitions from rankings
     * @param classNames
     *            the class label names
     * @return the evaluation statistics
     */
    public static HashMap<String, Double> calcMLStats(double predictions[][], int goldStandard[][],
            double t[],
            String[] classNames)
    {

        int N = goldStandard.length;
        int L = goldStandard[0].length;
        int Ypred[][] = ThresholdUtils.threshold(predictions, t);

        HashMap<String, Double> results = new LinkedHashMap<String, Double>();
        Mean mean = new Mean();

        results.put(NUMBER_LABELS, (double) L);
        results.put(NUMBER_EXAMPLES, (double) N);
        results.put(ZERO_ONE_LOSS, Metrics.L_ZeroOne(goldStandard, Ypred));
        results.put(LABEL_CARDINALITY_PRED, MLUtils.labelCardinality(Ypred));
        results.put(LABEL_CARDINALITY_REAL, MLUtils.labelCardinality(goldStandard));
        results.put(AVERAGE_THRESHOLD, mean.evaluate(t, 0, t.length)); // average
        results.put(EMPTY_VECTORS, MLUtils.emptyVectors(Ypred));

        // class-wise measures
        for (int j = 0; j < L; j++) {
            results.put(FMEASURE + " [" + classNames[j] + "]",
                    Metrics.P_FmicroAvg(goldStandard, Ypred));
            results.put(PRECISION + " [" + classNames[j] + "]",
                    Metrics.P_Precision(goldStandard, Ypred, j));
            results.put(RECALL + " [" + classNames[j] + "]",
                    Metrics.P_Recall(goldStandard, Ypred, j));
        }
        return results;
    }
}
