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

import java.util.Map;


/**
 * @author Andriy Nadolskyy
 * 
 */
public class MultiContingencyTable
	extends AbstractContingencyTable<Map<String, Map<String, Double>>>
{

	public MultiContingencyTable(
			Map<String, Map<String, Double>> matrix,
			Map<String, Integer> class2number)
	{
		super(matrix, class2number);
	}

	@Override
	public ContingencyTable decomposeContingencyTable()
	{
		ContingencyTable cTable = new ContingencyTable(class2number);
		
		for (int decomposed = 0; decomposed < class2number.size(); decomposed++){
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
										cTable.addTruePositives(decomposed, matrix.get(goldKey).get(predictionKey));
										break;	
									}						
								}
								// "false negatives"
								if (! matchedPositives){
									cTable.addFalseNegatives(decomposed, matrix.get(goldKey).get(predictionKey));
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
										cTable.addFalsePositives(decomposed, matrix.get(goldKey).get(predictionKey));
										break;
									}
								}
								// "true negatives"
								if (! matchedNegatives){
									cTable.addTrueNegatives(decomposed, matrix.get(goldKey).get(predictionKey));
								}
							}
						}
					}
				}
			}
		}
		return cTable;
	}
}
