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
package de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.example;

import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.tc.evaluation.Id2Outcome;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.SingleOutcome;


/**
 * calculation of the measure is based on:
 * 
 * <pre>
 * <code>
 * {@literal @}article{madjarov2012extensive,
 * title={An extensive experimental comparison of methods for multi-label learning},
 * author={Madjarov, Gjorgji and Kocev, Dragi and Gjorgjevikj, Dejan and D{\v{z}}eroski, Sa{\v{s}}o},
 * journal={Pattern Recognition},
 * volume={45},
 * number={9},
 * pages={3095--3096},
 * year={2012},
 * publisher={Elsevier}
 * }
 * </code>
 * </pre>
 * 
 * @author Andriy Nadolskyy
 * 
 */
public class SubsetAccuracy
{

	/**
	 * 
	 * @param id2Outcome
	 * @return
	 */
	public static Map<String, Double> calculate(Id2Outcome id2Outcome) {
		Double subsetAccuracy = 0.0;
		for (SingleOutcome outcome : id2Outcome.getOutcomes()) {
			if (outcome.isExactMatch()){
				subsetAccuracy += 1;
			}
		}
		Map<String, Double> results = new HashMap<String, Double>();
		int numberOfInstances = id2Outcome.getOutcomes().size();
		results.put(SubsetAccuracy.class.getSimpleName(), subsetAccuracy / numberOfInstances);
		return results;
	}	
	
}