/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.ml.report.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.dkpro.tc.core.Constants;

import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.Accuracy;
import de.unidue.ltl.evaluation.measures.EvaluationMeasure;
import de.unidue.ltl.evaluation.measures.categorial.Fscore;
import de.unidue.ltl.evaluation.measures.correlation.PearsonCorrelation;
import de.unidue.ltl.evaluation.measures.correlation.SpearmanCorrelation;
import de.unidue.ltl.evaluation.measures.multilabel.ExactMatchRatio;
import de.unidue.ltl.evaluation.measures.multilabel.HammingLoss;
import de.unidue.ltl.evaluation.measures.multilabel.MultilabelAccuracy;
import de.unidue.ltl.evaluation.measures.regression.MeanAbsoluteError;
import de.unidue.ltl.evaluation.measures.regression.MeanSquaredError;
import de.unidue.ltl.evaluation.measures.regression.RSquared;
import de.unidue.ltl.evaluation.visualization.ConfusionMatrix;

public class MetricComputationUtil {
	
	public static Map<String, String> getResults(File id2o, String mode) throws Exception {
		
		if(mode == null){
			throw new IllegalArgumentException("The learning mode is null");
		}
		

		Map<String, String> map = new HashMap<>();

		if (mode.equals(Constants.LM_SINGLE_LABEL)) {
			EvaluationData<String> data = Tc2LtlabEvalConverter.convertSingleLabelModeId2Outcome(id2o);

			Accuracy<String> acc = new Accuracy<>(data);
			map.put(acc.getClass().getSimpleName(), "" + acc.getResult());

			Fscore<String> fmeasure = new Fscore<>(data);
			map.put("Micro FScore", "" + getMicroFscore(fmeasure));
			map.put("Macro FScore", "" + getMacroFscore(fmeasure));
			
			ConfusionMatrix<String> matrix = new ConfusionMatrix<>(data);
			File matrixFile = new File(id2o.getParentFile(), "confusionMatrix.txt");
			FileUtils.writeStringToFile(matrixFile, getMatrix(matrix), "utf-8");
			
		} else if (mode.equals(Constants.LM_REGRESSION)) {
			
			EvaluationData<Double> data = Tc2LtlabEvalConverter.convertRegressionModeId2Outcome(id2o);

			EvaluationMeasure<?> m = new RSquared(data);
			map.put(m.getClass().getSimpleName(), getExceptionFreeResult(m));

			m = new PearsonCorrelation(data);
			map.put(m.getClass().getSimpleName(), getExceptionFreeResult(m));

			m = new SpearmanCorrelation(data);
			map.put(m.getClass().getSimpleName(), getExceptionFreeResult(m));

			m = new MeanSquaredError(data);
			map.put(m.getClass().getSimpleName(), getExceptionFreeResult(m));

			m = new MeanAbsoluteError(data);
			map.put(m.getClass().getSimpleName(), getExceptionFreeResult(m));

		} else if (mode.equals(Constants.LM_MULTI_LABEL)) {

			EvaluationData<String> data = Tc2LtlabEvalConverter.convertMultiLabelModeId2Outcome(id2o);

			EvaluationMeasure<?> m = new ExactMatchRatio<>(data);
			map.put(m.getClass().getSimpleName(), getExceptionFreeResult(m));
			
			EvaluationData<Integer> dataInt = Tc2LtlabEvalConverter.convertMultiLabelModeId2OutcomeUseInteger(id2o);
			
			m = new HammingLoss(dataInt);
			map.put(m.getClass().getSimpleName(), getExceptionFreeResult(m));
			
			m = new MultilabelAccuracy(dataInt);
			map.put(m.getClass().getSimpleName(), getExceptionFreeResult(m));
			
		}
		return map;
	}

	/**
	 * if an exception occurs, it is caught and written to string, execution should not be interrupted at this point.
	 * @param measure
	 * 		the current measure
	 * @return
	 * 		a string with the computed measure or the exception error message if an error occurred
	 */
	private static String getExceptionFreeResult(EvaluationMeasure<?> measure) {
		String val=null;
		
		try {
			val = measure.getResult()+"";
			
		} catch (Exception e) {
			String stackTrace = ExceptionUtils.getStackTrace(e);
			return "Exception occurred with following stack trace: [" + stackTrace + "]";
		}
		
		return val;
	}

	private static String getMatrix(ConfusionMatrix<String> matrix) {
		String val = "";
		
		try {
			val = matrix.toText();
		} catch (Exception e) {
			String stackTrace = ExceptionUtils.getStackTrace(e);
			return "Exception occurred with following stack trace: [" + stackTrace + "]";
		}

		return val;
	}

	private static String getMicroFscore(Fscore<String> fmeasure) {
		String retVal="";
		
		try {
			retVal = fmeasure.getMicroFscore()+"";
		} catch (Exception e) {
			String stackTrace = ExceptionUtils.getStackTrace(e);
			return "Exception occurred with following stack trace: [" + stackTrace + "]";
		}
		
		return retVal;
	}
	
	private static String getMacroFscore(Fscore<String> fmeasure) {
		String retVal="";
		
		try {
			retVal = fmeasure.getMacroFscore() + "";
		} catch (Exception e) {
			String stackTrace = ExceptionUtils.getStackTrace(e);
			return "Exception occurred with following stack trace: [" + stackTrace + "]";
		}
		
		return retVal;
	}

}
