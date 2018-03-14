/**
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
 */
package org.dkpro.tc.ml.xgboost;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatLoadModelConnector;

public class XgboostLoadModelConnector extends LibsvmDataFormatLoadModelConnector {
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		//SvmHmm doesn't like negative values or zeros as dummy outcomes
		OUTCOME_PLACEHOLDER = "1";
	}
	
	@Override
	protected File runPrediction(File testFile) throws Exception {
		
		File model = new File(tcModelLocation, Constants.MODEL_CLASSIFIER);
		File prediction = XgboostTestTask.createTemporaryPredictionFile();
		File executable = XgboostTestTask.getExecutable();
		String content = XgboostTestTask.buildTestConfigFile(testFile, (File) model, prediction);
		File file = XgboostTestTask.writeConfigFile(executable.getParentFile(), "predict.conf", content);
		
		List<String> predictionCommand = new ArrayList<>();
		predictionCommand.add(executable.getAbsolutePath());
		predictionCommand.add(file.getAbsolutePath());
		XgboostTestTask.runCommand(predictionCommand);
		
		return prediction;
	}
	
}