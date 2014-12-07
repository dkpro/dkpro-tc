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
package de.tudarmstadt.ukp.dkpro.tc.weka.evaluation;

import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.AVERAGE_THRESHOLD;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.EMPTY_VECTORS;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.FMEASURE;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.LABEL_CARDINALITY_PRED;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.LABEL_CARDINALITY_REAL;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.NUMBER_EXAMPLES;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.NUMBER_LABELS;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.PRECISION;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.RECALL;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.ZERO_ONE_LOSS;

import java.util.HashMap;
import java.util.LinkedHashMap;

import meka.core.MLEvalUtils;
import meka.core.MLUtils;
import meka.core.Metrics;
import meka.core.ThresholdUtils;

import org.apache.commons.math.stat.descriptive.moment.Mean;

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
