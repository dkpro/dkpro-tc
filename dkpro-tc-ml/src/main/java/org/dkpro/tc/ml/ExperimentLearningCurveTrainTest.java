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
package org.dkpro.tc.ml;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.reporting.Report;
import org.dkpro.lab.reporting.ReportBase;
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
import org.dkpro.tc.ml.base.ShallowLearningExperiment_ImplBase;
import org.dkpro.tc.ml.report.BasicResultReport;
import org.dkpro.tc.ml.report.shallowlearning.InnerReport;

/**
 * Runs a learning curve experiment, which uses a fixed test set. The training
 * data is split into N folds and all fold-variations will be used to test
 * against the fixed test set.This experiment type should be used if testing
 * against a fixed development or test set. If no fixed test set is required use
 * {@link ExperimentLearningCurve} instead.
 */
public class ExperimentLearningCurveTrainTest extends ShallowLearningExperiment_ImplBase implements Constants {

	protected InitTask initTaskTrain;
	protected InitTask initTaskTest;
	protected OutcomeCollectionTask collectionTask;
	protected MetaInfoTask metaTask;
	protected ExtractFeaturesTask featuresTrainTask;
	protected ExtractFeaturesTask featuresTestTask;
	protected TaskBase testTask;
	private int numFolds;
	private int limitPerStage = -1;

	public ExperimentLearningCurveTrainTest() {/* needed for Groovy */
	}

	/**
	 * Creates a train test experiment that runs a learning curve in which an
	 * increasing amount of training data is added to the training data set. For
	 * avoiding using of all possible training set permutations:
	 * {@see #ExperimentLearningCurveTrainTest(String, int, int)}, which allows
	 * setting a limit
	 * 
	 * @param aExperimentName Name of the experiment
	 * @param numFolds        Number of folds
	 * @throws TextClassificationException In case of an error
	 */
	public ExperimentLearningCurveTrainTest(String aExperimentName, int numFolds) throws TextClassificationException {
		this.numFolds = numFolds;
		this.limitPerStage = -1;
		setExperimentName(aExperimentName);
		// set name of overall batch task
		setType("Evaluation-" + experimentName);
		setAttribute(TC_TASK_TYPE, TcTaskType.EVALUATION.toString());
	}

	/**
	 * Creates a train test experiment that runs a learning curve in which an
	 * increasing amount of training data is added to the training data set.
	 * 
	 * @param aExperimentName Name of the experiment
	 * @param numFolds        Number of folds
	 * @param limitPerStage   Limits the number of training runs per stage. This is
	 *                        an optional parameter. If not set all training set
	 *                        permutations will be used once for training, which is
	 *                        a rather expensive operation. Must be a non-zero,
	 *                        positive integer value smaller than numFolds.
	 * @throws TextClassificationException In case of an error
	 */
	public ExperimentLearningCurveTrainTest(String aExperimentName, int numFolds, int limitPerStage)
			throws TextClassificationException {
		this.numFolds = numFolds;
		this.limitPerStage = limitPerStage;
		setExperimentName(aExperimentName);
		// set name of overall batch task
		setType("Evaluation-" + experimentName);
		setAttribute(TC_TASK_TYPE, TcTaskType.EVALUATION.toString());
	}

	/**
	 * Initializes the experiment. This is called automatically before execution.
	 * It's not done directly in the constructor, because we want to be able to use
	 * setters instead of the arguments in the constructor.
	 * 
	 */
	@Override
	protected void init() {
		if (experimentName == null) {
			throw new IllegalStateException("You must set an experiment name");
		}

		// init the train part of the experiment
		initTaskTrain = new InitTask();
		initTaskTrain.setPreprocessing(getPreprocessing());
		initTaskTrain.setOperativeViews(operativeViews);
		initTaskTrain.setTesting(false);
		initTaskTrain.setType(initTaskTrain.getType() + "-Train-" + experimentName);
		initTaskTrain.setAttribute(TC_TASK_TYPE, TcTaskType.INIT_TRAIN.toString());

		// inner batch task (carried out numFolds times)
		DefaultBatchTask crossValidationTask = new DefaultBatchTask() {
			@Discriminator(name = DIM_FEATURE_MODE)
			private String featureMode;

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

				if (fileNames.length < numFolds) {
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
				LearningCurveDimBundleFixedTestSet foldDim = getFoldDim(fileNames);
				Dimension<File> filesRootDim = Dimension.create(DIM_FILES_ROOT, xmiPathRoot);

				ParameterSpace pSpace = new ParameterSpace(foldDim, filesRootDim);
				setParameterSpace(pSpace);
			}

			/**
			 * 
			 * @param fileNames the file names
			 * @return fold dimension bundle
			 */
			protected LearningCurveDimBundleFixedTestSet getFoldDim(String[] fileNames) {
				return new LearningCurveDimBundleFixedTestSet("files", Dimension.create("", fileNames), numFolds,
						limitPerStage);
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
					File outputFolder = FoldUtil.createMinimalSplit(xmiPathRoot.getAbsolutePath(), numFolds,
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

				if (numCas < numFolds) {
					throw new IllegalStateException(
							"Not enough TextClassificationUnits found to create at least [" + numFolds + "] folds");
				}
			}
		};

		// init the test part of the experiment
		initTaskTest = new InitTask();
		initTaskTest.setTesting(true);
		initTaskTest.setPreprocessing(getPreprocessing());
		initTaskTest.setOperativeViews(operativeViews);
		initTaskTest.setType(initTaskTest.getType() + "-Test-" + experimentName);
		initTaskTest.setAttribute(TC_TASK_TYPE, TcTaskType.INIT_TEST.toString());

		collectionTask = new OutcomeCollectionTask();
		collectionTask.setType(collectionTask.getType() + "-" + experimentName);
		collectionTask.setAttribute(TC_TASK_TYPE, TcTaskType.COLLECTION.toString());
		collectionTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN);
		collectionTask.addImport(initTaskTest, InitTask.OUTPUT_KEY_TEST);

		// get some meta data depending on the whole document collection that we need
		// for training
		metaTask = new MetaInfoTask();
		metaTask.setOperativeViews(operativeViews);
		metaTask.setType(metaTask.getType() + "-" + experimentName);

		metaTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN, MetaInfoTask.INPUT_KEY);
		metaTask.setAttribute(TC_TASK_TYPE, TcTaskType.META.toString());

		// feature extraction on training data
		featuresTrainTask = new ExtractFeaturesTask();
		featuresTrainTask.setType(featuresTrainTask.getType() + "-Train-" + experimentName);
		featuresTrainTask.setTesting(false);
		featuresTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);
		featuresTrainTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN, ExtractFeaturesTask.INPUT_KEY);
		featuresTrainTask.addImport(collectionTask, OutcomeCollectionTask.OUTPUT_KEY,
				ExtractFeaturesTask.COLLECTION_INPUT_KEY);
		featuresTrainTask.setAttribute(TC_TASK_TYPE, TcTaskType.FEATURE_EXTRACTION_TRAIN.toString());

		// feature extraction on test data
		featuresTestTask = new ExtractFeaturesTask();
		featuresTestTask.setType(featuresTestTask.getType() + "-Test-" + experimentName);
		featuresTestTask.setTesting(true);
		featuresTestTask.addImport(metaTask, MetaInfoTask.META_KEY);
		featuresTestTask.addImport(initTaskTest, InitTask.OUTPUT_KEY_TEST, ExtractFeaturesTask.INPUT_KEY);
		featuresTestTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY);
		featuresTestTask.addImport(collectionTask, OutcomeCollectionTask.OUTPUT_KEY,
				ExtractFeaturesTask.COLLECTION_INPUT_KEY);
		featuresTestTask.setAttribute(TC_TASK_TYPE, TcTaskType.FEATURE_EXTRACTION_TEST.toString());

		// test task operating on the models of the feature extraction train and test
		// tasks
		List<ReportBase> reports = new ArrayList<>();
		reports.add(new BasicResultReport());

		testTask = new DKProTcShallowTestTask(featuresTrainTask, featuresTestTask, collectionTask, reports,
				experimentName);
		testTask.setType(testTask.getType() + "-" + experimentName);
		testTask.setAttribute(TC_TASK_TYPE, TcTaskType.FACADE_TASK.toString());

		if (innerReports != null) {
			for (Class<? extends Report> report : innerReports) {
				testTask.addReport(report);
			}
		}

		crossValidationTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN);
		crossValidationTask.addImport(initTaskTest, InitTask.OUTPUT_KEY_TEST);
		crossValidationTask.setType(crossValidationTask.getType() + "-" + experimentName);
		crossValidationTask.addTask(collectionTask);
		crossValidationTask.addTask(metaTask);
		crossValidationTask.addTask(featuresTrainTask);
		crossValidationTask.addTask(featuresTestTask);
		crossValidationTask.addTask(testTask);
		crossValidationTask.setExecutionPolicy(ExecutionPolicy.USE_EXISTING);
		// report of the inner batch task (sums up results for the folds)
		// we want to re-use the old CV report, we need to collect the evaluation.bin
		// files from
		// the test task here (with another report)
		crossValidationTask.addReport(InnerReport.class);
		crossValidationTask.setAttribute(TC_TASK_TYPE, TcTaskType.CROSS_VALIDATION.toString());

		// DKPro Lab issue 38: must be added as *first* task
		addTask(initTaskTrain);
		addTask(initTaskTest);
		addTask(crossValidationTask);
	}
}
