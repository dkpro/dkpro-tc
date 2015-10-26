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
package de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.serialization;

import java.io.File;

import de.tudarmstadt.ukp.dkpro.lab.reporting.Report;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.TaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.InitTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.ml.Experiment_ImplBase;

/**
 * Train-Test setup
 * 
 */
public class SaveModelCRFSuiteTrainTestTask
    extends Experiment_ImplBase
{

    private InitTask initTaskTrain;
    private InitTask initTaskTest;
    private MetaInfoTask metaTask;
    private ExtractFeaturesTask featuresTrainTask;
    private ExtractFeaturesTask featuresTestTask;
    private TaskBase testTask;
    private ModelSerializationDescription saveModelTask;
    private File outputFolder;

    public SaveModelCRFSuiteTrainTestTask()
    {/* needed for Groovy */
    }

    /**
     * Preconfigured train-test setup that also stores the trained classifier.
     * 
     * @param aExperimentName
     *            name of the experiment
     */
    public SaveModelCRFSuiteTrainTestTask(String aExperimentName,  File outputFolder, Class<? extends TCMachineLearningAdapter> mlAdapter)
            throws TextClassificationException
    {
        setExperimentName(aExperimentName);
        setMachineLearningAdapter(mlAdapter);
        // set name of overall batch task
        setType("Evaluation-" + experimentName);
        setOutputFolder(outputFolder);
        setTcMachineLearningAdapter(mlAdapter);
        setOutputFolder(outputFolder);
    }

    /**
     * Initializes the experiment. This is called automatically before execution. It's not done
     * directly in the constructor, because we want to be able to use setters instead of the
     * arguments in the constructor.
     * 
     * @throws IllegalStateException
     *             if not all necessary arguments have been set.
     */
    protected void init()
    {
        if (experimentName == null || getPreprocessing() == null)

        {
            throw new IllegalStateException(
                    "You must set Experiment Name, DataWriter and Aggregate.");
        }

        // init the train part of the experiment
        initTaskTrain = new InitTask();
        initTaskTrain.setType(initTaskTrain.getType() + "-Train-" + experimentName);
        initTaskTrain.setMlAdapter(mlAdapter);
        initTaskTrain.setPreprocessing(getPreprocessing());
        initTaskTrain.setOperativeViews(operativeViews);
        initTaskTrain.setTesting(false);
        initTaskTrain.setDropInvalidCases(dropInvalidCases);
        initTaskTrain.setType(initTaskTrain.getType() + "-Train-" + experimentName);

        // init the test part of the experiment
        initTaskTest = new InitTask();
        initTaskTest.setTesting(true);
        initTaskTest.setMlAdapter(mlAdapter);
        initTaskTest.setPreprocessing(getPreprocessing());
        initTaskTest.setOperativeViews(operativeViews);
        initTaskTest.setDropInvalidCases(dropInvalidCases);
        initTaskTest.setType(initTaskTest.getType() + "-Test-" + experimentName);

        // get some meta data depending on the whole document collection that we need for training
        metaTask = new MetaInfoTask();
        metaTask.setOperativeViews(operativeViews);
        metaTask.setType(metaTask.getType() + "-" + experimentName);

        metaTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN,
                MetaInfoTask.INPUT_KEY);

        // feature extraction on training data
        featuresTrainTask = new ExtractFeaturesTask();
        featuresTrainTask.setType(featuresTrainTask.getType() + "-Train-" + experimentName);
        featuresTrainTask.setMlAdapter(mlAdapter);
        featuresTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);
        featuresTrainTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN,
                ExtractFeaturesTask.INPUT_KEY);

        // feature extraction on test data
        featuresTestTask = new ExtractFeaturesTask();
        featuresTestTask.setType(featuresTestTask.getType() + "-Test-" + experimentName);
        featuresTestTask.setMlAdapter(mlAdapter);
        featuresTestTask.addImport(metaTask, MetaInfoTask.META_KEY);
        featuresTestTask.addImport(initTaskTest, InitTask.OUTPUT_KEY_TEST,
                ExtractFeaturesTask.INPUT_KEY);
        featuresTestTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY);

        // test task operating on the models of the feature extraction train and test tasks
        testTask = mlAdapter.getTestTask();
        testTask.setType(testTask.getType() + "-" + experimentName);

        if (innerReports != null) {
            for (Class<? extends Report> report : innerReports) {
                testTask.addReport(report);
            }
        }

        // always add default report
        testTask.addReport(mlAdapter.getClassificationReportClass());
        // always add OutcomeIdReport
        testTask.addReport(mlAdapter.getOutcomeIdReportClass());

        testTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
                Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
        testTask.addImport(featuresTestTask, ExtractFeaturesTask.OUTPUT_KEY,
                Constants.TEST_TASK_INPUT_KEY_TEST_DATA);
        
        
        // store the model
        saveModelTask = new ModelSerializationDescription();
        saveModelTask.setType(saveModelTask.getType() + "-" + experimentName);
        saveModelTask.addImport(metaTask, MetaInfoTask.META_KEY);
        saveModelTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
                Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
        saveModelTask.setAndCreateOutputFolder(outputFolder);

        // DKPro Lab issue 38: must be added as *first* task
        addTask(initTaskTrain);
        addTask(initTaskTest);
        addTask(metaTask);
        addTask(featuresTrainTask);
        addTask(featuresTestTask);
        addTask(testTask);
        addTask(saveModelTask);
    }
    public void setOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
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
}
