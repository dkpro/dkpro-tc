/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator;

import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.Id2Outcome;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.CombinedSmallContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.SmallContingencyTables;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroFScore;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroPrecision;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroRecall;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MicroFScore;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MicroPrecision;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MicroRecall;


/**
 * @author Andriy Nadolskyy
 * 
 */
public abstract class EvaluatorBase {
	
	protected Id2Outcome id2Outcome;
	protected boolean softEvaluation;
	protected boolean individualLabelMeasures;
	
	public EvaluatorBase(Id2Outcome id2Outcome, boolean softEvaluation,
			boolean individualLabelMeasures) {
		super();
		this.id2Outcome = id2Outcome;
		this.softEvaluation = softEvaluation;
		this.individualLabelMeasures = individualLabelMeasures;
	}
	
    public abstract Map<String, Double> calculateEvaluationMeasures() throws TextClassificationException;

    public abstract Map<String, Double> calculateMicroEvaluationMeasures() throws TextClassificationException;
	
	/**
	 * calculation of label based macro measures 
	 * 
	 * @param cTable
	 * @return
	 */
	protected Map<String, Double> calculateMacroMeasures(SmallContingencyTables cTable)
	{
		Map<String, Double> results = new HashMap<String, Double>();
		Map<String, Double> macroFScRes;
		Map<String, Double> macroPrRes;
		Map<String, Double> macroReRes;

		if (individualLabelMeasures) {
			Map<Integer, String> number2class = new  HashMap<Integer, String>();
			for (String classValue : cTable.getClass2Number().keySet()) {
				Integer number = cTable.getClass2Number().get(classValue);
				number2class.put(number, classValue);
			}

			macroFScRes = MacroFScore.calculateExtraIndividualLabelMeasures(cTable, 
					softEvaluation, number2class);
			macroPrRes = MacroPrecision.calculateExtraIndividualLabelMeasures(cTable, 
					softEvaluation, number2class);
			macroReRes = MacroRecall.calculateExtraIndividualLabelMeasures(cTable, 
					softEvaluation, number2class);
		}
		else{
			macroFScRes = MacroFScore.calculate(cTable, softEvaluation);
			macroPrRes = MacroPrecision.calculate(cTable, softEvaluation);
			macroReRes = MacroRecall.calculate(cTable, softEvaluation);
		}
		results.putAll(macroFScRes);
		results.putAll(macroPrRes);
		results.putAll(macroReRes);
		return results;
	}
	
	/**
	 * calculation of label based micro measures 
	 * 
	 * @param cCTable
	 * @return
	 */
	protected Map<String, Double> calculateMicroMeasures(CombinedSmallContingencyTable cCTable)
	{
		Map<String, Double> results = new HashMap<String, Double>();
		
		Map<String, Double> microFScRes = MicroFScore.calculate(cCTable, softEvaluation);
		Map<String, Double> microPrRes = MicroPrecision.calculate(cCTable, softEvaluation);
		Map<String, Double> microReRes = MicroRecall.calculate(cCTable, softEvaluation);
		
		results.putAll(microFScRes);
		results.putAll(microPrRes);
		results.putAll(microReRes);		
		return results;
	}
}
