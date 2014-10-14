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

import java.util.List;
import java.util.Map;


/**
 * @author Andriy Nadolskyy
 * 
 */
public class SingleContingencyTable 
	extends AbstractContingencyTable<List<List<Double>>>
{

	public SingleContingencyTable(List<List<Double>> matrix,
			Map<String, Integer> class2number)
	{
		super(matrix, class2number);
	}

	@Override
	public ContingencyTable decomposeConfusionMatrix()
	{
		ContingencyTable table = new ContingencyTable(class2number);
		
		for (int x = 0; x < table.getSize(); x++){
			for (int y = 0; y < table.getSize(); y++){
				// true positives
				if (x == y) {
					table.addTruePositives(x, matrix.get(x).get(x));
				} 
				// false negatives
				else {
					table.addFalseNegatives(x, matrix.get(x).get(y));
				}
				
				if (y != x ){
					// false positives
					table.addFalsePositives(x, matrix.get(y).get(x));
					
					for (int z = 0; z < table.getSize(); z++){
						// true negatives
						if (z != x){
							table.addTrueNegatives(x, matrix.get(y).get(z));
						}
					}
				}
			}
		}
		
		return table;
	}

}
