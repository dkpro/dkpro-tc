/**
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka.task.serialization;

import java.io.File;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.InitTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.ml.Experiment_ImplBase;

/**
 * Save model batch
 * 
 */
public class SaveModelWekaBatchTask
    extends Experiment_ImplBase
{

    private File outputFolder;

    // tasks
    private InitTask initTaskTrain;
    private MetaInfoTask metaTask;
    private ExtractFeaturesTask featuresTrainTask;
    private ModelSerializationTask saveModelTask;

    public SaveModelWekaBatchTask()
    {/* needed for Groovy */
    }

    public SaveModelWekaBatchTask(String aExperimentName,
            Class<? extends TCMachineLearningAdapter> mlAdapter, File outputFolder)
        throws TextClassificationException
    {
        setExperimentName(aExperimentName);
        // set name of overall batch task
        setType("Evaluation-" + experimentName);
        setMachineLearningAdapter(mlAdapter);
        setOutputFolder(outputFolder);
    }

    @Override
    public void initialize(TaskContext aContext)
    {
        super.initialize(aContext);
        init();
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
        initTaskTrain = new InitTask();
        initTaskTrain.setMlAdapter(mlAdapter);
        initTaskTrain.setPreprocessing(getPreprocessing());
        initTaskTrain.setOperativeViews(operativeViews);
        initTaskTrain.setTesting(false);
        initTaskTrain.setType(initTaskTrain.getType() + "-Train-" + experimentName);

        // get some meta data depending on the whole document collection that we need for training
        metaTask = new MetaInfoTask();
        metaTask.setOperativeViews(operativeViews);
        metaTask.setType(metaTask.getType() + "-" + experimentName);

        metaTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN, MetaInfoTask.INPUT_KEY);

        // feature extraction on training data
        featuresTrainTask = new ExtractFeaturesTask();
        featuresTrainTask.setType(featuresTrainTask.getType() + "-Train-" + experimentName);
        featuresTrainTask.setMlAdapter(mlAdapter);
        featuresTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);
        featuresTrainTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN,
                ExtractFeaturesTask.INPUT_KEY);

        // feature extraction and prediction on test data
        saveModelTask = new ModelSerializationTask();
        saveModelTask.setType(saveModelTask.getType() + "-" + experimentName);
        saveModelTask.addImport(metaTask, MetaInfoTask.META_KEY);
        saveModelTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
                Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
        saveModelTask.setOutputFolder(outputFolder);

        // DKPro Lab issue 38: must be added as *first* task
        addTask(initTaskTrain);
        addTask(metaTask);
        addTask(featuresTrainTask);
        addTask(saveModelTask);
    }

    public void setOutputFolder(File outputFolder)
    {
        this.outputFolder = outputFolder;
    }
}