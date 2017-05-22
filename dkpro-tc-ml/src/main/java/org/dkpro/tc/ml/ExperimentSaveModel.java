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
package org.dkpro.tc.ml;

import java.io.File;
import java.util.List;

import org.dkpro.lab.engine.TaskContext;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.task.CollectionTask;
import org.dkpro.tc.core.task.ExtractFeaturesTask;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.task.MetaInfoTask;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.ml.report.TcTaskType;

/**
 * Save model batch
 * 
 */
public class ExperimentSaveModel
    extends Experiment_ImplBase
{
    private File outputFolder;

    // tasks
    private InitTask initTask;
    private CollectionTask collectionTask;
    private MetaInfoTask metaTask;
    private ExtractFeaturesTask featuresTrainTask;
    private ModelSerializationTask saveModelTask;

    public ExperimentSaveModel()
    {/* needed for Groovy */
    }

    public ExperimentSaveModel(String aExperimentName,
            Class<? extends TCMachineLearningAdapter> mlAdapter, File outputFolder)
        throws TextClassificationException
    {
        setExperimentName(aExperimentName);
        // set name of overall batch task
        setType("Evaluation-" + experimentName);
        setTcMachineLearningAdapter(mlAdapter);
        setOutputFolder(outputFolder);
    }

    /**
     * Initializes the experiment. This is called automatically before execution. It's not done
     * directly in the constructor, because we want to be able to use setters instead of the
     * three-argument constructor.
     * 
     * @throws IllegalStateException
     *             if not all necessary arguments have been set.
     */
    protected void init()
    {
        if (experimentName == null)

        {
            throw new IllegalStateException("You must set an experiment name");
        }

        // init the train part of the experiment
        initTask = new InitTask();
        initTask.setMlAdapter(mlAdapter);
        initTask.setPreprocessing(getPreprocessing());
        initTask.setOperativeViews(operativeViews);
        initTask.setTesting(false);
        initTask.setType(initTask.getType() + "-Train-" + experimentName);
        initTask.setAttribute(TC_TASK_TYPE, TcTaskType.INIT_TRAIN.toString());
        
        collectionTask = new CollectionTask();
		collectionTask.setType(collectionTask.getType() + "-" + experimentName);
		collectionTask.setAttribute(TC_TASK_TYPE, TcTaskType.COLLECTION.toString());
		collectionTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN);

        metaTask = new MetaInfoTask();
        metaTask.setOperativeViews(operativeViews);
        metaTask.setType(metaTask.getType() + "-" + experimentName);
        metaTask.setAttribute(TC_TASK_TYPE, TcTaskType.META.toString());

        metaTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN, MetaInfoTask.INPUT_KEY);

        // feature extraction on training data
        featuresTrainTask = new ExtractFeaturesTask();
        featuresTrainTask.setType(featuresTrainTask.getType() + "-Train-" + experimentName);
        featuresTrainTask.setMlAdapter(mlAdapter);
        featuresTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);
        featuresTrainTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN,
                ExtractFeaturesTask.INPUT_KEY);
        featuresTrainTask.setAttribute(TC_TASK_TYPE, TcTaskType.FEATURE_EXTRACTION_TRAIN.toString());
        featuresTrainTask.addImport(collectionTask, CollectionTask.OUTPUT_KEY,
                ExtractFeaturesTask.COLLECTION_INPUT_KEY);

        // feature extraction and prediction on test data
		try {
			saveModelTask = mlAdapter.getSaveModelTask().newInstance();
			saveModelTask.setType(saveModelTask.getType() + "-" + experimentName);
			saveModelTask.addImport(metaTask, MetaInfoTask.META_KEY);
			saveModelTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
					Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
			saveModelTask.addImport(collectionTask, CollectionTask.OUTPUT_KEY, Constants.OUTCOMES_INPUT_KEY);
			saveModelTask.setOutputFolder(outputFolder);
			saveModelTask.setAttribute(TC_TASK_TYPE, TcTaskType.MACHINE_LEARNING_ADAPTER.toString());

		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

        // DKPro Lab issue 38: must be added as *first* task
        addTask(initTask);
        addTask(collectionTask);
        addTask(metaTask);
        addTask(featuresTrainTask);
        addTask(saveModelTask);
    }

    @Override
    public void initialize(TaskContext aContext)
    {
        super.initialize(aContext);
        init();
    }

    public void setExperimentName(String experimentName)
    {
        this.experimentName = experimentName;
    }

    public void setOperativeViews(List<String> operativeViews)
    {
        this.operativeViews = operativeViews;
    }

    public void setTcMachineLearningAdapter(Class<? extends TCMachineLearningAdapter> mlAdapter)
        throws TextClassificationException
    {
        try {
            this.mlAdapter = mlAdapter.newInstance();
        }
        catch (InstantiationException e) {
            throw new TextClassificationException(e);
        }
        catch (IllegalAccessException e) {
            throw new TextClassificationException(e);
        }
    }

    public void setOutputFolder(File outputFolder)
    {
        this.outputFolder = outputFolder;
    }
}
