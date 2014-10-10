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
package de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.ContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroAccuracy;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroFScore;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroPrecision;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroRecall;


/**
 * @author Andriy Nadolskyy
 * 
 */
public abstract class EvaluatorBase {
	
	protected Map<String, Integer> class2number;
	protected List<String> readData;
	protected boolean softEvaluation;
	
	public EvaluatorBase(Map<String, Integer> class2number,
			List<String> readData, boolean softEvaluation) {
		super();
		this.class2number = class2number;
		this.readData = readData;
		this.softEvaluation = softEvaluation;
	}
	
	public abstract Map<String, String> calculateEvaluationMeasures();		
	
	/***
	 * calculation of label based measures 
	 * 
	 * @param decomposedConfusionMatrix
	 * @return
	 */
	protected Map<String, String> calculateLabelBasedEvaluationMeasures(ContingencyTable cTable)
	{
		Map<String, String> results = new HashMap<String, String>();
		
		// macro precision
		String keyMacroPrec = MacroPrecision.class.getSimpleName(); 
		Double macroPrecValue = MacroPrecision.calculate(cTable, softEvaluation);
		results.put(keyMacroPrec, String.valueOf(macroPrecValue));
		
		// macro recall
		String keyMacroRec = MacroRecall.class.getSimpleName(); 
		Double macroRecValue = MacroRecall.calculate(cTable, softEvaluation);
		results.put(keyMacroRec, String.valueOf(macroRecValue));
		
		// macro accuracy
		String keyMacroAcc = MacroAccuracy.class.getSimpleName(); 
		Double macroAccValue = MacroAccuracy.calculate(cTable, softEvaluation);
		results.put(keyMacroAcc, String.valueOf(macroAccValue));
		
		// macro f-score
		String keyMacroFSc = MacroFScore.class.getSimpleName(); 
		Double macroFScValue = MacroFScore.calculate(cTable, softEvaluation);
		results.put(keyMacroFSc, String.valueOf(macroFScValue));
		
		return results;
	}
}
