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

import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.ContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.MeasuresBase;


/**
 * @author Andriy Nadolskyy
 * 
 */
// TODO fscore should calculate f_1 as a default but allow to configure other betas
public class MacroFScore
	implements MeasuresBase
{

	public static Double calculate(ContingencyTable cTable, boolean softEvaluation){
		int numberOfMatrices = cTable.getSize();
		double summedFScore = 0.0;
		
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
				return Double.NaN;
			}
		}
		return summedFScore / numberOfMatrices;	
	}
	
}
