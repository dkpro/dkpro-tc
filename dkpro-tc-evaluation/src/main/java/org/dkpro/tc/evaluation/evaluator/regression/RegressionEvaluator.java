/*******************************************************************************
 * Copyright 2017
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
package org.dkpro.tc.evaluation.evaluator.regression;

import java.util.HashMap;
import java.util.Map;

import org.dkpro.tc.evaluation.Id2Outcome;
import org.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import org.dkpro.tc.evaluation.measures.regression.MeanAbsoluteError;
import org.dkpro.tc.evaluation.measures.regression.PearsonCorrelation;
import org.dkpro.tc.evaluation.measures.regression.RootMeanSquaredError;
import org.dkpro.tc.evaluation.measures.regression.SpearmanCorrelation;

public class RegressionEvaluator
    extends EvaluatorBase
{

    public RegressionEvaluator(Id2Outcome id2Outcome, boolean softEvaluation,
            boolean individualLabelMeasures)
    {
        super(id2Outcome, softEvaluation, individualLabelMeasures);
    }

    @Override
    public Map<String, Double> calculateEvaluationMeasures()
    {
        Map<String, Double> meanAbsoluteError = MeanAbsoluteError.calculate(id2Outcome);
        Map<String, Double> rootMeanSquaredError = RootMeanSquaredError.calculate(id2Outcome);
        Map<String, Double> pearsonCorrelation = PearsonCorrelation.calculate(id2Outcome);
        Map<String, Double> spearmanCorrelation = SpearmanCorrelation.calculate(id2Outcome);

        Map<String, Double> results = new HashMap<>();
        results.putAll(meanAbsoluteError);
        results.putAll(rootMeanSquaredError);
        results.putAll(pearsonCorrelation);
        results.putAll(spearmanCorrelation);
        return results;
    }

    @Override
    public Map<String, Double> calculateMicroEvaluationMeasures()
    {
        // TODO add measures
        return null;
    }

}
