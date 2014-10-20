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
public class MacroPrecision
{

	public static Map<String, Double> calculate(ContingencyTable cTable, boolean softEvaluation) {
		int numberOfMatrices = cTable.getSize();
		double summedPrecision = 0.0;
		Map<String, Double> results = new HashMap<String, Double>();
		
		Double result = 0.0;
		for (int i = 0; i < numberOfMatrices; i++){
			double tp = cTable.getTruePositives(i);
			double fp = cTable.getFalsePositives(i);
			
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
			result = (Double) summedPrecision / numberOfMatrices;
		}
		results.put(MacroPrecision.class.getSimpleName(), result);
		return results;
	}

	
	public static Map<String, Double> calculateExtraIndividualLabelMeasures(ContingencyTable cTable,
			boolean softEvaluation, Map<Integer, String> number2class) {
		int numberOfMatrices = cTable.getSize();
		Double[] precision = new Double[numberOfMatrices];
		double summedPrecision = 0.0;
		Map<String, Double> results = new HashMap<String, Double>();
		
		boolean computableCombined = true;
		for (int i = 0; i < numberOfMatrices; i++){
			double tp = cTable.getTruePositives(i);
			double fp = cTable.getFalsePositives(i);
			
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
			combinedPrecision = (Double) summedPrecision / numberOfMatrices;
		} 
		results.put(MacroPrecision.class.getSimpleName(), combinedPrecision);
		return results;
	}
}