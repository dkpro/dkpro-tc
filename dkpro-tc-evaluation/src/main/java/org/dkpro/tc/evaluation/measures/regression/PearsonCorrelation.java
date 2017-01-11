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
package org.dkpro.tc.evaluation.measures.regression;

import java.util.HashMap;
import java.util.Map;

import org.dkpro.tc.evaluation.Id2Outcome;


public class PearsonCorrelation
{
	public static Map<String, Double> calculate(Id2Outcome id2Outcome) 
	{
		Map<String, Double> results = new HashMap<String, Double>();
		
        double[] goldstandard = id2Outcome.getGoldValues();
        double[] prediction = id2Outcome.getPredictions();
        results.put(
        		PearsonCorrelation.class.getSimpleName(),
        		de.tudarmstadt.ukp.dkpro.statistics.correlation.PearsonCorrelation.computeCorrelation(goldstandard, prediction)
        );
        return results; 	
	}	
}