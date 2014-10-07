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
import java.util.LinkedList;

import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroAccuracy;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroFScore;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroPrecision;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroRecall;


/**
 * @author Andriy Nadolskyy
 * 
 */
public abstract class EvaluatorBase {
	
	protected HashMap<String, Integer> class2number;
	protected LinkedList<String> readData;
	protected boolean softEvaluation;
	
	public EvaluatorBase(HashMap<String, Integer> class2number,
			LinkedList<String> readData, boolean softEvaluation) {
		super();
		this.class2number = class2number;
		this.readData = readData;
		this.softEvaluation = softEvaluation;
	}
	
	public abstract HashMap<String, String> calculateEvaluationMeasures();		
	
	/***
	 * calculation of label based measures 
	 * 
	 * @param decomposedConfusionMatrix
	 * @return
	 */
	protected HashMap<String, String> calculateLabelBasedEvaluationMeasures(
			double[][][] decomposedConfusionMatrix) {
		HashMap<String, String> results = new HashMap<String, String>();
		
		// macro precision
		MacroPrecision macroPrec = new MacroPrecision(decomposedConfusionMatrix);
		String keyMacroPrec = MacroPrecision.class.getSimpleName(); 
		Double macroPrecValue = macroPrec.calculateMeasure(softEvaluation);
		results.put(keyMacroPrec, String.valueOf(macroPrecValue));
		// macro recall
		MacroRecall macroRec = new MacroRecall(decomposedConfusionMatrix);
		String keyMacroRec = MacroRecall.class.getSimpleName(); 
		Double macroRecValue = macroRec.calculateMeasure(softEvaluation);
		results.put(keyMacroRec, String.valueOf(macroRecValue));
		// macro accuracy
		MacroAccuracy macroAcc = new MacroAccuracy(decomposedConfusionMatrix);
		String keyMacroAcc = MacroAccuracy.class.getSimpleName(); 
		Double macroAccValue = macroAcc.calculateMeasure(softEvaluation);
		results.put(keyMacroAcc, String.valueOf(macroAccValue));
		// macro f-score
		MacroFScore macroFSc = new MacroFScore(decomposedConfusionMatrix);
		String keyMacroFSc = MacroFScore.class.getSimpleName(); 
		Double macroFScValue = macroFSc.calculateMeasure(softEvaluation);
		results.put(keyMacroFSc, String.valueOf(macroFScValue));
		return results;
	}
}
