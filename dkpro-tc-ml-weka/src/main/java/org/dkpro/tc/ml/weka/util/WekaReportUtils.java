/**
 * Copyright 2018
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
package org.dkpro.tc.ml.weka.util;

import java.io.IOException;
import java.util.Arrays;

import org.dkpro.tc.ml.weka.evaluation.MulanEvaluationWrapper;

import mulan.evaluation.measure.MicroPrecision;
import mulan.evaluation.measure.MicroRecall;

/**
 * Utility methods needed in reports
 */
public class WekaReportUtils
{

    /*
     * Creates data for average PR curve diagram over a threshold. <br>
     * See:
     * 
     * <pre>
     * <code>
     * {@literal @}article{Vens2008, <br>
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
     * </code>
     * </pre>
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