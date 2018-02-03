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
package org.dkpro.tc.io.libsvm;

import java.io.File;
import java.util.List;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;

public abstract class LibsvmDataFormatTestTask extends ExecutableTaskBase implements Constants {

	@Discriminator(name = DIM_CLASSIFICATION_ARGS)
	protected List<String> classificationArguments;

	@Discriminator(name = DIM_LEARNING_MODE)
	protected String learningMode;

	@Discriminator(name = DIM_FEATURE_MODE)
	protected String featureMode;

	protected abstract void runPrediction(TaskContext aContext) throws Exception;

	protected File getPredictionFile(TaskContext aContext) {
		File folder = aContext.getFolder("", AccessMode.READWRITE);
		return new File(folder, Constants.FILENAME_PREDICTIONS);
	}

	protected File getTestFile(TaskContext aContext) {
		File testFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY);
		File fileTest = new File(testFolder, Constants.FILENAME_DATA_IN_CLASSIFIER_FORMAT);
		return fileTest;
	}

	protected File getTrainFile(TaskContext aContext) {
		File trainFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY);
		File fileTrain = new File(trainFolder, Constants.FILENAME_DATA_IN_CLASSIFIER_FORMAT);

		return fileTrain;
	}

	protected void throwExceptionIfMultiLabelMode() throws TextClassificationException {
		boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);
		if (multiLabel) {
			throw new TextClassificationException("Multi-label is not supported");
		}
	}

}
