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

import org.apache.commons.io.FileUtils;
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
	protected File runPrediction(File testFile) throws Exception {

		File prediction = FileUtil.createTempFile("svmHmmPrediction", ".svmhmm");
		prediction.deleteOnExit();
		
		File model = new File(tcModelLocation, Constants.MODEL_CLASSIFIER);
		File binary = SvmHmmTestTask.resolveSvmHmmPredictionCommand();

		// SvmHmm struggles with paths longer than 255 characters to circumvent this
		// issue, we copy all files together into a local directory to ensure short path
		// names that are below this threshold
		File localModel = new File(binary.getParentFile(), "model.tmp");
		FileUtils.copyFile(model, localModel);
		File localTestFile = new File(binary.getParentFile(), "testfile.txt");
		FileUtils.copyFile(testFile, localTestFile);
		
		
		List<String> command = SvmHmmTestTask.buildPredictionCommand(binary, localTestFile, localModel, prediction);
		SvmHmmTestTask.runCommand(command);

		
		FileUtils.deleteQuietly(localModel);
		FileUtils.deleteQuietly(localTestFile);
		
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