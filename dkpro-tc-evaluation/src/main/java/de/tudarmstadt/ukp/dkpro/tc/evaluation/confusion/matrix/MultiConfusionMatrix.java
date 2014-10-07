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
package de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix;

import java.util.HashMap;


/**
 * @author Andriy Nadolskyy
 * 
 */
public class MultiConfusionMatrix extends ConfusionMatrix<HashMap<String, HashMap<String, Double>>>{

	public MultiConfusionMatrix(
			HashMap<String, HashMap<String, Double>> matrix,
			HashMap<String, Integer> class2number) {
		super(matrix, class2number);
	}

	@Override
	public double[][][] decomposeConfusionMatrix() {
		int numberOfClasses = class2number.size(); 
		double[][][] decomposedConfusionMatrix = new double[numberOfClasses][2][2];
		
		for (int decomposed = 0; decomposed < numberOfClasses; decomposed++){
			for (String goldKey : matrix.keySet()) {
				
				String[] goldLabels = goldKey.split(",");
				for (String goldLabel : goldLabels) {
					
					// gold includes number of label for building decomposed matrix
					// case for "true positives" and "false negatives" 
					if (Integer.valueOf(goldLabel) == decomposed){
						for (String predictionKey : matrix.get(goldKey).keySet()) {
							if (matrix.get(goldKey).get(predictionKey) != 0.0){
								String[] predictionLabels = predictionKey.split(",");
								boolean matchedPositives = false;
								for (String predictionLabel : predictionLabels) {
									// "true positives"
									if (predictionLabel == goldLabel){
										matchedPositives = true;
										decomposedConfusionMatrix[decomposed][0][0] += 
												matrix.get(goldKey).get(predictionKey);
										break;	
									}						
								}
								// "false negatives"
								if (! matchedPositives){
									decomposedConfusionMatrix[decomposed][0][1] += 
											matrix.get(goldKey).get(predictionKey);
								}
							}
						}
					}
					// case for "false positives" and "true negatives"
					else{
						for (String predictionKey : matrix.get(goldKey).keySet()) {
							if (matrix.get(goldKey).get(predictionKey) != 0.0){
								String[] predictionLabels = predictionKey.split(",");
								boolean matchedNegatives = false;
								for (String predictionLabel : predictionLabels) {
									// "false positives"
									if (predictionLabel == goldLabel){
										matchedNegatives = true;
										decomposedConfusionMatrix[decomposed][1][0] += 
												matrix.get(goldKey).get(predictionKey);
										break;
									}
								}
								// "true negatives"
								if (! matchedNegatives){
									decomposedConfusionMatrix[decomposed][1][1] += 
											matrix.get(goldKey).get(predictionKey);
								}
							}
						}
					}
				}
			}
		}
		return decomposedConfusionMatrix;
	}

}
