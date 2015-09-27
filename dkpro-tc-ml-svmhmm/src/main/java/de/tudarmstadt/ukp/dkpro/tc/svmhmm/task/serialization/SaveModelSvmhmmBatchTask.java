/**
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.tc.svmhmm.task.serialization;

import java.io.File;
import java.util.List;
import java.util.SortedSet;

import org.apache.commons.collections.BidiMap;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.DefaultBatchTask;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.InitTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.core.util.SaveModelUtils;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.SVMHMMAdapter;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.task.SVMHMMTestTask;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.util.SVMHMMUtils;

/**
 * Save model batch
 * 
 */
public class SaveModelSvmhmmBatchTask extends DefaultBatchTask {
	private String experimentName;
	private AnalysisEngineDescription preprocessingPipeline;
	private List<String> operativeViews;
	private TCMachineLearningAdapter mlAdapter;
	private File outputFolder;

	// tasks
	private InitTask initTaskTrain;
	private MetaInfoTask metaTask;
	private ExtractFeaturesTask featuresTrainTask;
	private ModelSerializationDescription saveModelTask;

	public SaveModelSvmhmmBatchTask() {/* needed for Groovy */
	}

	public SaveModelSvmhmmBatchTask(String aExperimentName, File outputFolder,
			Class<? extends TCMachineLearningAdapter> mlAdapter, AnalysisEngineDescription preprocessingPipeline)
					throws TextClassificationException {
		setExperimentName(aExperimentName);
		setPreprocessingPipeline(preprocessingPipeline);
		// set name of overall batch task
		setType("Evaluation-" + experimentName);
		setTcMachineLearningAdapter(mlAdapter);
		setOutputFolder(outputFolder);
	}

	/**
	 * Initializes the experiment. This is called automatically before
	 * execution. It's not done directly in the constructor, because we want to
	 * be able to use setters instead of the three-argument constructor.
	 * 
	 * @throws IllegalStateException
	 *             if not all necessary arguments have been set.
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private void init() {
		if (experimentName == null || preprocessingPipeline == null)

		{
			throw new IllegalStateException("You must set Experiment Name and Aggregate.");
		}

		// init the train part of the experiment
		initTaskTrain = new InitTask();
		initTaskTrain.setMlAdapter(mlAdapter);
		initTaskTrain.setPreprocessing(preprocessingPipeline);
		initTaskTrain.setOperativeViews(operativeViews);
		initTaskTrain.setTesting(false);
		initTaskTrain.setType(initTaskTrain.getType() + "-Train-" + experimentName);

		// get some meta data depending on the whole document collection that we
		// need for training
		metaTask = new MetaInfoTask();
		metaTask.setOperativeViews(operativeViews);
		metaTask.setType(metaTask.getType() + "-" + experimentName);

		metaTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN, MetaInfoTask.INPUT_KEY);

		// feature extraction on training data
		featuresTrainTask = new ExtractFeaturesTask();
		featuresTrainTask.setType(featuresTrainTask.getType() + "-Train-" + experimentName);
		featuresTrainTask.setMlAdapter(mlAdapter);
		featuresTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);
		featuresTrainTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN, ExtractFeaturesTask.INPUT_KEY);

		// feature extraction and prediction on test data
		saveModelTask = new ModelSerializationDescription();
		saveModelTask.setType(saveModelTask.getType() + "-" + experimentName);
		saveModelTask.addImport(metaTask, MetaInfoTask.META_KEY);
		saveModelTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
				Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
		saveModelTask.setAndCreateOutputFolder(outputFolder);

		// DKPro Lab issue 38: must be added as *first* task
		addTask(initTaskTrain);
		addTask(metaTask);
		addTask(featuresTrainTask);
		addTask(saveModelTask);
	}

	@Override
	public void initialize(TaskContext aContext) {
		super.initialize(aContext);
		init();
	}

	public void setExperimentName(String experimentName) {
		this.experimentName = experimentName;
	}

	public void setPreprocessingPipeline(AnalysisEngineDescription preprocessingPipeline) {
		this.preprocessingPipeline = preprocessingPipeline;
	}

	public void setOperativeViews(List<String> operativeViews) {
		this.operativeViews = operativeViews;
	}

	public void setTcMachineLearningAdapter(Class<? extends TCMachineLearningAdapter> mlAdapter)
			throws TextClassificationException {
		try {
			this.mlAdapter = mlAdapter.newInstance();
		} catch (InstantiationException e) {
			throw new TextClassificationException(e);
		} catch (IllegalAccessException e) {
			throw new TextClassificationException(e);
		}
	}

	public void setOutputFolder(File outputFolder) {
		this.outputFolder = outputFolder;
	}
}

class ModelSerializationDescription extends ExecutableTaskBase implements Constants {

	@Discriminator
	protected List<Object> pipelineParameters;
	@Discriminator
	protected List<String> featureSet;
	@Discriminator
	private String[] classificationArguments;

	private File outputFolder;

	public void setAndCreateOutputFolder(File outputFolder) {
		this.outputFolder = outputFolder;
		outputFolder.mkdirs();
	}

	@Override
	public void execute(TaskContext aContext) throws Exception {

		trainAndStoreModel(aContext);
		//TODO: persist classificationArguments
		SaveModelUtils.writeFeatureInformation(outputFolder, featureSet);
		SaveModelUtils.writeFeatureClassFiles(outputFolder, featureSet);
		SaveModelUtils.writeModelParameters(aContext, outputFolder, featureSet, pipelineParameters);
		SaveModelUtils.writeModelAdapterInformation(outputFolder, SVMHMMAdapter.class.getName());

	}

	private void trainAndStoreModel(TaskContext aContext) throws Exception {
		
		File trainingDataStorage = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
				StorageService.AccessMode.READONLY);

		// file name of the data; THE FILES HAVE SAME NAME FOR BOTH TRAINING AND
		// TESTING!!!!!!
		String fileName = new SVMHMMAdapter()
				.getFrameworkFilename(TCMachineLearningAdapter.AdapterNameEntries.featureVectorsFile);

		File trainingFile = new File(trainingDataStorage, fileName);

		SortedSet<String> outcomeLabels = SVMHMMUtils.extractOutcomeLabelsFromFeatureVectorFiles(trainingFile);
		BidiMap labelsToIntegersMapping = SVMHMMUtils.mapVocabularyToIntegers(outcomeLabels);

		// // save mapping to file
		File mappingFile = new File(outputFolder.toString() + "/" + SVMHMMUtils.LABELS_TO_INTEGERS_MAPPING_FILE_NAME);
		SVMHMMUtils.saveMapping(labelsToIntegersMapping, mappingFile);

		File augmentedTrainingFile = SVMHMMUtils.replaceLabelsWithIntegers(trainingFile, labelsToIntegersMapping);

		File classifier = new File(outputFolder.getAbsolutePath() + "/" + MODEL_CLASSIFIER);
		// train the model
		new SVMHMMTestTask().trainModel(classifier, augmentedTrainingFile);
	}

}