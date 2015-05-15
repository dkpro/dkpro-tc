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
package de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label;

import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.CombinedSmallContingencyTable;


/**
 * calculation of the measure is based on:
 * @article{madjarov2012extensive,
 * title={An extensive experimental comparison of methods for multi-label learning},
 * author={Madjarov, Gjorgji and Kocev, Dragi and Gjorgjevikj, Dejan and D{\v{z}}eroski, Sa{\v{s}}o},
 * journal={Pattern Recognition},
 * volume={45},
 * number={9},
 * pages={3095--3096},
 * year={2012},
 * publisher={Elsevier}
 * }
 * 
 * creating of contingency tables is based on:
 * @article{van2013macro,
 * title={Macro-and micro-averaged evaluation measures [[BASIC DRAFT]]},
 * author={Van Asch, Vincent},
 * pages = {11--12},
 * year={2013},
 * url = {http://www.cnts.ua.ac.be/~vincent/pdf/microaverage.pdf}
 * }
 * EMPTY_PREDICTION is not defined as a valid label. 
 * 
 * 
 * @author Andriy Nadolskyy
 */
public class MicroFScore
{

	public static Map<String, Double> calculate(CombinedSmallContingencyTable cSCTable, 
			boolean softEvaluation) 
	{
		double tp = cSCTable.getTruePositives();
		double fp = cSCTable.getFalsePositives();
		double fn = cSCTable.getFalseNegatives();
		
		Double fScore = 0.0;
		double localSum = 0.0;
		double precision = 0.0;
		double recall = 0.0;
		if ((localSum = tp + fp) != 0.0) {
			precision = tp / localSum;
		}
		if ((localSum = tp + fn) != 0.0) {
			recall = tp / localSum;
		}
		if ((localSum = precision + recall) != 0.0) {
			fScore += (Double) (2 * precision * recall) / localSum;
		}
		else if (! softEvaluation) {
			fScore = Double.NaN;
		}		
		
		Map<String, Double> results = new HashMap<String, Double>();
		results.put(MicroFScore.class.getSimpleName(), fScore);
		return results;	
	}	
}