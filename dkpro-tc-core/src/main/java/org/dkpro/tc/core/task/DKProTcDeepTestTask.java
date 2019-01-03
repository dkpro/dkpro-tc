/*******************************************************************************
 * Copyright 2019
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
package org.dkpro.tc.core.task;

import java.util.HashSet;
import java.util.List;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.DefaultBatchTask;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcDeepLearningAdapter;
import org.dkpro.tc.core.task.deep.EmbeddingTask;
import org.dkpro.tc.core.task.deep.PreparationTask;
import org.dkpro.tc.core.task.deep.VectorizationTask;

/**
 * This is a facade-task that is used in experimental setups to avoid the need for providing the
 * machine learning adapter when the experiment is wired together, i.e. train-test or
 * cross-validation. This enables using multiple machine learning classifier within the same
 * experimental setup. When the experiments starts, this task retrieves from the classification
 * arguments (at first position, idx=0), the machine learning adapter. The execution of this task
 * executes the machine learning-specific task that is provided as argument. This is only possible
 * if the decision which machine learning adapter to use is deferred to be a decision when the
 * experiment is already running.
 * 
 * i.e., during experiment wiring, the classification arguments are not yet available (Lab hasn't
 * started yet). The detour of using this facade-task works-around this issue by deferring the
 * decision of which adapter should be executed to a point in time where Lab is running and the
 * classification argument is filled and can be read. This task essentially executes whatever task
 * is specified by the machine learning adapter that is expected to be at the first position in the
 * classification arguments.
 *
 */
public class DKProTcDeepTestTask
    extends DefaultBatchTask
    implements Constants
{

    @Discriminator(name = DIM_CLASSIFICATION_ARGS)
    protected List<Object> classArgs;

    private VectorizationTask vectorizationTrainTask;

    private VectorizationTask vectorizationTestTask;

    private PreparationTask preparationTask;

    private List<ReportBase> reports;

    private String experimentName;

    private EmbeddingTask embeddingTask;

    public DKProTcDeepTestTask()
    {
        // groovy
    }

    public DKProTcDeepTestTask(PreparationTask preparationTask, EmbeddingTask embeddingTask,
            VectorizationTask trainVectorization, VectorizationTask testVectorization,
            List<ReportBase> reports, String experimentName)
    {
        this.embeddingTask = embeddingTask;
        this.vectorizationTrainTask = trainVectorization;
        this.vectorizationTestTask = testVectorization;
        this.preparationTask = preparationTask;
        this.reports = reports;
        this.experimentName = experimentName;
    }

    @Override
    public void initialize(TaskContext aContext)
    {

        super.initialize(aContext);

        TcDeepLearningAdapter adapter = (TcDeepLearningAdapter) classArgs.get(0);
        ExecutableTaskBase testTask = adapter.getTestTask();

        testTask.addReport(adapter.getOutcomeIdReportClass());
        testTask.addReport(adapter.getMetaCollectionReport());
        ReportBase majorityBaselineIdReportClass = adapter.getMajorityBaselineIdReportClass();
        if (majorityBaselineIdReportClass != null) {
            testTask.addReport(majorityBaselineIdReportClass);
        }

        ReportBase randomBaselineIdReportClass = adapter.getRandomBaselineIdReportClass();
        if (randomBaselineIdReportClass != null) {
            testTask.addReport(randomBaselineIdReportClass);
        }

        if (reports != null) {
            for (ReportBase b : reports) {
                testTask.addReport(b);
            }
        }

        testTask.addImport(preparationTask, PreparationTask.OUTPUT_KEY,
                TcDeepLearningAdapter.PREPARATION_FOLDER);
        testTask.addImport(vectorizationTrainTask, VectorizationTask.OUTPUT_KEY,
                Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
        testTask.addImport(vectorizationTestTask, VectorizationTask.OUTPUT_KEY,
                Constants.TEST_TASK_INPUT_KEY_TEST_DATA);

        testTask.addImport(embeddingTask, EmbeddingTask.OUTPUT_KEY,
                TcDeepLearningAdapter.EMBEDDING_FOLDER);

        testTask.addImport(vectorizationTrainTask, VectorizationTask.OUTPUT_KEY,
                TcDeepLearningAdapter.VECTORIZIATION_TRAIN_OUTPUT);
        testTask.addImport(vectorizationTrainTask, VectorizationTask.OUTPUT_KEY,
                TcDeepLearningAdapter.TARGET_ID_MAPPING_TRAIN);

        testTask.addImport(vectorizationTestTask, VectorizationTask.OUTPUT_KEY,
                TcDeepLearningAdapter.VECTORIZIATION_TEST_OUTPUT);
        testTask.addImport(vectorizationTestTask, VectorizationTask.OUTPUT_KEY,
                TcDeepLearningAdapter.TARGET_ID_MAPPING_TEST);
        
        testTask.setAttribute(TC_TASK_TYPE, TcTaskType.MACHINE_LEARNING_ADAPTER.toString());
        testTask.setType(testTask.getType() + "-" + experimentName);

        deleteOldTaskSetNewOne(testTask);
    }

    /**
     * This method removes the (sub-)task storage of this task (in case another adapter ran before,
     * which would be stored there) and sets the new one as only adapter
     * 
     * @param testTask
     *            the current new task that shall be exeucted next as machine learning adapter
     */
    private void deleteOldTaskSetNewOne(ExecutableTaskBase testTask)
    {
        tasks = new HashSet<>();
        addTask(testTask);
    }

    @Override
    public boolean isInitialized()
    {
        // This is a hack - the facade-task has to re-initialize at every
        // execution to load the <i>current</i> machine learning adapter from
        // the classification arguments
        return false;
    }

}
