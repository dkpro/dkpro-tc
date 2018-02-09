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
package org.dkpro.tc.ml.svmhmm.task.serialization;

import java.io.File;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatLoadModelConnector;
import org.dkpro.tc.ml.svmhmm.task.SvmHmmTestTask;

public class SvmhmmLoadModelConnector extends LibsvmDataFormatLoadModelConnector {
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		//SvmHmm doesn't like negative values or zeros as dummy outcomes
		OUTCOME_PLACEHOLDER = "1";
	}
	
	@Override
	protected File runPrediction(File tempFile) throws Exception {

		File prediction = FileUtil.createTempFile("svmHmmPrediction", ".svmhmm");
		prediction.deleteOnExit();
		
		File model = new File(tcModelLocation, Constants.MODEL_CLASSIFIER);
		
		List<String> command = SvmHmmTestTask.buildPredictionCommand(tempFile, model, prediction);
		SvmHmmTestTask.runCommand(command);

		return prediction;
	}

	int currSeqId = 0;
	int lastId = -1;
	static final String TAB = "\t";

	@Override
	protected String injectSequenceId(Instance instance) {
		/*
		 * The sequence id must continuously increase, TC's id is Cas-relative
		 * and restarts for a new Cas at zero again
		 */
		if (lastId < 0) {
			lastId = instance.getJcasId();
		}

		if (lastId > -1 && lastId != instance.getJcasId()) {
			currSeqId++;
		}

		return TAB + "qid:" + currSeqId;
	}

}