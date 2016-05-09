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
package org.dkpro.tc.evaluation.evaluator.regression;

import java.util.Map;

import org.dkpro.tc.evaluation.Id2Outcome;
import org.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import org.dkpro.tc.evaluation.measures.regression.MeanAbsoluteError;

public class RegressionEvaluator
    extends EvaluatorBase
{

    public RegressionEvaluator(Id2Outcome id2Outcome, boolean softEvaluation,
			boolean individualLabelMeasures) {
		super(id2Outcome, softEvaluation, individualLabelMeasures);	
	}

	@Override
    public Map<String, Double> calculateEvaluationMeasures()
    {

	    Map<String, Double> calculate = MeanAbsoluteError.calculate(id2Outcome);
	    
        return calculate;
    }


    @Override
    public Map<String, Double> calculateMicroEvaluationMeasures()
    {
        // TODO add measures
        return null;
    }

}
