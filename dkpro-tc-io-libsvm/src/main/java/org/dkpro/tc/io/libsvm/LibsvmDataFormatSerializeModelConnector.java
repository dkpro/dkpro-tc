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
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.ModelSerializationTask;

public abstract class LibsvmDataFormatSerializeModelConnector extends ModelSerializationTask implements Constants {

	@Discriminator(name = DIM_CLASSIFICATION_ARGS)
	protected List<String> classificationArguments;

	boolean trainModel = true;

	@Override
	public void execute(TaskContext aContext) throws Exception {
		trainAndStoreModel(aContext);
		writeModelConfiguration(aContext);
	}

	private void trainAndStoreModel(TaskContext aContext) throws Exception {
		boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

		if (multiLabel) {
			throw new TextClassificationException("Multi-label is not yet implemented");
		}

		File fileTrain = getTrainFile(aContext);

		trainModel(fileTrain);

		copyOutcomeMappingToThisFolder(aContext);
		copyFeatureNameMappingToThisFolder(aContext);
	}

	protected abstract void trainModel(File fileTrain) throws Exception;

	private void copyOutcomeMappingToThisFolder(TaskContext aContext) throws IOException {

		if (isRegression()) {
			return;
		}

		File trainDataFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY);
		String mapping = AdapterFormat.getOutcomeMappingFilename();

		FileUtils.copyFile(new File(trainDataFolder, mapping), new File(outputFolder, mapping));
	}

	private boolean isRegression() {
		return learningMode.equals(Constants.LM_REGRESSION);
	}

	private void copyFeatureNameMappingToThisFolder(TaskContext aContext) throws IOException {
		File trainDataFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY);
		String mapping = AdapterFormat.getFeatureNameMappingFilename();

		FileUtils.copyFile(new File(trainDataFolder, mapping), new File(outputFolder, mapping));
	}

	private File getTrainFile(TaskContext aContext) {
		File trainFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY);
		File fileTrain = new File(trainFolder, Constants.FILENAME_DATA_IN_CLASSIFIER_FORMAT);

		return fileTrain;
	}

	@Override
	protected abstract void writeAdapter() throws Exception;

}