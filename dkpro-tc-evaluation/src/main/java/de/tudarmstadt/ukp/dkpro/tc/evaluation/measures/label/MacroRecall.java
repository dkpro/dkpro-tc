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
package de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label;

import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.ContingencyTable;


/**
 * @author Andriy Nadolskyy
 * 
 */
public class MacroRecall
{

	public static Map<String, Double> calculate(ContingencyTable cTable, boolean softEvaluation) {
		int numberOfMatrices = cTable.getSize();
		double summedRecall = 0.0;
		Map<String, Double> results = new HashMap<String, Double>();
		
		Double result = 0.0;
		for (int i = 0; i < numberOfMatrices; i++){
			double tp = cTable.getTruePositives(i);
			double fn = cTable.getFalseNegatives(i);
			
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
			result = (Double) summedRecall / numberOfMatrices;
		}
		results.put(MacroRecall.class.getSimpleName(), result);
		return results;
	}

	
	public static Map<String, Double> calculateExtraIndividualLabelMeasures(ContingencyTable cTable,
			boolean softEvaluation, Map<Integer, String> number2class) {
		int numberOfMatrices = cTable.getSize();
		Double[] recall = new Double[numberOfMatrices];
		double summedRecall = 0.0;
		Map<String, Double> results = new HashMap<String, Double>();
		
		boolean computableCombined = true;
		for (int i = 0; i < numberOfMatrices; i++){
			double tp = cTable.getTruePositives(i);
			double fn = cTable.getFalseNegatives(i);
			
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
			combinedRecall = (Double) summedRecall / numberOfMatrices;
		} 
		results.put(MacroRecall.class.getSimpleName(), combinedRecall);
		return results;
	}	
}