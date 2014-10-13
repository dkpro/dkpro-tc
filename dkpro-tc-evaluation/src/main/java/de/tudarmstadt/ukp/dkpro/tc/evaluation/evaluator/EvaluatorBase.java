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

import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.CombinedContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.ContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroAccuracy;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroFScore;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroPrecision;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroRecall;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MicroAccuracy;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MicroFScore;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MicroPrecision;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MicroRecall;


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
	
	/**
	 * calculation of label based macro measures 
	 * 
	 * @param cTable
	 * @return
	 */
	protected Map<String, String> calculateLabelBasedMacroMeasures(ContingencyTable cTable)
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
	
	/**
	 * calculation of label based micro measures 
	 * 
	 * @param cCTable
	 * @return
	 */
	protected Map<String, String> calculateLabelBasedMicroMeasures(CombinedContingencyTable cCTable)
	{
		Map<String, String> results = new HashMap<String, String>();
		
		// micro precision
		String keyMicroPrec = MicroPrecision.class.getSimpleName(); 
		Double microPrecValue = MicroPrecision.calculate(cCTable, softEvaluation);
		results.put(keyMicroPrec, String.valueOf(microPrecValue));
		
		// micro recall
		String keyMicroRec = MicroRecall.class.getSimpleName(); 
		Double microRecValue = MicroRecall.calculate(cCTable, softEvaluation);
		results.put(keyMicroRec, String.valueOf(microRecValue));
		
		// micro accuracy
		String keyMicroAcc = MicroAccuracy.class.getSimpleName(); 
		Double microAccValue = MicroAccuracy.calculate(cCTable, softEvaluation);
		results.put(keyMicroAcc, String.valueOf(microAccValue));
		
		// micro f-score
		String keyMicroFSc = MicroFScore.class.getSimpleName(); 
		Double microFScValue = MicroFScore.calculate(cCTable, softEvaluation);
		results.put(keyMicroFSc, String.valueOf(microFScValue));
		
		return results;
	}
}
