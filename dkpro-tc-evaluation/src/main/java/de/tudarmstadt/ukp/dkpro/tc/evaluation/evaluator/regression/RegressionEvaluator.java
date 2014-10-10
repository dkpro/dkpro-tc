/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universit�t Darmstadt
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
package de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.regression;

import java.util.List;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.EvaluatorBase;

/**
 * @author Andriy Nadolskyy
 * 
 */
public class RegressionEvaluator
    extends EvaluatorBase
{

    public RegressionEvaluator(Map<String, Integer> class2number,
            List<String> readData, boolean softEvaluation)
    {
        super(class2number, readData, softEvaluation);
    }

    @Override
    public Map<String, String> calculateEvaluationMeasures()
    {
        // TODO add measures
        return null;
    }

}
