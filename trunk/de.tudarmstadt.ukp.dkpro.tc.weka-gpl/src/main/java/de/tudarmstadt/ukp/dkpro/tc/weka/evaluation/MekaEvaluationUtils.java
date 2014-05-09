package de.tudarmstadt.ukp.dkpro.tc.weka.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

import weka.core.MLEvalUtils;
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
     * @param Rankings
     *            predictions by the classifier (ranking)
     * @param Actuals
     *            gold standard (bipartition)
     * @param t
     *            a threshold to create bipartitions from rankings
     * @param classNames
     *            the class label names
     * @return various measures, manually defined
     */
    public static HashMap<String, Double> calcMLStats(ArrayList<double[]> Rankings,
            ArrayList<int[]> Actuals, double t[], String[] classNames)
    {
        double N = Rankings.size();
        int L = Rankings.iterator().next().length;
        int fp = 0, tp = 0, tn = 0, fn = 0;
        int p_sum_total = 0, r_sum_total = 0;
        double log_loss_D = 0.0, log_loss_L = 0.0;
        int set_empty_total = 0;
        int exact_match = 0;
        int hloss_total = 0;
        int[] o_tp = new int[L], o_fp = new int[L], o_fn = new int[L], o_tn = new int[L];
        // double average_accuracy_online = 0.0;

        for (int i = 0; i < N; i++) {
            double ranking[] = Rankings.get(i);
            int actual[] = Actuals.get(i);

            int pred[] = new int[actual.length];
            for (int j = 0; j < L; j++) {
                pred[j] = (ranking[j] >= t[j]) ? 1 : 0;
            }

            // calculate
            int p_sum = 0, r_sum = 0;
            for (int j = 0; j < L; j++) {
                int p = pred[j];
                int R = actual[j];
                if (p == 1) {
                    p_sum++;
                    // predt 1, real 1
                    if (R == 1) {
                        r_sum++;
                        tp++;
                        o_tp[j]++; // f1 macro (L)
                    }
                    // predt 1, real 0
                    else {
                        fp++;
                        o_fp[j]++; // f1 macro (L)
                        hloss_total++;
                    }
                }
                else {
                    // predt 0, real 1
                    if (R == 1) {
                        r_sum++;
                        fn++;
                        o_fn[j]++; // f1 macro (L)
                        hloss_total++;
                    }
                    // predt 0, real 0
                    else {
                        tn++;
                        o_tn[j]++; // f1 macro (L)
                    }
                }

                // log losses:
                log_loss_D += calcLogLoss(R, ranking[j], Math.log(N));
                log_loss_L += calcLogLoss(R, ranking[j], Math.log(L));
            }

            p_sum_total += p_sum;
            r_sum_total += r_sum;

            if (p_sum <= 0) {
                set_empty_total++;
            }
        }

        double[] fms = new double[L];
        double[] precs = new double[L];
        double[] recs = new double[L];
        for (int j = 0; j < L; j++) {
            // macro average
            if (o_tp[j] <= 0) {
                fms[j] = 0.0;
                precs[j] = 0.0;
                recs[j] = 0.0;
            }
            else {
                double prec = o_tp[j] / ((double) o_tp[j] + (double) o_fp[j]);
                double recall = o_tp[j] / ((double) o_tp[j] + (double) o_fn[j]);
                fms[j] = 2 * ((prec * recall) / (prec + recall));
                precs[j] = prec;
                recs[j] = recall;
            }
        }

        // class-wise measures
        HashMap<String, Double> results = new LinkedHashMap<String, Double>();
        for (int j = 0; j < L; j++) {
            results.put(FMEASURE + "_" + classNames[j], fms[j]);
            results.put(RECALL + "_" + classNames[j], recs[j]);
            results.put(PRECISION + "_" + classNames[j], precs[j]);
        }

        results.put(NUMBER_EXAMPLES, N);
        results.put(NUMBER_LABELS, (double) L);
        results.put(HEMMING_ACCURACY, 1.0 - (hloss_total / (N * L)));
        results.put(ZERO_ONE_LOSS, 1.0 - (exact_match / N));
        results.put(EXAMPLE_BASED_LOG_LOSS, (log_loss_D / N));
        results.put(LABEL_BASED_LOG_LOSS, (log_loss_L / N));
        results.put(TP_RATE, (double) tp / (double) (tp + fn));
        results.put(FP_RATE, (double) fp / (double) (fp + tn));
        results.put(EMPTY_VECTORS, set_empty_total / N);
        results.put(LABEL_CARDINALITY_PRED, p_sum_total / N);
        results.put(LABEL_CARDINALITY_REAL, r_sum_total / N);
        Mean mean = new Mean();
        results.put(AVERAGE_THRESHOLD, mean.evaluate(t, 0, t.length)); // average

        return results;
    }
}
