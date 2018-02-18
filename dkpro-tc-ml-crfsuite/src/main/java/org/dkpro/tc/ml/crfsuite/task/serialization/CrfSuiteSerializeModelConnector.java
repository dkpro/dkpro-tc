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

package org.dkpro.tc.ml.crfsuite.task.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.ml.crfsuite.CrfSuiteAdapter;
import org.dkpro.tc.ml.crfsuite.task.CrfSuiteTestTask;
import org.dkpro.tc.ml.crfsuite.task.CrfUtil;

public class CrfSuiteSerializeModelConnector extends ModelSerializationTask implements Constants {

	@Discriminator(name = DIM_CLASSIFICATION_ARGS)
	private List<Object> classificationArguments;

	boolean trainModel = true;

	private String algoName;

	private List<String> algoParameters;

	@Override
	public void execute(TaskContext aContext) throws Exception {

		if (trainModel) {
			processParameters(classificationArguments);
			trainAndStoreModel(aContext);
		} else {
			copyAlreadyTrainedModel(aContext);
		}

		writeModelConfiguration(aContext);
	}

	private void copyAlreadyTrainedModel(TaskContext aContext) throws Exception {
		File file = aContext.getFile(MODEL_CLASSIFIER, AccessMode.READONLY);

		FileInputStream fis = new FileInputStream(file);
		FileOutputStream fos = new FileOutputStream(new File(outputFolder, MODEL_CLASSIFIER));
		IOUtils.copy(fis, fos);
	}

	private void trainAndStoreModel(TaskContext aContext) throws Exception {
		File trainFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY);

		String classifierPath = outputFolder.getAbsolutePath() + "/" + MODEL_CLASSIFIER;
		String trainingDataPath = trainFolder.getPath() + "/" + Constants.FILENAME_DATA_IN_CLASSIFIER_FORMAT;
		List<String> commandTrainModel = CrfSuiteTestTask.getTrainCommand(classifierPath, trainingDataPath, algoName,
				algoParameters);

		Process process = new ProcessBuilder().inheritIO().command(commandTrainModel).start();
		process.waitFor();
	}

	private void processParameters(List<Object> classificationArguments) throws Exception {
		algoName = CrfUtil.getAlgorithm(classificationArguments);
		algoParameters = CrfUtil.getAlgorithmConfigurationParameter(classificationArguments);
	}

	public void trainModel(boolean b) {
		trainModel = b;
	}

	@Override
	protected void writeAdapter() throws Exception {
		writeModelAdapterInformation(outputFolder, CrfSuiteAdapter.class.getName());
	}
}