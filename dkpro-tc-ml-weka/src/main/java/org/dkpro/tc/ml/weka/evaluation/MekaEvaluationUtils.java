/**
 * Copyright 2019
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
package org.dkpro.tc.ml.weka.evaluation;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.math.stat.descriptive.moment.Mean;

import meka.core.MLEvalUtils;
import meka.core.MLUtils;
import meka.core.Metrics;
import meka.core.ThresholdUtils;

/**
 * Originally written by Jesse Read. Small adaptions by Johannes Daxenberger.
 */
public class MekaEvaluationUtils
    extends MLEvalUtils
{

    public static final String HAMMING_ACCURACY = "Hemming Accuracy";

    /**
     * Calculates a number of evaluation measures for multi-label classification, without class-wise
     * measures.
     * 
     * @param predictions
     *            predictions by the classifier (ranking)
     * @param goldStandard
     *            gold standard (bipartition)
     * @param t
     *            a threshold to create bipartitions from rankings
     * @return the evaluation statistics
     */
    public static HashMap<String, Double> calcMLStats(double predictions[][], int goldStandard[][],
            double t[])
    {
        int N = goldStandard.length;
        int L = goldStandard[0].length;
        int Ypred[][] = ThresholdUtils.threshold(predictions, t);

        HashMap<String, Double> results = new LinkedHashMap<String, Double>();
        Mean mean = new Mean();

        results.put("Number labels", (double) L);
        results.put("Number examples", (double) N);
        results.put("Zero-one-loss", Metrics.L_ZeroOne(goldStandard, Ypred));
        results.put("Label cardinality predicted", MLUtils.labelCardinality(Ypred));
        results.put("Label cardinality actual", MLUtils.labelCardinality(goldStandard));
        results.put("Average threshold", mean.evaluate(t, 0, t.length)); // average
        results.put("Empy vectors", MLUtils.emptyVectors(Ypred));

        return results;
    }

    /**
     * Calculates a number of evaluation measures for multi-label classification, including
     * class-wise measures.
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
            double t[], String[] classNames)
    {
        HashMap<String, Double> results = calcMLStats(predictions, goldStandard, t);
        int L = goldStandard[0].length;
        int Ypred[][] = ThresholdUtils.threshold(predictions, t);

        // class-wise measures
        for (int j = 0; j < L; j++) {
            results.put(HAMMING_ACCURACY + " [" + classNames[j] + "]",
                    Metrics.P_Hamming(goldStandard, Ypred, j));
            results.put("Precision" + " [" + classNames[j] + "]",
                    Metrics.P_Precision(goldStandard, Ypred, j));
            results.put("Recall" + " [" + classNames[j] + "]",
                    Metrics.P_Recall(goldStandard, Ypred, j));
        }
        return results;
    }
}
