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

import org.dkpro.tc.core.Constants;

import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.Accuracy;
import de.unidue.ltl.evaluation.measures.categorial.Fscore;
import de.unidue.ltl.evaluation.measures.correlation.PearsonCorrelation;
import de.unidue.ltl.evaluation.measures.correlation.SpearmanCorrelation;
import de.unidue.ltl.evaluation.measures.multilabel.ExactMatchRatio;
import de.unidue.ltl.evaluation.measures.multilabel.HammingLoss;
import de.unidue.ltl.evaluation.measures.multilabel.MultilabelAccuracy;
import de.unidue.ltl.evaluation.measures.regression.MeanAbsoluteError;
import de.unidue.ltl.evaluation.measures.regression.MeanSquaredError;
import de.unidue.ltl.evaluation.measures.regression.RSquared;
import de.unidue.ltl.evaluation.util.convert.DKProTcDataFormatConverter;

public class MetricComputationUtil {
	
	public static Map<String, String> getResults(File id2o, String mode) throws Exception {

		Map<String, String> map = new HashMap<>();

		if (mode.equals(Constants.LM_SINGLE_LABEL)) {
			EvaluationData<String> data = DKProTcDataFormatConverter.convertSingleLabelModeId2Outcome(id2o);

			Accuracy<String> acc = new Accuracy<>(data);
			map.put(acc.getClass().getSimpleName(), "" + acc.getResult());

			Fscore<String> fmeasure = new Fscore<>(data);
			map.put("Micro FScore", "" + fmeasure.getMicroFscore());
			map.put("Macro FScore", "" + fmeasure.getMacroFscore());
		} else if (mode.equals(Constants.LM_REGRESSION)) {
			EvaluationData<Double> data = DKProTcDataFormatConverter.convertRegressionModeId2Outcome(id2o);

			RSquared rsq = new RSquared(data);
			map.put(rsq.getClass().getSimpleName(), "" + rsq.getResult());

			PearsonCorrelation pc = new PearsonCorrelation(data);
			map.put(pc.getClass().getSimpleName(), "" + pc.getResult());

			SpearmanCorrelation sc = new SpearmanCorrelation(data);
			map.put(sc.getClass().getSimpleName(), "" + sc.getResult());

			MeanSquaredError mse = new MeanSquaredError(data);
			map.put(mse.getClass().getSimpleName(), "" + mse.getResult());

			MeanAbsoluteError mae = new MeanAbsoluteError(data);
			map.put(mae.getClass().getSimpleName(), "" + mae.getResult());

		} else if (mode.equals(Constants.LM_MULTI_LABEL)) {

			EvaluationData<String> data = DKProTcDataFormatConverter.convertMultiLabelModeId2Outcome(id2o);

			ExactMatchRatio<String> emr = new ExactMatchRatio<>(data);
			map.put(emr.getClass().getSimpleName(), "" + emr.getResult());
			
			
			EvaluationData<Integer> dataInt = DKProTcDataFormatConverter.convertMultiLabelModeId2OutcomeUseInteger(id2o);
			
			HammingLoss hl = new HammingLoss(dataInt);
			map.put(hl.getClass().getSimpleName(), "" + hl.getResult());
			
			MultilabelAccuracy ma = new MultilabelAccuracy(dataInt);
			map.put(ma.getClass().getSimpleName(), "" + ma.getResult());
			
		}
		return map;
	}

}
