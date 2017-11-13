/*******************************************************************************
 * Copyright 2017
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
package org.dkpro.tc.ml.liblinear;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.liblinear.util.LiblinearUtils;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class LiblinearTestTask extends ExecutableTaskBase implements Constants {
	@Discriminator(name = DIM_CLASSIFICATION_ARGS)
	private List<String> classificationArguments;
	@Discriminator(name = DIM_FEATURE_MODE)
	private String featureMode;
	@Discriminator(name = DIM_LEARNING_MODE)
	private String learningMode;

	public static final String SEPARATOR_CHAR = ";";
	public static final double EPISILON_DEFAULT = 0.01;
	public static final double PARAM_C_DEFAULT = 1.0;

	@Override
	public void execute(TaskContext aContext) throws Exception {
		boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);
		if (multiLabel) {
			throw new TextClassificationException(
					"Multi-label requested, but LIBLINEAR only supports single label setups.");
		}

		boolean isRegression = learningMode.equals(Constants.LM_REGRESSION);

		File fileTrain = getTrainFile(aContext);
		File fileTest = getTestFile(aContext);
		
		File outcomeFolder = aContext.getFolder(Constants.OUTCOMES_INPUT_KEY, AccessMode.READONLY);
        File outcomeFile = new File(outcomeFolder, Constants.FILENAME_OUTCOMES);

		Map<String, Integer> outcomeMapping = LiblinearUtils.createMapping(outcomeFile, isRegression);
		File idMappedTrainFile = LiblinearUtils.replaceOutcome(fileTrain, outcomeMapping);
		File idMappedTestFile = LiblinearUtils.replaceOutcome(fileTest, outcomeMapping);
		writeMapping(aContext, outcomeMapping, isRegression);

		// default for bias is -1, documentation says to set it to 1 in order to
		// get results closer
		// to libsvm
		// writer adds bias, so if we de-activate that here for some reason, we
		// need to also
		// deactivate it there
		Problem train = Problem.readFromFile(idMappedTrainFile, 1.0);

		SolverType solver = LiblinearUtils.getSolver(classificationArguments);
		double C = LiblinearUtils.getParameterC(classificationArguments);
		double eps = LiblinearUtils.getParameterEpsilon(classificationArguments);

		Linear.setDebugOutput(null);

		Parameter parameter = new Parameter(solver, C, eps);
		Model model = Linear.train(train, parameter);

		Problem test = Problem.readFromFile(idMappedTestFile, 1.0);

		predict(aContext, model, test);
	}

	private void writeMapping(TaskContext aContext, Map<String, Integer> outcomeMapping, boolean isRegression)
			throws IOException {
		if (isRegression) {
			return;
		}
		File mappingFile = new File(aContext.getFolder("", AccessMode.READWRITE),
				LiblinearAdapter.getOutcomeMappingFilename());
		FileUtils.writeStringToFile(mappingFile, LiblinearUtils.outcomeMap2String(outcomeMapping), "utf-8");
	}

	private File getTestFile(TaskContext aContext) {
		File testFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY);
		String testFileName = LiblinearAdapter.getInstance()
				.getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
		File fileTest = new File(testFolder, testFileName);
		return fileTest;
	}

	private File getTrainFile(TaskContext aContext) {
		File trainFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY);
		String trainFileName = LiblinearAdapter.getInstance()
				.getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
		File fileTrain = new File(trainFolder, trainFileName);

		return fileTrain;
	}

	private void predict(TaskContext aContext, Model model, Problem test) throws Exception {
		File predFolder = aContext.getFolder("", AccessMode.READWRITE);
		String predFileName = LiblinearAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.predictionsFile);
		File predictionsFile = new File(predFolder, predFileName);

		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(predictionsFile), "utf-8"));
		writer.append("#PREDICTION;GOLD" + "\n");

		Feature[][] testInstances = test.x;
		for (int i = 0; i < testInstances.length; i++) {
			Feature[] instance = testInstances[i];
			Double prediction = Linear.predict(model, instance);

			writer.write(prediction + SEPARATOR_CHAR + new Double(test.y[i]));
			writer.write("\n");
		}

		writer.close();
	}

}