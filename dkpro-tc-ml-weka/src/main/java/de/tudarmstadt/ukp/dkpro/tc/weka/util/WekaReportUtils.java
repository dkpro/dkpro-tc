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
package de.tudarmstadt.ukp.dkpro.tc.weka.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import meka.core.Result;
import mulan.evaluation.measure.MicroPrecision;
import mulan.evaluation.measure.MicroRecall;
import de.tudarmstadt.ukp.dkpro.tc.core.util.ReportUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.evaluation.MulanEvaluationWrapper;

/**
 * Utility methods needed in reports
 * 
 * @author Johannes Daxenberger
 * 
 */
public class WekaReportUtils
{
    /**
     * Adds results from one fold to the overall CV confusion matrix. Also updates actual and
     * predicted label lists for the label powerset transformation
     * 
     * @param numInstances
     * @param r
     * @param classNames
     * @param actualLabelsList
     *            not null
     * @param predictedLabelsList
     *            not null
     * @param tempM
     *            not null
     */
    public static void updateTempMLConfusionMatrix(MultilabelResult r, String[] classNames,
            List<String> actualLabelsList, List<String> predictedLabelsList,
            HashMap<String, Map<String, Integer>> tempM)
    {
        for (int i = 0; i < r.getGoldstandard().length; i++) {
            int[] prediction = r.getPredictionsBipartition()[i];
            int[] actual = r.getGoldstandard()[i];

            // in ML mode, we build the confusion matrix over the Label Power Set of all
            // actuals/predictions
            String predString = ReportUtils.doubleArrayToClassNames(prediction, classNames, ';');
            String actString = ReportUtils.doubleArrayToClassNames(actual, classNames, ';');

            if (!predictedLabelsList.contains(predString)) {
                predictedLabelsList.add(predString);
            }
            if (tempM.get(actString) != null) {
                if (tempM.get(actString).get(predString) != null) {
                    tempM.get(actString).put(predString, tempM.get(actString).get(predString) + 1);
                }
                else {
                    tempM.get(actString).put(predString, 1);
                }
            }
            else {
                HashMap<String, Integer> h = new HashMap<String, Integer>();
                h.put(predString, 1);
                tempM.put(actString, h);
                actualLabelsList.add(actString);
            }
        }
    }

    /**
     * Creates data for average PR curve diagram over a threshold. <br>
     * See: <br>
     * article{Vens2008, <br>
     * author = {Vens, Celine and Struyf, Jan and Schietgat, Leander and D\v{z}eroski, Sa\v{s}o and
     * Blockeel, Hendrik}, <br>
     * title = {Decision trees for hierarchical multi-label classification}, <br>
     * journal = {Mach. Learn.}, <br>
     * issue_date = {November 2008}, <br>
     * volume = {73}, <br>
     * number = {2}, <br>
     * month = nov, <br>
     * year = {2008},<br>
     * pages = {185--214} <br>
     * }
     * 
     * @param r
     * @return
     * @throws IOException
     */
    public static double[][] createPRData(boolean[][] actualsArray, double[][] predictions)
        throws IOException
    {
        double[][] data = new double[2][11];
        double t = 0;

        for (int j = 0; j <= 10; j++) {
            double[] thresholds = new double[predictions.length];
            Arrays.fill(thresholds, t / 10);

            double precision = MulanEvaluationWrapper.getMulanMeasure(predictions, actualsArray,
                    thresholds, new MicroPrecision(actualsArray[0].length)).getValue();
            double recall = MulanEvaluationWrapper.getMulanMeasure(predictions, actualsArray,
                    thresholds, new MicroRecall(actualsArray[0].length)).getValue();
            data[0][j] = recall;
            data[1][j] = precision;

            t += 1;
        }
        return data;
    }
}