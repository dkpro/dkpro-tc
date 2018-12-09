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
package org.dkpro.tc.ml.experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.reporting.Report;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.lab.task.impl.DefaultBatchTask;
import org.dkpro.lab.task.impl.TaskBase;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.DKProTcShallowTestTask;
import org.dkpro.tc.core.task.ExtractFeaturesTask;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.task.MetaInfoTask;
import org.dkpro.tc.core.task.OutcomeCollectionTask;
import org.dkpro.tc.core.task.TcTaskType;
import org.dkpro.tc.ml.FoldUtil;
import org.dkpro.tc.ml.base.Experiment_ImplBase;
import org.dkpro.tc.ml.experiment.dim.LearningCurveDimBundleCrossValidation;
import org.dkpro.tc.ml.report.BasicResultReport;
import org.dkpro.tc.ml.report.shallowlearning.InnerReport;

/**
 * Runs a learning curve experiment that splits the data into N folds. Each fold
 * will be in the test set and all fold combinations will be used as training
 * set from which averaged performance results are computed. If you have a fixed
 * test set against which a learning curve shall be run, then use
 * {@link ExperimentLearningCurveTrainTest}
 */
public class ExperimentLearningCurve extends Experiment_ImplBase {

	protected int aNumFolds = 10;

	protected InitTask initTask;
	protected OutcomeCollectionTask collectionTask;
	protected MetaInfoTask metaTask;
	protected ExtractFeaturesTask extractFeaturesTrainTask;
	protected ExtractFeaturesTask extractFeaturesTestTask;
	protected TaskBase testTask;
	private int aLimitPerStage;

	public ExperimentLearningCurve() {/* needed for Groovy */
	}

	/**
	 * 
	 * @param aExperimentName the experiment name
	 * @param aNumFolds       the number of folds
	 * @param aLimitPerStage  maximum number of runs on each learning curve stage
	 * @throws TextClassificationException in case of errors
	 */
	public ExperimentLearningCurve(String aExperimentName, int aNumFolds, int aLimitPerStage) throws TextClassificationException {
		this.aNumFolds = aNumFolds;
		this.aLimitPerStage = aLimitPerStage;
		this.experimentName = aExperimentName;
		setType("Evaluation-" + experimentName);
	}

	/**
	 * Use this constructor for CV fold control. The Comparator is used to determine
	 * which instances must occur together in the same CV fold.
	 * 
	 * @param aExperimentName the experiment name
	 * @param aNumFolds       the number of folds
	 * @throws TextClassificationException in case of errors
	 */
	public ExperimentLearningCurve(String aExperimentName, int aNumFolds)
			throws TextClassificationException {
		this(aExperimentName, aNumFolds, -1);
	}

	/**
	 * Initializes the experiment. This is called automatically before execution.
	 * It's not done directly in the constructor, because we want to be able to use
	 * setters instead of the three-argument constructor.
	 */
	protected void init() throws IllegalStateException {

		if (experimentName == null) {
			throw new IllegalStateException("You must set an experiment name");
		}

		if (aNumFolds < 2 && aNumFolds != -1) {
			throw new IllegalStateException(
					"Number of folds is not configured correctly. Number of folds needs to be at "
							+ "least [2] or [-1 (leave one out cross validation)] but was [" + aNumFolds + "]");
		}

		// initialize the setup
		initTask = new InitTask();
		initTask.setPreprocessing(getPreprocessing());
		initTask.setOperativeViews(operativeViews);
		initTask.setType(initTask.getType() + "-" + experimentName);
		initTask.setAttribute(TC_TASK_TYPE, TcTaskType.INIT_TRAIN.toString());

		// inner batch task (carried out numFolds times)
		DefaultBatchTask crossValidationTask = new DefaultBatchTask() {
			@Discriminator(name = DIM_FEATURE_MODE)
			private String featureMode;

			@Discriminator(name = DIM_CROSS_VALIDATION_MANUAL_FOLDS)
			private boolean useCrossValidationManualFolds;

			@Override
			public void initialize(TaskContext aContext) {
				super.initialize(aContext);

				File xmiPathRoot = aContext.getFolder(InitTask.OUTPUT_KEY_TRAIN, AccessMode.READONLY);
				Collection<File> files = FileUtils.listFiles(xmiPathRoot, new String[] { "bin" }, true);
				String[] fileNames = new String[files.size()];
				int i = 0;
				for (File f : files) {
					// adding file paths, not names
					fileNames[i] = f.getAbsolutePath();
					i++;
				}
				Arrays.sort(fileNames);
				if (aNumFolds == LEAVE_ONE_OUT) {
					aNumFolds = fileNames.length;
				}

				// is executed if we have less CAS than requested folds and manual mode is
				// turned
				// off
				if (!useCrossValidationManualFolds && fileNames.length < aNumFolds) {
					xmiPathRoot = createRequestedNumberOfCas(xmiPathRoot, fileNames.length, featureMode);
					files = FileUtils.listFiles(xmiPathRoot, new String[] { "bin" }, true);
					fileNames = new String[files.size()];
					i = 0;
					for (File f : files) {
						// adding file paths, not names
						fileNames[i] = f.getAbsolutePath();
						i++;
					}
				}
				// don't change any names!!
				LearningCurveDimBundleCrossValidation foldDim = getFoldDim(fileNames);
				Dimension<File> filesRootDim = Dimension.create(DIM_FILES_ROOT, xmiPathRoot);

				ParameterSpace pSpace = new ParameterSpace(foldDim, filesRootDim);
				setParameterSpace(pSpace);
			}

			/**
			 * creates required number of CAS
			 * 
			 * @param xmiPathRoot      input path
			 * @param numAvailableJCas all CAS
			 * @param featureMode      the feature mode
			 * @return a file
			 */
			private File createRequestedNumberOfCas(File xmiPathRoot, int numAvailableJCas, String featureMode) {

				try {
					File outputFolder = FoldUtil.createMinimalSplit(xmiPathRoot.getAbsolutePath(), aNumFolds,
							numAvailableJCas, FM_SEQUENCE.equals(featureMode));

					if (outputFolder == null) {
						throw new NullPointerException("Output folder is null");
					}

					verfiyThatNeededNumberOfCasWasCreated(outputFolder);

					return outputFolder;
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}

			private void verfiyThatNeededNumberOfCasWasCreated(File outputFolder) {
				int numCas = 0;

				File[] listFiles = outputFolder.listFiles();
				if (listFiles == null) {
					throw new NullPointerException("Retrieving files in folder led to a NullPointer");
				}

				for (File f : listFiles) {
					if (f.getName().contains(".bin")) {
						numCas++;
					}
				}

				if (numCas < aNumFolds) {
					throw new IllegalStateException(
							"Not enough TextClassificationUnits found to create at least [" + aNumFolds + "] folds");
				}
			}
		};

		// ================== SUBTASKS OF THE INNER BATCH TASK =======================

		// collecting meta features only on the training data (numFolds times)
		collectionTask = new OutcomeCollectionTask();
		collectionTask.setType(collectionTask.getType() + "-" + experimentName);
		collectionTask.setAttribute(TC_TASK_TYPE, TcTaskType.COLLECTION.toString());
		collectionTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN);

		metaTask = new MetaInfoTask();
		metaTask.setOperativeViews(operativeViews);
		metaTask.setType(metaTask.getType() + "-" + experimentName);
		metaTask.setAttribute(TC_TASK_TYPE, TcTaskType.META.toString());

		// extracting features from training data (numFolds times)
		extractFeaturesTrainTask = new ExtractFeaturesTask();
		extractFeaturesTrainTask.setTesting(false);
		extractFeaturesTrainTask.setType(extractFeaturesTrainTask.getType() + "-Train-" + experimentName);
		extractFeaturesTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);
		extractFeaturesTrainTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN, ExtractFeaturesTask.INPUT_KEY);
		extractFeaturesTrainTask.addImport(collectionTask, OutcomeCollectionTask.OUTPUT_KEY,
				ExtractFeaturesTask.COLLECTION_INPUT_KEY);
		extractFeaturesTrainTask.setAttribute(TC_TASK_TYPE, TcTaskType.FEATURE_EXTRACTION_TRAIN.toString());

		// extracting features from test data (numFolds times)
		extractFeaturesTestTask = new ExtractFeaturesTask();
		extractFeaturesTestTask.setTesting(true);
		extractFeaturesTestTask.setType(extractFeaturesTestTask.getType() + "-Test-" + experimentName);
		extractFeaturesTestTask.addImport(metaTask, MetaInfoTask.META_KEY);
		extractFeaturesTestTask.addImport(extractFeaturesTrainTask, ExtractFeaturesTask.OUTPUT_KEY);
		extractFeaturesTestTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN, ExtractFeaturesTask.INPUT_KEY);
		extractFeaturesTestTask.addImport(collectionTask, OutcomeCollectionTask.OUTPUT_KEY,
				ExtractFeaturesTask.COLLECTION_INPUT_KEY);
		extractFeaturesTestTask.setAttribute(TC_TASK_TYPE, TcTaskType.FEATURE_EXTRACTION_TEST.toString());

		// test task operating on the models of the feature extraction train and test
		// tasks
		List<Report> reports = new ArrayList<>();
		reports.add(new BasicResultReport());

		testTask = new DKProTcShallowTestTask(extractFeaturesTrainTask, extractFeaturesTestTask, collectionTask,
				reports, experimentName);
		testTask.setType(testTask.getType() + "-" + experimentName);
		testTask.setAttribute(TC_TASK_TYPE, TcTaskType.FACADE_TASK.toString());

		if (innerReports != null) {
			for (Report report : innerReports) {
				testTask.addReport(report);
			}
		}

		testTask.addImport(extractFeaturesTrainTask, ExtractFeaturesTask.OUTPUT_KEY, TEST_TASK_INPUT_KEY_TRAINING_DATA);
		testTask.addImport(extractFeaturesTestTask, ExtractFeaturesTask.OUTPUT_KEY, TEST_TASK_INPUT_KEY_TEST_DATA);
		testTask.addImport(collectionTask, OutcomeCollectionTask.OUTPUT_KEY, Constants.OUTCOMES_INPUT_KEY);

		// ================== CONFIG OF THE INNER BATCH TASK =======================

		crossValidationTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN);
		crossValidationTask.setType(crossValidationTask.getType() + "-" + experimentName);
		crossValidationTask.addTask(collectionTask);
		crossValidationTask.addTask(metaTask);
		crossValidationTask.addTask(extractFeaturesTrainTask);
		crossValidationTask.addTask(extractFeaturesTestTask);
		crossValidationTask.addTask(testTask);
		crossValidationTask.setExecutionPolicy(ExecutionPolicy.USE_EXISTING);
		// report of the inner batch task (sums up results for the folds)
		// we want to re-use the old CV report, we need to collect the evaluation.bin
		// files from
		// the test task here (with another report)
		crossValidationTask.addReport(InnerReport.class);
		crossValidationTask.setAttribute(TC_TASK_TYPE, TcTaskType.CROSS_VALIDATION.toString());

		// DKPro Lab issue 38: must be added as *first* task
		addTask(initTask);
		addTask(crossValidationTask);
	}

	/**
	 * 
	 * @param fileNames the file names
	 * @return fold dimension bundle
	 */
	protected LearningCurveDimBundleCrossValidation getFoldDim(String[] fileNames) {
		return new LearningCurveDimBundleCrossValidation("files", Dimension.create("", fileNames), aNumFolds, aLimitPerStage);
	}

	/**
	 * sets the number of folds
	 * 
	 * @param numFolds folds
	 */
	public void setNumFolds(int numFolds) {
		this.aNumFolds = numFolds;
	}

}