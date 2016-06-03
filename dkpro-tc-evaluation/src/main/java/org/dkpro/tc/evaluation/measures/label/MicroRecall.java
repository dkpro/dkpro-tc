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
 * {@literal @}article{madjarov2012extensive,
 * title={An extensive experimental comparison of methods for multi-label learning},
 * author={Madjarov, Gjorgji and Kocev, Dragi and Gjorgjevikj, Dejan and D{\v{z}}eroski, Sa{\v{s}}o},
 * journal={Pattern Recognition},
 * volume={45},
 * number={9},
 * pages={3095--3096},
 * year={2012},
 * publisher={Elsevier}
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
public class MicroRecall
{

	public static Map<String, Double> calculate(CombinedSmallContingencyTable cSCTable, 
			boolean softEvaluation) 
	{
		double tp = cSCTable.getTruePositives();
		double fn = cSCTable.getFalseNegatives();
		
		Double recall = 0.0;
		double denominator = tp + fn;
		if (denominator != 0.0) {
			recall = (Double) tp / denominator;
		}
		else if (! softEvaluation) {
			recall = Double.NaN;
		}		
		Map<String, Double> results = new HashMap<String, Double>();
		results.put(MicroRecall.class.getSimpleName(), recall);
		return results;	 
	}	
}