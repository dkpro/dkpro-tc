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
package de.tudarmstadt.ukp.dkpro.tc.crfsuite.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.dkpro.core.api.resources.PlatformDetector;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteAdapter;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.writer.LabelSubstitutor;

public class CRFSuiteTestTask extends ExecutableTaskBase implements Constants {
	@Discriminator
	private String learningMode;
	@Discriminator
	private String[] classificationArguments;

	public static final String MODELNAME = "model.crfsuite";
	public static final String FILE_PER_CLASS_PRECISION_RECALL_F1 = "precisionRecallF1PerWordClass.txt";
	Log logger = null;

	private String executablePath = null;
	private String modelLocation = null;
	private File trainFile = null;
	private File testFile = null;

	@Override
	public void execute(TaskContext aContext) throws Exception {
		boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

		if (multiLabel) {
			throw new TextClassificationException(
					"Multi-label requested, but CRFSuite only supports single label setups.");
		}

		sanityCheckOnClassificationArguments();

		executablePath = getExecutablePath();
		modelLocation = trainModel(aContext);
		String rawTextOutput = testModel(aContext);

		writePredictions2File(aContext, rawTextOutput);

	}

	private void sanityCheckOnClassificationArguments() throws Exception {
		if (classificationArguments == null
				|| classificationArguments.length == 0) {
			log("No algorithm has been provided - will use CRFsuite default (lbfgs)");
			return;
		}

		if (classificationArguments.length == 1) {
			return;
		}

		/*
		 * At the moment only a pair of parameters is expected for provide the
		 * algorithm CRFsuite uses
		 */
		throw new Exception("Unexpected amount of classification arguments: "
				+ "[" + classificationArguments.length
				+ "] expected either zero or one");
	}

	public static String getExecutablePath() throws Exception {
		PlatformDetector pd = new PlatformDetector();
		String platform = pd.getPlatformId();
		LogFactory.getLog(CRFSuiteTestTask.class.getName()).info(
				"Load binary for platform: [" + platform + "]");

		String executablePath = new RuntimeProvider(
				"classpath:/de/tudarmstadt/ukp/dkpro/tc/crfsuite/").getFile(
				"crfsuite").getAbsolutePath();

		LogFactory.getLog(CRFSuiteTestTask.class.getName()).info(
				"Will use binary: [" + executablePath + "]");

		return executablePath;
	}

	private void writePredictions2File(TaskContext aContext,
			String aRawTextOutput) throws Exception {

		writeCRFSuiteGeneratedReports2File(aContext);

		List<String> predictionValues = writeSelfGeneratedAccuracyReport2File(
				aContext, aRawTextOutput);

		writeFileWithPredictedLabels(aContext, predictionValues);
	}

	private void writeFileWithPredictedLabels(TaskContext aContext,
			List<String> predictionValues) throws Exception {
		File predictionsFile = new File(aContext.getStorageLocation(
				TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE), CRFSuiteAdapter
				.getInstance().getFrameworkFilename(
						AdapterNameEntries.predictionsFile));

		StringBuilder sb = new StringBuilder();
		sb.append("#Gold\tPrediction\n");
		for (String p : predictionValues) {
			sb.append(LabelSubstitutor.undoLabelReplacement(p) + "\n");
			// NOTE: CRFSuite has a bug when the label is ':' (as in
			// PennTreeBank Part-of-speech tagset for instance)
			// We perform a substitutions to something crfsuite can handle
			// correctly, see class
			// LabelSubstitutor for more details
		}
		FileUtils.writeStringToFile(predictionsFile, sb.toString());

	}

	private List<String> writeSelfGeneratedAccuracyReport2File(
			TaskContext aContext, String aRawTextOutput) throws Exception {
		String[] lines = aRawTextOutput.split("\n");

		int correct = 0;
		int incorrect = 0;

		List<String> predictionValues = new ArrayList<String>();
		for (String line : lines) {
			predictionValues.add(line);

			String[] split = line.split("\t");
			if (split.length < 2) {
				continue;
			}
			String actual = split[0];
			String prediction = split[1];

			if (actual.equals(prediction)) {
				correct++;
			} else {
				incorrect++;
			}
		}

		double denominator = correct + incorrect;
		double numerator = correct;
		double accuracy = 0;
		if (denominator > 0) {
			accuracy = numerator / denominator;
		}
		log("Accuracy: " + accuracy * 100 + " (" + correct + " correct, "
				+ incorrect + " incorrect)");

		// file to hold prediction results
		File evalFile = new File(aContext.getStorageLocation(
				TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE), CRFSuiteAdapter
				.getInstance().getFrameworkFilename(
						AdapterNameEntries.evaluationFile));
		StringBuilder sb = new StringBuilder();
		sb.append(ReportConstants.CORRECT + "=" + correct + "\n");
		sb.append(ReportConstants.INCORRECT + "=" + incorrect + "\n");
		sb.append(ReportConstants.PCT_CORRECT + "=" + accuracy + "\n");
		FileUtils.writeStringToFile(evalFile, sb.toString());

		return predictionValues;
	}

	private void writeCRFSuiteGeneratedReports2File(TaskContext aContext)
			throws Exception {
		String precRecF1perClass = getPrecisionRecallF1PerClass();
		log(precRecF1perClass);
		File precRecF1File = new File(aContext.getStorageLocation(
				TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE),
				FILE_PER_CLASS_PRECISION_RECALL_F1);
		FileUtils.write(precRecF1File, "\n" + precRecF1perClass);
	}

	private String getPrecisionRecallF1PerClass() throws Exception {
		String executablePath = getExecutablePath();
		List<String> evalCommand = new ArrayList<String>();
		evalCommand.add(executablePath);
		evalCommand.add("tag");
		evalCommand.add("-qt");
		evalCommand.add("-m");
		evalCommand.add(modelLocation);
		evalCommand.add(testFile.getAbsolutePath());

		Process process = new ProcessBuilder().command(evalCommand).start();
		String output = captureProcessOutput(process);

		return output;
	}

	private String testModel(TaskContext aContext) throws Exception {

		List<String> testModelCommand = buildTestCommand(aContext);
		log("Testing model");
		String output = runTest(testModelCommand);
		log("Testing model finished");

		return output;
	}

	public static String runTest(List<String> aTestModelCommand)
			throws Exception {
		Process process = new ProcessBuilder().command(aTestModelCommand)
				.start();

		String output = captureProcessOutput(process);

		return output;

	}

	private static String captureProcessOutput(Process aProcess) {
		InputStream src = aProcess.getInputStream();
		Scanner sc = new Scanner(src);
		StringBuilder dest = new StringBuilder();
		while (sc.hasNextLine()) {
			String l = sc.nextLine();
			dest.append(l + "\n");
		}
		sc.close();
		return dest.toString();
	}

	private List<String> buildTestCommand(TaskContext aContext)
			throws Exception {
		File tmpTest = new File(aContext.getStorageLocation(
				TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY).getPath()
				+ "/"
				+ CRFSuiteAdapter.getInstance().getFrameworkFilename(
						AdapterNameEntries.featureVectorsFile));
		testFile = ResourceUtils.getUrlAsFile(tmpTest.toURI().toURL(), true);

		return wrapTestCommandAsList(testFile, executablePath, modelLocation);
	}

	public static List<String> wrapTestCommandAsList(File aTestFile,
			String aExecutablePath, String aModelLocation) {
		List<String> commandTestModel = new ArrayList<String>();
		commandTestModel.add(aExecutablePath);
		commandTestModel.add("tag");
		commandTestModel.add("-r");
		commandTestModel.add("-m");
		commandTestModel.add(aModelLocation);
		commandTestModel.add(aTestFile.getAbsolutePath());
		return commandTestModel;
	}

	private String trainModel(TaskContext aContext) throws Exception {
		String tmpModelLocation = System.getProperty("java.io.tmpdir")
				+ File.separator + MODELNAME;
		List<String> modelTrainCommand = buildTrainCommand(aContext,
				tmpModelLocation);

		log("Start training model");
		long time = System.currentTimeMillis();
		runTrain(modelTrainCommand);
		long completedIn = System.currentTimeMillis() - time;
		String formattedDuration = DurationFormatUtils.formatDuration(
				completedIn, "HH:mm:ss:SS");
		log("Training finished after " + formattedDuration);

		return writeModel(aContext, tmpModelLocation);
	}

	private void runTrain(List<String> aModelTrainCommand) throws Exception {
		Process process = new ProcessBuilder().inheritIO()
				.command(aModelTrainCommand).start();
		process.waitFor();
	}

	private String writeModel(TaskContext aContext, String aTmpModelLocation)
			throws Exception {
		aContext.storeBinary(MODELNAME, new FileInputStream(new File(
				aTmpModelLocation)));

		File modelLocation = aContext.getStorageLocation(MODELNAME,
				AccessMode.READONLY);

		return modelLocation.getAbsolutePath();
	}

	private List<String> buildTrainCommand(TaskContext aContext,
			String aTmpModelLocation) throws Exception {
		File tmpTrain = new File(aContext.getStorageLocation(
				TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY)
				.getPath()
				+ "/"
				+ CRFSuiteAdapter.getInstance().getFrameworkFilename(
						AdapterNameEntries.featureVectorsFile));

		trainFile = ResourceUtils.getUrlAsFile(tmpTrain.toURI().toURL(), true);

		return getTrainCommand(aTmpModelLocation, trainFile.getAbsolutePath(),
				classificationArguments != null ? classificationArguments[0]
						: null);
	}

	public static List<String> getTrainCommand(String modelOutputLocation,
			String trainingFile, String algorithm) throws Exception {
		List<String> commandTrainModel = new ArrayList<String>();
		commandTrainModel.add(getExecutablePath());
		commandTrainModel.add("learn");
		commandTrainModel.add("-m");
		commandTrainModel.add(modelOutputLocation);

		// add algorithm if provided
		if (algorithm != null) {
			commandTrainModel.add("-a");
			commandTrainModel.add(algorithm);
		}

		commandTrainModel.add(trainingFile);
		return commandTrainModel;
	}

	private void log(String text) {
		if (logger == null) {
			logger = LogFactory.getLog(getClass());
		}
		logger.info(text);
	}
}
