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

import org.dkpro.lab.reporting.Report;
import org.dkpro.lab.task.impl.TaskBase;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.core.task.CollectionTask;
import org.dkpro.tc.core.task.ExtractFeaturesTask;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.task.MetaInfoTask;
import org.dkpro.tc.ml.base.ShallowLearningExperiment_ImplBase;
import org.dkpro.tc.ml.report.BasicResultReport;
import org.dkpro.tc.ml.report.TcTaskType;

/**
 * Train-Test setup
 * 
 */
public class ExperimentTrainTest
    extends ShallowLearningExperiment_ImplBase
{

    protected InitTask initTaskTrain;
    protected InitTask initTaskTest;
    protected CollectionTask collectionTask;
    protected MetaInfoTask metaTask;
    protected ExtractFeaturesTask featuresTrainTask;
    protected ExtractFeaturesTask featuresTestTask;
    protected TaskBase testTask;

    public ExperimentTrainTest()
    {/* needed for Groovy */
    }

    /**
     * Preconfigured train-test setup.
     * 
     */
    public ExperimentTrainTest(String aExperimentName, Class<? extends TcShallowLearningAdapter> mlAdapter)
            throws TextClassificationException
    {
        setExperimentName(aExperimentName);
        setMachineLearningAdapter(mlAdapter);
        // set name of overall batch task
        setType("Evaluation-" + experimentName);
        setAttribute(TC_TASK_TYPE, TcTaskType.EVALUATION.toString());
    }

    /**
     * Initializes the experiment. This is called automatically before execution. It's not done
     * directly in the constructor, because we want to be able to use setters instead of the
     * arguments in the constructor.
     * 
     */
    @Override
    protected void init()
    {
        if (experimentName == null)

        {
            throw new IllegalStateException(
                    "You must set an experiment name");
        }

        // init the train part of the experiment
        initTaskTrain = new InitTask();
        initTaskTrain.setMlAdapter(mlAdapter);
        initTaskTrain.setPreprocessing(getPreprocessing());
        initTaskTrain.setOperativeViews(operativeViews);
        initTaskTrain.setTesting(false);
        initTaskTrain.setType(initTaskTrain.getType() + "-Train-" + experimentName);
        initTaskTrain.setAttribute(TC_TASK_TYPE, TcTaskType.INIT_TRAIN.toString());

        // init the test part of the experiment
        initTaskTest = new InitTask();
        initTaskTest.setTesting(true);
        initTaskTest.setMlAdapter(mlAdapter);
        initTaskTest.setPreprocessing(getPreprocessing());
        initTaskTest.setOperativeViews(operativeViews);
        initTaskTest.setType(initTaskTest.getType() + "-Test-" + experimentName);
        initTaskTest.setAttribute(TC_TASK_TYPE, TcTaskType.INIT_TEST.toString());
        
		collectionTask = new CollectionTask();
		collectionTask.setType(collectionTask.getType() + "-" + experimentName);
		collectionTask.setAttribute(TC_TASK_TYPE, TcTaskType.COLLECTION.toString());
		collectionTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN);
		collectionTask.addImport(initTaskTest, InitTask.OUTPUT_KEY_TEST);

        // get some meta data depending on the whole document collection that we need for training
        metaTask = new MetaInfoTask();
        metaTask.setOperativeViews(operativeViews);
        metaTask.setType(metaTask.getType() + "-" + experimentName);

        metaTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN,
                MetaInfoTask.INPUT_KEY);
        metaTask.setAttribute(TC_TASK_TYPE, TcTaskType.META.toString());

        // feature extraction on training data
        featuresTrainTask = new ExtractFeaturesTask();
        featuresTrainTask.setType(featuresTrainTask.getType() + "-Train-" + experimentName);
        featuresTrainTask.setMlAdapter(mlAdapter);
        featuresTrainTask.setTesting(false);
        featuresTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);
        featuresTrainTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN,
                ExtractFeaturesTask.INPUT_KEY);
        featuresTrainTask.addImport(collectionTask, CollectionTask.OUTPUT_KEY,
                ExtractFeaturesTask.COLLECTION_INPUT_KEY);
        featuresTrainTask.setAttribute(TC_TASK_TYPE, TcTaskType.FEATURE_EXTRACTION_TRAIN.toString());

        // feature extraction on test data
        featuresTestTask = new ExtractFeaturesTask();
        featuresTestTask.setType(featuresTestTask.getType() + "-Test-" + experimentName);
        featuresTestTask.setMlAdapter(mlAdapter);
        featuresTestTask.setTesting(true);
        featuresTestTask.addImport(metaTask, MetaInfoTask.META_KEY);
        featuresTestTask.addImport(initTaskTest, InitTask.OUTPUT_KEY_TEST,
                ExtractFeaturesTask.INPUT_KEY);
        featuresTestTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY);
        featuresTestTask.addImport(collectionTask, CollectionTask.OUTPUT_KEY,
                ExtractFeaturesTask.COLLECTION_INPUT_KEY);
        featuresTestTask.setAttribute(TC_TASK_TYPE, TcTaskType.FEATURE_EXTRACTION_TEST.toString());

        // test task operating on the models of the feature extraction train and test tasks
        testTask = mlAdapter.getTestTask();
        testTask.setType(testTask.getType() + "-" + experimentName);
        testTask.setAttribute(TC_TASK_TYPE, TcTaskType.MACHINE_LEARNING_ADAPTER.toString());

        if (innerReports != null) {
            for (Class<? extends Report> report : innerReports) {
                testTask.addReport(report);
            }
        }

        // always add OutcomeIdReport
        testTask.addReport(mlAdapter.getOutcomeIdReportClass());
        testTask.addReport(BasicResultReport.class);

        testTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
                Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
        testTask.addImport(featuresTestTask, ExtractFeaturesTask.OUTPUT_KEY,
                Constants.TEST_TASK_INPUT_KEY_TEST_DATA);
        testTask.addImport(collectionTask, CollectionTask.OUTPUT_KEY, Constants.OUTCOMES_INPUT_KEY);

        // DKPro Lab issue 38: must be added as *first* task
        addTask(initTaskTrain);
        addTask(initTaskTest);
        addTask(collectionTask);
        addTask(metaTask);
        addTask(featuresTrainTask);
        addTask(featuresTestTask);
        addTask(testTask);
    }
}
