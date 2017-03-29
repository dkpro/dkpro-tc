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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.dkpro.tc.evaluation.Id2Outcome;

import de.tudarmstadt.ukp.dkpro.statistics.correlation.SpearmansRankCorrelation;


public class SpearmanCorrelation
{
	public static Map<String, Double> calculate(Id2Outcome id2Outcome) 
	{
		Map<String, Double> results = new HashMap<String, Double>();
		
        Double[] goldstandard = ArrayUtils.toObject(id2Outcome.getGoldValues());
        Double[] prediction = ArrayUtils.toObject(id2Outcome.getPredictions());
        results.put(SpearmanCorrelation.class.getSimpleName(), SpearmansRankCorrelation.computeCorrelation(Arrays.asList(goldstandard), Arrays.asList(prediction)));
        return results; 	
	}	
}