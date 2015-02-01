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
public class MultiLargeContingencyTable
	extends AbstractLargeContingencyTable<Map<String, Map<String, Double>>>
{

	public MultiLargeContingencyTable(
			Map<String, Map<String, Double>> largeContingencyTable,
			Map<String, Integer> class2number)
	{
		super(largeContingencyTable, class2number);
	}

	@Override
	public SmallContingencyTables decomposeLargeContingencyTable()
	{
		SmallContingencyTables smallContingencyTables = new SmallContingencyTables(class2number);
		
		for (int decomposed = 0; decomposed < class2number.size(); decomposed++){
			for (String goldKey : largeContingencyTable.keySet()) {
				for (String predictionKey : largeContingencyTable.get(goldKey).keySet()) {
					
					if (largeContingencyTable.get(goldKey).get(predictionKey) != 0.0) {
						String[] goldLabels = goldKey.split(",");
						for (String goldLabel : goldLabels) {
							String[] predictionLabels = predictionKey.split(",");
							for (String predictionLabel : predictionLabels) {
								// true positives and false negatives
								if (Integer.valueOf(goldLabel) == decomposed) {							
									// true positives
									if (Integer.valueOf(predictionLabel) == Integer.valueOf(goldLabel)) {
										smallContingencyTables.addTruePositives(decomposed, largeContingencyTable.get(goldKey).get(predictionKey));
									}
									// false negatives
									else {
										smallContingencyTables.addFalseNegatives(decomposed, largeContingencyTable.get(goldKey).get(predictionKey));
									}
								}
								// true negatives and false positives
								else {
									// false positives
									if (Integer.valueOf(predictionLabel) == decomposed) {
										smallContingencyTables.addFalsePositives(decomposed, largeContingencyTable.get(goldKey).get(predictionKey));
									}
									// true negatives
									else {
										smallContingencyTables.addTrueNegatives(decomposed, largeContingencyTable.get(goldKey).get(predictionKey));
									}
								}
							}
						}
					}
					else {
						// do nothing
					}
				}
				
			}
		}
		return smallContingencyTables;
	}
}
