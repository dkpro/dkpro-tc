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
import org.dkpro.tc.core.ml.TcDeepLearningAdapter;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.task.InitTaskDeep;
import org.dkpro.tc.core.task.deep.EmbeddingTask;
import org.dkpro.tc.core.task.deep.PreparationTask;
import org.dkpro.tc.core.task.deep.VectorizationTask;
import org.dkpro.tc.ml.report.DeeplearningBasicResultReport;
import org.dkpro.tc.ml.report.TcTaskType;

/**
 * Train-Test setup
 */
public class DeepLearningExperimentTrainTest
    extends Experiment_ImplBase
{

    protected InitTaskDeep initTaskTrain;
    protected InitTaskDeep initTaskTest;
    protected PreparationTask preparationTask;
    protected EmbeddingTask embeddingTask;
    protected VectorizationTask vectorizationTrainTask;
    protected VectorizationTask vectorizationTestTask;
    protected TaskBase learningTask;

    private TcDeepLearningAdapter mlDeepLearningAdapter;
    // FIXME: belongs into super class - requires interface changes - will be huge pain to merge
    // that
    // because of work on other branches - to be fixed after merge into master

    public DeepLearningExperimentTrainTest()
    {/* needed for Groovy */
    }

    /**
     * Preconfigured train-test setup.
     */
    public DeepLearningExperimentTrainTest(String aExperimentName,
            Class<? extends TcDeepLearningAdapter> mlAdapter)
                throws TextClassificationException
    {
        try {
            mlDeepLearningAdapter = mlAdapter.newInstance();
        }
        catch (Exception e) {
            throw new TextClassificationException(e);
        }
        setExperimentName(aExperimentName);
        // set name of overall batch task
        setType("Evaluation-" + experimentName);
        setAttribute(TC_TASK_TYPE, TcTaskType.EVALUATION.toString());
    }

    // TODO: Introduce new attributes or reuse existing ? Becomes confusing with
    // some being used for DL setups only

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
            throw new IllegalStateException("You must set an experiment name");
        }

        // init the train part of the experiment
        initTaskTrain = new InitTaskDeep();
        initTaskTrain.setPreprocessing(getPreprocessing());
        initTaskTrain.setOperativeViews(operativeViews);
        initTaskTrain.setTesting(false);
        initTaskTrain.setType(initTaskTrain.getType() + "-Train-" + experimentName);
        initTaskTrain.setAttribute(TC_TASK_TYPE, TcTaskType.INIT_TRAIN.toString());

        // init the test part of the experiment
        initTaskTest = new InitTaskDeep();
        initTaskTest.setTesting(true);
        initTaskTest.setPreprocessing(getPreprocessing());
        initTaskTest.setOperativeViews(operativeViews);
        initTaskTest.setType(initTaskTest.getType() + "-Test-" + experimentName);
        initTaskTest.setAttribute(TC_TASK_TYPE, TcTaskType.INIT_TEST.toString());

        // get some meta data depending on the whole document collection 
        preparationTask = new PreparationTask();
        preparationTask.setType(preparationTask.getType() + "-" + experimentName);
        preparationTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN,
                PreparationTask.INPUT_KEY_TRAIN);
        preparationTask.addImport(initTaskTest, InitTask.OUTPUT_KEY_TEST,
                PreparationTask.INPUT_KEY_TEST);
        preparationTask.setAttribute(TC_TASK_TYPE, TcTaskType.META.toString());
        
        embeddingTask = new EmbeddingTask();
        embeddingTask.setType(embeddingTask.getType() + "-" + experimentName);
        embeddingTask.addImport(preparationTask, PreparationTask.OUTPUT_KEY,
                EmbeddingTask.INPUT_MAPPING);

        // feature extraction on training data
        vectorizationTrainTask = new VectorizationTask();
        vectorizationTrainTask
                .setType(vectorizationTrainTask.getType() + "-Train-" + experimentName);
        vectorizationTrainTask.setTesting(false);
        vectorizationTrainTask.addImport(initTaskTrain, InitTaskDeep.OUTPUT_KEY_TRAIN,
                VectorizationTask.DATA_INPUT_KEY);
        vectorizationTrainTask.addImport(preparationTask, PreparationTask.OUTPUT_KEY,
                VectorizationTask.MAPPING_INPUT_KEY);
        vectorizationTrainTask.setAttribute(TC_TASK_TYPE,
                TcTaskType.FEATURE_EXTRACTION_TRAIN.toString());

        // feature extraction on test data
        vectorizationTestTask = new VectorizationTask();
        vectorizationTestTask.setType(vectorizationTestTask.getType() + "-Test-" + experimentName);
        vectorizationTestTask.setTesting(true);
        vectorizationTestTask.addImport(initTaskTest, InitTaskDeep.OUTPUT_KEY_TEST,
                VectorizationTask.DATA_INPUT_KEY);
        vectorizationTestTask.addImport(preparationTask, PreparationTask.OUTPUT_KEY,
                VectorizationTask.MAPPING_INPUT_KEY);
        vectorizationTrainTask.setAttribute(TC_TASK_TYPE,
                TcTaskType.FEATURE_EXTRACTION_TEST.toString());

        // test task operating on the models of the feature extraction train and test tasks
        learningTask = mlDeepLearningAdapter.getTestTask();
        learningTask.setType(learningTask.getType() + "-" + experimentName);
        learningTask.setAttribute(TC_TASK_TYPE, TcTaskType.MACHINE_LEARNING_ADAPTER.toString());

        if (innerReports != null) {
            for (Class<? extends Report> report : innerReports) {
                learningTask.addReport(report);
            }
        }

        // // always add OutcomeIdReport
        learningTask.addReport(mlDeepLearningAdapter.getOutcomeIdReportClass());
        learningTask.addReport(DeeplearningBasicResultReport.class);
        learningTask.addImport(preparationTask, PreparationTask.OUTPUT_KEY,
                TcDeepLearningAdapter.PREPARATION_FOLDER);
        learningTask.addImport(vectorizationTrainTask, VectorizationTask.OUTPUT_KEY,
                Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
        learningTask.addImport(vectorizationTestTask, VectorizationTask.OUTPUT_KEY,
                Constants.TEST_TASK_INPUT_KEY_TEST_DATA);
        learningTask.addImport(initTaskTest, InitTaskDeep.OUTPUT_KEY_TEST,
                TcDeepLearningAdapter.TARGET_ID_MAPPING);

        // DKPro Lab issue 38: must be added as *first* task
        addTask(initTaskTrain);
        addTask(initTaskTest);
        addTask(preparationTask);
        addTask(embeddingTask);
        addTask(vectorizationTrainTask);
        addTask(vectorizationTestTask);
        addTask(learningTask);
    }
}
