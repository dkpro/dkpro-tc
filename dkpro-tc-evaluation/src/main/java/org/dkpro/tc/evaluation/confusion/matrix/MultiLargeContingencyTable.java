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
package org.dkpro.tc.evaluation.confusion.matrix;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;


/**
 * @author Andriy Nadolskyy
 *
 */
public class MultiLargeContingencyTable
	extends AbstractLargeContingencyTable<Map<String, Map<String, Double>>>
{

	public MultiLargeContingencyTable(
			Map<String, Map<String, Double>> largeContingencyTable, List<String> labels)
	{
		super(largeContingencyTable, labels);
	}

	@Override
	public SmallContingencyTables decomposeLargeContingencyTable()
	{
		SmallContingencyTables smallContingencyTables = new SmallContingencyTables(labels);

		for (int decomposed = 0; decomposed < largeContingencyTable.size(); decomposed++){
			for (String goldKey : largeContingencyTable.keySet()) {
				for (String predictionKey : largeContingencyTable.get(goldKey).keySet()) {
					if (largeContingencyTable.get(goldKey).get(predictionKey) != 0.0) {
//						String[] goldLabels = goldKey.split(",");
//						for (String goldLabel : goldLabels) {
							int goldLabelIndex;
							try {
								goldLabelIndex = labels.indexOf(URLDecoder.decode(goldKey, "UTF-8"));
							} catch (UnsupportedEncodingException e1) {
								goldLabelIndex = labels.indexOf(goldKey);
							}
//							String[] predictionLabels = predictionKey.split(",");
//							for (String predictionLabel : predictionLabels) {
								int predictionLabelIndex;
								try {
									 predictionLabelIndex = labels.indexOf(URLDecoder.decode(predictionKey, "UTF-8"));
								} catch (UnsupportedEncodingException e) {
									 predictionLabelIndex = labels.indexOf(predictionKey);
								}
								// true positives and false negatives
								if (goldLabelIndex == decomposed) {
									// true positives
									if (predictionLabelIndex == goldLabelIndex) {
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
									if (predictionLabelIndex == decomposed) {
										smallContingencyTables.addFalsePositives(decomposed, largeContingencyTable.get(goldKey).get(predictionKey));
									}
									// true negatives
									else {
										smallContingencyTables.addTrueNegatives(decomposed, largeContingencyTable.get(goldKey).get(predictionKey));
									}
								}
//							}
//						}
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
