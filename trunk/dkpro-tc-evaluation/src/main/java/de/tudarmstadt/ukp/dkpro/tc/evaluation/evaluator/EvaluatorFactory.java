/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.Id2Outcome;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.multi.MultiEvaluator;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.regression.RegressionEvaluator;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.single.SingleEvaluator;

/**
 * @author Andriy Nadolskyy
 * 
 */
public class EvaluatorFactory
{

    /***
     * 
     * @param file
     *            - file containing data are to be evaluated
     * @param mode
     * @param softEvaluation
     *            Controls how division by zero is treated. Soft: returns 0; Hard: returns NaN
     * @param individualLabelMeasures
     *            Controls calculation of measures for individual labels. individual: returns
     *            measures for each label and composite measures; not individual: returns just
     *            composite measures
     * @throws IOException
     */
    public static EvaluatorBase createEvaluator(File file, String learningMode,
            boolean softEvaluation, boolean individualLabelMeasures)
        throws IOException
    {
        List<String> readData = new LinkedList<String>();

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        List<String> labels = null;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("#labels")) {
                labels = Id2Outcome.getLabels(line);
            }
            else if (!line.startsWith("#")) {
                // line might contain several '=', split at the last one
                int idxMostRightHandEqual = line.lastIndexOf("=");
                String evaluationData = line.substring(idxMostRightHandEqual + 1);
                readData.add(evaluationData);
            }
        }
        br.close();
        if (labels == null) {
            throw new IOException("Wrong file format.");
        }

        Map<String, Integer> class2number = Id2Outcome.classNamesToMapping(labels);

        EvaluatorBase evaluator = null;
        if (learningMode.equals(Constants.LM_SINGLE_LABEL)) {
            evaluator = new SingleEvaluator(class2number, readData, softEvaluation,
                    individualLabelMeasures);
        }
        else if (learningMode.equals(Constants.LM_MULTI_LABEL)) {
            evaluator = new MultiEvaluator(class2number, readData, softEvaluation,
                    individualLabelMeasures);
        }
        else if (learningMode.equals(Constants.LM_REGRESSION)) {
            evaluator = new RegressionEvaluator(class2number, readData, softEvaluation,
                    individualLabelMeasures);
        }
        else {
            throw new IllegalArgumentException("Invalid value for learning mode: " + learningMode);
        }

        return evaluator;
    }
}
