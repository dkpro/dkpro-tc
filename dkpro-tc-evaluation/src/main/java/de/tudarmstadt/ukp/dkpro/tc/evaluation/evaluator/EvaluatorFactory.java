/*******************************************************************************
 * Copyright 2015
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

import java.io.File;
import java.io.IOException;

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
        Id2Outcome id2outcome = new Id2Outcome(file, learningMode);
        return createEvaluator(id2outcome, softEvaluation, individualLabelMeasures);
       
    }
    
    /***
     * 
     * @param id2outcome
     * @param mode
     * @param softEvaluation
     *            Controls how division by zero is treated. Soft: returns 0; Hard: returns NaN
     * @param individualLabelMeasures
     *            Controls calculation of measures for individual labels. individual: returns
     *            measures for each label and composite measures; not individual: returns just
     *            composite measures
     * @throws IOException
     */
    public static EvaluatorBase createEvaluator(Id2Outcome id2outcome,
            boolean softEvaluation, boolean individualLabelMeasures)
        throws IOException
    {
    	EvaluatorBase evaluator = null;
        
        if (id2outcome.getLearningMode().equals(Constants.LM_SINGLE_LABEL)) {
            evaluator = new SingleEvaluator(id2outcome, softEvaluation,
                    individualLabelMeasures);
        }
        else if (id2outcome.getLearningMode().equals(Constants.LM_MULTI_LABEL)) {
            evaluator = new MultiEvaluator(id2outcome,softEvaluation,
                    individualLabelMeasures);
        }
        else if (id2outcome.getLearningMode().equals(Constants.LM_REGRESSION)) {
            evaluator = new RegressionEvaluator(id2outcome, softEvaluation,
                    individualLabelMeasures);
        }
        else {
            throw new IllegalArgumentException("Invalid value for learning mode: " + id2outcome.getLearningMode());
        }

        return evaluator;
    }
}
