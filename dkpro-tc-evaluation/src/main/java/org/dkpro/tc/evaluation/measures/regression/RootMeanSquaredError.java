/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.evaluation.measures.regression;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dkpro.tc.evaluation.Id2Outcome;
import org.dkpro.tc.evaluation.SingleOutcome;

public class RootMeanSquaredError
{
    public static Map<String, Double> calculate(Id2Outcome id2Outcome)
    {
        Map<String, Double> results = new HashMap<String, Double>();

        double sum = 0.0;

        Set<SingleOutcome> outcomes = id2Outcome.getOutcomes();
        for (SingleOutcome o : outcomes) {
            double[] goldstandard = o.getGoldstandard();
            double[] prediction = o.getPrediction();
            sum += Math.pow(goldstandard[0] - prediction[0], 2);
        }

        double average = sum / outcomes.size();
        double rmse = Math.sqrt(average);
        results.put(RootMeanSquaredError.class.getSimpleName(), rmse);
        return results;
    }
}