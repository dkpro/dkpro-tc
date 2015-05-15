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
public class MacroRecall
{

	public static Map<String, Double> calculate(SmallContingencyTables smallContTables, 
			boolean softEvaluation) 
	{
		int numberOfTables = smallContTables.getSize();
		double summedRecall = 0.0;
		Map<String, Double> results = new HashMap<String, Double>();
		
		Double result = 0.0;
		for (int i = 0; i < numberOfTables; i++){
			double tp = smallContTables.getTruePositives(i);
			double fn = smallContTables.getFalseNegatives(i);
			
			double denominator = tp + fn;
			if (denominator != 0.0) {
				double recall = (double) tp / denominator;
				summedRecall += recall;
			}
			else if (! softEvaluation) {
				result = Double.NaN;
				break;
			}
		}	
		
		if (result == 0.0){
			result = (Double) summedRecall / numberOfTables;
		}
		results.put(MacroRecall.class.getSimpleName(), result);
		return results;
	}

	
	public static Map<String, Double> calculateExtraIndividualLabelMeasures(SmallContingencyTables smallContTables,
			boolean softEvaluation, Map<Integer, String> number2class) 
	{
		int numberOfTables = smallContTables.getSize();
		Double[] recall = new Double[numberOfTables];
		double summedRecall = 0.0;
		Map<String, Double> results = new HashMap<String, Double>();
		
		boolean computableCombined = true;
		for (int i = 0; i < numberOfTables; i++){
			double tp = smallContTables.getTruePositives(i);
			double fn = smallContTables.getFalseNegatives(i);
			
			double denominator = tp + fn;
			String key = MacroRecall.class.getSimpleName() + "_" + number2class.get(i);
			if (denominator != 0.0) {
				recall[i] = (Double) tp / denominator;
				summedRecall += recall[i];
				results.put(key, recall[i]);
			}
			else if (! softEvaluation) {
				results.put(key, Double.NaN);
				computableCombined = false;
			}
		}	
		Double combinedRecall = Double.NaN;
		if (computableCombined){
			combinedRecall = (Double) summedRecall / numberOfTables;
		} 
		results.put(MacroRecall.class.getSimpleName(), combinedRecall);
		return results;
	}	
}