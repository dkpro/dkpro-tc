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

import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.CombinedContingencyTable;


/**
 * @author Andriy Nadolskyy
 * 
 */
public class MicroFScore
{


	public static Double calculate(CombinedContingencyTable cCTable, boolean softEvaluation) {
		double tp = cCTable.getTruePositives();
		double fp = cCTable.getFalsePositives();
		double fn = cCTable.getFalseNegatives();
		
		double fScore = 0.0;
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
			fScore += (2 * precision * recall) / localSum;
		}
		else if (! softEvaluation) {
			return Double.NaN;
		}		
		return fScore;
	}	
}