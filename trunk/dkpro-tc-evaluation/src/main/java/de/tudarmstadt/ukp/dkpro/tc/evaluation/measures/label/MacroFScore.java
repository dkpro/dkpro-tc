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
// TODO fscore should calculate f_1 as a default but allow to configure other betas
public class MacroFScore
{

	public static Map<String, Double> calculate(ContingencyTable cTable, boolean softEvaluation) {
		int numberOfMatrices = cTable.getSize();
		double summedFScore = 0.0;
		Map<String, Double> results = new HashMap<String, Double>();
		
		Double result = 0.0;
		for (int i = 0; i < numberOfMatrices; i++){
			double tp = cTable.getTruePositives(i);
			double fp = cTable.getFalsePositives(i);
			double fn = cTable.getFalseNegatives(i);
			
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
				summedFScore += (2 * precision * recall) / localSum;
			}
			else if (! softEvaluation) {
				result = Double.NaN;
				break;
			}
		}	
		
		if (result == 0.0){
			result = (Double) summedFScore / numberOfMatrices;
		}
		results.put(MacroFScore.class.getSimpleName(), result);
		return results;
	}

	
	public static Map<String, Double> calculateExtraIndividualLabelMeasures(ContingencyTable cTable,
			boolean softEvaluation, Map<Integer, String> number2class) {
		int numberOfMatrices = cTable.getSize();
		Double[] fScore = new Double[numberOfMatrices];
		double summedFScore = 0.0;
		Map<String, Double> results = new HashMap<String, Double>();
		
		boolean computableCombined = true;
		for (int i = 0; i < numberOfMatrices; i++){
			double tp = cTable.getTruePositives(i);
			double fp = cTable.getFalsePositives(i);
			double fn = cTable.getFalseNegatives(i);
						
			double localSum = 0.0;
			double precision = 0.0;
			double recall = 0.0;
			String key = MacroFScore.class.getSimpleName() + "_" + number2class.get(i);
			if ((localSum = tp + fp) != 0.0) {
				precision = tp / localSum;
			}
			if ((localSum = tp + fn) != 0.0) {
				recall = tp / localSum;
			}
			if ((localSum = precision + recall) != 0.0) {
				fScore[i] = (Double) (2 * precision * recall) / localSum;
				summedFScore += fScore[i];
				results.put(key, fScore[i]);
			}
			else if (! softEvaluation) {
				results.put(key, Double.NaN);
				computableCombined = false;
			}
		}	
		Double combinedFScore = Double.NaN;
		if (computableCombined){
			combinedFScore = (Double) summedFScore / numberOfMatrices;
		} 
		results.put(MacroFScore.class.getSimpleName(), combinedFScore);
		return results;
	}	
}
