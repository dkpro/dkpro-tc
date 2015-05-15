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

import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.SmallContingencyTables;


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
public class MacroPrecision
{

	public static Map<String, Double> calculate(SmallContingencyTables smallContTables, 
			boolean softEvaluation) 
	{
		int numberOfTables = smallContTables.getSize();
		double summedPrecision = 0.0;
		Map<String, Double> results = new HashMap<String, Double>();
		
		Double result = 0.0;
		for (int i = 0; i < numberOfTables; i++){
			double tp = smallContTables.getTruePositives(i);
			double fp = smallContTables.getFalsePositives(i);
			
			double denominator = tp + fp;
			if (denominator != 0.0) {
				double precision = (double) tp / denominator;
				summedPrecision += precision;
			}
			else if (! softEvaluation) {
				result = Double.NaN;
				break;
			}
		}	
		
		if (result == 0.0){
			result = (Double) summedPrecision / numberOfTables;
		}
		results.put(MacroPrecision.class.getSimpleName(), result);
		return results;
	}

	
	public static Map<String, Double> calculateExtraIndividualLabelMeasures(SmallContingencyTables smallContTables,
			boolean softEvaluation, Map<Integer, String> number2class) 
	{
		int numberOfTables = smallContTables.getSize();
		Double[] precision = new Double[numberOfTables];
		double summedPrecision = 0.0;
		Map<String, Double> results = new HashMap<String, Double>();
		
		boolean computableCombined = true;
		for (int i = 0; i < numberOfTables; i++){
			double tp = smallContTables.getTruePositives(i);
			double fp = smallContTables.getFalsePositives(i);
			
			double denominator = tp + fp;
			String key = MacroPrecision.class.getSimpleName() + "_" + number2class.get(i);
			if (denominator != 0.0) {
				precision[i] = (Double) tp / denominator;
				summedPrecision += precision[i];
				results.put(key, precision[i]);
			}
			else if (! softEvaluation) {
				results.put(key, Double.NaN);
				computableCombined = false;
			}
		}	
		Double combinedPrecision = Double.NaN;
		if (computableCombined){
			combinedPrecision = (Double) summedPrecision / numberOfTables;
		} 
		results.put(MacroPrecision.class.getSimpleName(), combinedPrecision);
		return results;
	}
}