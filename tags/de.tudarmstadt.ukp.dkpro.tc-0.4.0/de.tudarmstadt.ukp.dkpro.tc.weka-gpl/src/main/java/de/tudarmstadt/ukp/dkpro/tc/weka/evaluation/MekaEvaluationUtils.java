package de.tudarmstadt.ukp.dkpro.tc.weka.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

import weka.core.MLEvalUtils;
import weka.core.Utils;

/**
 * Originally written by Jesse Read. Small adaptions by Johannes Daxenberger.
 * 
 * @author Jesse Read (jmr30@cs.waikato.ac.nz)
 * @author Johannes Daxenberger
 * 
 */
public class MekaEvaluationUtils
    extends MLEvalUtils
{

    public static HashMap<String, Double> calcMLStats(ArrayList<double[]> Rankings,
            ArrayList<int[]> Actuals, double t[], String[] classNames)
    {
        double N = Rankings.size();
        int L = Rankings.iterator().next().length;
        int fp = 0, tp = 0, tn = 0, fn = 0;
        int p_sum_total = 0, r_sum_total = 0;
        double log_loss_D = 0.0, log_loss_L = 0.0;
        int set_empty_total = 0, set_inter_total = 0;
        int exact_match = 0, one_error = 0, coverage = 0;
        double accuracy = 0.0, f1_macro_D = 0.0, f1_macro_L = 0.0;
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

            // System.out.println("act"+Arrays.toString(actual));
            // System.out.println("prd"+Arrays.toString(pred));

            // calculate
            int p_sum = 0, r_sum = 0;
            int set_union = 0;
            int set_inter = 0;
            int doc_inter = 0;
            int doc_union = 0;
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
                        set_inter++;
                        set_union++;
                    }
                    // predt 1, real 0
                    else {
                        fp++;
                        o_fp[j]++; // f1 macro (L)
                        hloss_total++;
                        set_union++;
                    }
                }
                else {
                    // predt 0, real 1
                    if (R == 1) {
                        r_sum++;
                        fn++;
                        o_fn[j]++; // f1 macro (L)
                        hloss_total++;
                        set_union++;
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

            set_inter_total += set_inter;

            p_sum_total += p_sum;
            r_sum_total += r_sum;

            if (set_union > 0) {
                accuracy += ((double) set_inter / (double) set_union);
                // System.out.println(""+set_inter+","+set_union);
            }

            if (p_sum <= 0) {
                set_empty_total++;
            }

            // exact match (eval. by example)
            if (set_inter == set_union) {
                exact_match++;
            }

            // f1 macro average by example
            if (p_sum > 0 && r_sum > 0 && set_inter > 0) {
                double prec = (double) set_inter / (double) p_sum;
                double rec = (double) set_inter / (double) r_sum;
                if (prec > 0 || rec > 0) {
                    f1_macro_D += ((2.0 * prec * rec) / (prec + rec));
                }
            }

            // one error: how many times the top ranked label is NOT in the label set
            if (actual[Utils.maxIndex(ranking)] <= 0) {
                one_error++;
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
            results.put("F1 " + classNames[j], fms[j]);
            results.put("Recall " + classNames[j], recs[j]);
            results.put("Precision " + classNames[j], precs[j]);
        }

        double precision = (double) set_inter_total / (double) p_sum_total;
        double recall = (double) set_inter_total / (double) r_sum_total;

        // @ temp
        double a[] = new double[L];
        for (int j = 0; j < L; j++) {
            a[j] = (o_tp[j] + o_tn[j]) / N;
        }
        // System.out.println("Individual accuracies: " + Arrays.toString(a));
        // @ temp

        results.put("N", N);
        results.put("L", (double) L);
        // results.put("Accuracy", (accuracy / N));
        // results.put("H_loss", (hloss_total / (N * L)));
        results.put("Hemming Accuracy", 1.0 - (hloss_total / (N * L)));
        // results.put("Exact_match", (exact_match / N));
        results.put("ZeroOne Loss", 1.0 - (exact_match / N));
        // results.put("LCard_diff" ,Math.abs((((double)p_sum_total/N)-(double)r_sum_total/N)));
        // results.put("Coverage" ,((double)coverage/N));
        // results.put("One_error", (one_error / N));
        results.put("Example-Based Log Loss", (log_loss_D / N));
        results.put("Label-Based Log Loss", (log_loss_L / N));
        // results.put("EmptyAccuracy" ,(accuracy/(N-set_empty_total)));
        // results.put("EmptyMacroF1" ,f1_macro_D/(N-(double)set_empty_total));
        // results.put("Build_time" ,s.vals.get("Build_time"));
        // results.put("Test_time" ,s.vals.get("Test_time"));
        // results.put("Total_time" ,s.vals.get("Build_time") + s.vals.get("Test_time"));
        results.put("TP Rate", (double) tp / (double) (tp + fn));
        results.put("FP Rate", (double) fp / (double) (fp + tn));
        // results.put("Precision", precision);
        // results.put("Recall", recall);
        // results.put("F1_micro", (2.0 * precision * recall) / (precision + recall));
        // results.put("F1_macro_D", (f1_macro_D / N));
        // results.put("F1_macro_L", (Utils.sum(fms) / L));
        results.put("Empty Vectors", set_empty_total / N);
        results.put("Label Cardinality pred.", p_sum_total / N);
        results.put("Label Cardinality real", r_sum_total / N);
        Mean mean = new Mean();
        results.put("Averaged Threshold", mean.evaluate(t, 0, t.length)); // average
        // results.put("AUPRC" ,0.0); // "@see (`Hierarchical Multi-label Classification' by Vens et
        // al, 2008);

        return results;
    }
}
