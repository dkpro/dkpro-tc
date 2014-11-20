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
public class SingleLargeContingencyTable 
	extends AbstractLargeContingencyTable<List<List<Double>>>
{

	public SingleLargeContingencyTable(List<List<Double>> largeContingencyTable,
			Map<String, Integer> class2number)
	{
		super(largeContingencyTable, class2number);
	}

	@Override
	public SmallContingencyTables decomposeLargeContingencyTable()
	{
		SmallContingencyTables smallContingencyTables = new SmallContingencyTables(class2number);
		
		for (int x = 0; x < smallContingencyTables.getSize(); x++){
			for (int y = 0; y < smallContingencyTables.getSize(); y++){
				// true positives
				if (x == y) {
					smallContingencyTables.addTruePositives(x, largeContingencyTable.get(x).get(x));
				} 
				// false negatives
				else {
					smallContingencyTables.addFalseNegatives(x, largeContingencyTable.get(x).get(y));
				}
				
				if (y != x ){
					// false positives
					smallContingencyTables.addFalsePositives(x, largeContingencyTable.get(y).get(x));
					
					for (int z = 0; z < smallContingencyTables.getSize(); z++){
						// true negatives
						if (z != x){
							smallContingencyTables.addTrueNegatives(x, largeContingencyTable.get(y).get(z));
						}
					}
				}
			}
		}
		
		return smallContingencyTables;
	}

}
