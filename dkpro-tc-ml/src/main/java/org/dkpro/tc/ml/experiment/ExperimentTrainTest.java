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

import java.util.ArrayList;
import java.util.List;

import org.dkpro.lab.reporting.Report;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.impl.TaskBase;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.task.DKProTcShallowTestTask;
import org.dkpro.tc.core.task.ExtractFeaturesTask;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.task.MetaInfoTask;
import org.dkpro.tc.core.task.OutcomeCollectionTask;
import org.dkpro.tc.core.task.TcTaskType;
import org.dkpro.tc.ml.base.Experiment_ImplBase;
import org.dkpro.tc.ml.report.BasicResultReport;

/**
 * Train-Test setup
 * 
 */
public class ExperimentTrainTest
    extends Experiment_ImplBase
{

    protected InitTask initTaskTrain;
    protected InitTask initTaskTest;
    protected OutcomeCollectionTask collectionTask;
    protected MetaInfoTask metaTask;
    protected ExtractFeaturesTask featuresTrainTask;
    protected ExtractFeaturesTask featuresTestTask;
    protected TaskBase testTask;

    public ExperimentTrainTest()
    {/* needed for Groovy */
    }

    /*
     * Preconfigured train-test setup.
     */
    public ExperimentTrainTest(String aExperimentName) throws TextClassificationException
    {
        setExperimentName(aExperimentName);
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

        // get some meta data depending on the whole document collection that we need for training
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
        featuresTrainTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN,
                ExtractFeaturesTask.INPUT_KEY);
        featuresTrainTask.addImport(collectionTask, OutcomeCollectionTask.OUTPUT_KEY,
                ExtractFeaturesTask.COLLECTION_INPUT_KEY);
        featuresTrainTask.setAttribute(TC_TASK_TYPE,
                TcTaskType.FEATURE_EXTRACTION_TRAIN.toString());

        // feature extraction on test data
        featuresTestTask = new ExtractFeaturesTask();
        featuresTestTask.setType(featuresTestTask.getType() + "-Test-" + experimentName);
        featuresTestTask.setTesting(true);
        featuresTestTask.addImport(metaTask, MetaInfoTask.META_KEY);
        featuresTestTask.addImport(initTaskTest, InitTask.OUTPUT_KEY_TEST,
                ExtractFeaturesTask.INPUT_KEY);
        featuresTestTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY);
        featuresTestTask.addImport(collectionTask, OutcomeCollectionTask.OUTPUT_KEY,
                ExtractFeaturesTask.COLLECTION_INPUT_KEY);
        featuresTestTask.setAttribute(TC_TASK_TYPE, TcTaskType.FEATURE_EXTRACTION_TEST.toString());

        // test task operating on the models of the feature extraction train and test tasks
        List<Report> reports = new ArrayList<>();
        reports.add(new BasicResultReport());

        testTask = new DKProTcShallowTestTask(featuresTrainTask, featuresTestTask, collectionTask,
                reports, experimentName);
        testTask.setType(testTask.getType() + "-" + experimentName);
        testTask.setAttribute(TC_TASK_TYPE, TcTaskType.FACADE_TASK.toString());

        if (innerReports != null) {
            for (Report report : innerReports) {
                testTask.addReport(report);
            }
        }

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
