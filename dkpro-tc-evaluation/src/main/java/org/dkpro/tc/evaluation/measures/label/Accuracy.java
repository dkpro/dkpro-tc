/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.evaluation.measures.label;

import java.util.HashMap;
import java.util.Map;

import org.dkpro.tc.evaluation.confusion.matrix.CombinedSmallContingencyTable;


/**
 * calculation of the measure is based on:
 * 
 * <pre>
 * <code>
 * {@literal @}article{van2013macro,
 * title={Macro-and micro-averaged evaluation measures [[BASIC DRAFT]]},
 * author={Van Asch, Vincent},
 * pages = {3--4},
 * year={2013},
 * url = {http://www.cnts.ua.ac.be/~vincent/pdf/microaverage.pdf}
 * }
 * </code>
 * </pre>
 * 
 * creating of contingency tables is based on:
 * 
 * <pre>
 * <code>
 * {@literal @}article{van2013macro,
 * title={Macro-and micro-averaged evaluation measures [[BASIC DRAFT]]},
 * author={Van Asch, Vincent},
 * pages = {11--12},
 * year={2013},
 * url = {http://www.cnts.ua.ac.be/~vincent/pdf/microaverage.pdf}
 * }
 * </code>
 * </pre>
 * 
 * EMPTY_PREDICTION is not defined as a valid label. 
 */
public class Accuracy
{

	/**
	 * take just true positives of each class: diagonal of the large confusion matrix
	 * and divide it by the total number of instances in the large confusion matrix
	 * 
	 * @param cSCTable
	 * @param numberOfSmallContingencyTables
	 * @param softEvaluation
	 * @return
	 */
	public static Map<String, Double> calculate(CombinedSmallContingencyTable cSCTable, 
			int numberOfSmallContingencyTables, boolean softEvaluation) 
	{
		double tp = cSCTable.getTruePositives();
		double fp = cSCTable.getFalsePositives();
		double fn = cSCTable.getFalseNegatives();
		double tn = cSCTable.getTrueNegatives();
			
		Double accuracy = 0.0;
		double n = (Double) (tp + fp + fn + tn) / numberOfSmallContingencyTables;
		if (n != 0.0) {
			accuracy = (Double) tp / n;
		}
		else if (! softEvaluation) {
			accuracy = Double.NaN;
		}
		Map<String, Double> results = new HashMap<String, Double>();
		results.put(Accuracy.class.getSimpleName(), accuracy);
		return results;	 	
	}	
}