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
package org.dkpro.tc.ml.experiment.deep;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.reporting.Report;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.lab.task.impl.DefaultBatchTask;
import org.dkpro.lab.task.impl.DimensionBundle;
import org.dkpro.lab.task.impl.FoldDimensionBundle;
import org.dkpro.lab.task.impl.TaskBase;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.task.DKProTcDeepTestTask;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.task.TcTaskType;
import org.dkpro.tc.core.task.deep.EmbeddingTask;
import org.dkpro.tc.core.task.deep.InitTaskDeep;
import org.dkpro.tc.core.task.deep.PreparationTask;
import org.dkpro.tc.core.task.deep.VectorizationTask;
import org.dkpro.tc.ml.experiment.AbstractCrossValidation;
import org.dkpro.tc.ml.report.BasicResultReport;
import org.dkpro.tc.ml.report.shallowlearning.InnerReport;

public class DeepLearningExperimentCrossValidation
    extends AbstractCrossValidation
{

    protected Comparator<String> comparator;

    protected InitTaskDeep initTask;
    protected PreparationTask preparationTask;
    protected EmbeddingTask embeddingTask;
    protected VectorizationTask vectorizationTrainTask;
    protected VectorizationTask vectorizationTestTask;
    protected TaskBase learningTask;

    public DeepLearningExperimentCrossValidation()
    {/* needed for Groovy */
    }

    /**
     * Cross-validation experiment
     * 
     * @param aExperimentName
     *            Name of the experiment
     * @param aNumFolds
     *            number of folds
     * @throws TextClassificationException
     *             in case of errors
     */
    public DeepLearningExperimentCrossValidation(String aExperimentName,
            int aNumFolds)
        throws TextClassificationException
    {
        setExperimentName(aExperimentName);
        setNumFolds(aNumFolds);
        // set name of overall batch task
        setType("Evaluation-" + experimentName);
    }

    /**
     * Cross-validation experiment with control over instances in folds
     * 
     * @param aExperimentName
     *            Name of the experiment
     * @param aNumFolds
     *            number of folds
     * @param aComparator
     *            keeps data together
     * @throws TextClassificationException
     *             in case of errors
     * @throws TextClassificationException
     *             in case of errors
     */
    public DeepLearningExperimentCrossValidation(String aExperimentName,
            int aNumFolds,
            Comparator<String> aComparator)
        throws TextClassificationException
    {
        setExperimentName(aExperimentName);
        setNumFolds(aNumFolds);
        setComparator(aComparator);
        // set name of overall batch task
        setType("Evaluation-" + experimentName);
    }

    /**
     * Initializes the experiment. This is called automatically before execution. It's not done
     * directly in the constructor, because we want to be able to use setters instead of the
     * three-argument constructor.
     * 
     * @throws IllegalStateException
     *             in case of errors
     */
    protected void init() throws IllegalStateException
    {

        if (experimentName == null) {
            throw new IllegalStateException("You must set an experiment name");
        }

        if (aNumFolds < 2 && aNumFolds != -1) {
        	throw new IllegalStateException(
                    "Number of folds is not configured correctly. Number of folds needs to be at "
                            + "least [2] or [-1 (leave one out cross validation)] but was [" + aNumFolds + "]");
        }

        // initialize the setup
        initTask = new InitTaskDeep();
        initTask.setPreprocessing(getPreprocessing());
        initTask.setOperativeViews(operativeViews);
        initTask.setType(initTask.getType() + "-" + experimentName);
        initTask.setAttribute(TC_TASK_TYPE, TcTaskType.INIT_TRAIN.toString());

        // inner batch task (carried out numFolds times)
        DefaultBatchTask crossValidationTask = new DefaultBatchTask()
        {
            @Discriminator(name = DIM_FEATURE_MODE)
            private String featureMode;

            @Discriminator(name = DIM_CROSS_VALIDATION_MANUAL_FOLDS)
            private boolean useCrossValidationManualFolds;

            @Override
            public void initialize(TaskContext aContext)
            {
                super.initialize(aContext);

                File xmiPathRoot = aContext.getFolder(InitTask.OUTPUT_KEY_TRAIN,
                        AccessMode.READONLY);
                
                String[] fileNames = setupBatchTask(aContext, xmiPathRoot,
                        useCrossValidationManualFolds, featureMode);
                
                DimensionBundle<Collection<String>> bundle = getFoldDim(fileNames);
                Dimension<File> filesRootDim = Dimension.create(DIM_FILES_ROOT, xmiPathRoot);

                ParameterSpace pSpace = new ParameterSpace(bundle, filesRootDim);
                setParameterSpace(pSpace);
            }

        };

        // ================== SUBTASKS OF THE INNER BATCH TASK
        // =======================

        // collecting meta features only on the training data (numFolds times)
        // get some meta data depending on the whole document collection
        preparationTask = new PreparationTask();
        preparationTask.setType(preparationTask.getType() + "-" + experimentName);
        preparationTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN,
                PreparationTask.INPUT_KEY_TRAIN);
        preparationTask.setAttribute(TC_TASK_TYPE, TcTaskType.PREPARATION.toString());

        embeddingTask = new EmbeddingTask();
        embeddingTask.setType(embeddingTask.getType() + "-" + experimentName);
        embeddingTask.addImport(preparationTask, PreparationTask.OUTPUT_KEY,
                EmbeddingTask.INPUT_MAPPING);
        embeddingTask.setAttribute(TC_TASK_TYPE, TcTaskType.EMBEDDING.toString());

        // feature extraction on training data
        vectorizationTrainTask = new VectorizationTask();
        vectorizationTrainTask
                .setType(vectorizationTrainTask.getType() + "-Train-" + experimentName);
        vectorizationTrainTask.setTesting(false);
        vectorizationTrainTask.addImport(preparationTask, PreparationTask.OUTPUT_KEY,
                VectorizationTask.MAPPING_INPUT_KEY);
        vectorizationTrainTask.setAttribute(TC_TASK_TYPE,
                TcTaskType.VECTORIZATION_TRAIN.toString());

        // feature extraction on test data
        vectorizationTestTask = new VectorizationTask();
        vectorizationTestTask.setType(vectorizationTestTask.getType() + "-Test-" + experimentName);
        vectorizationTestTask.setTesting(true);
        vectorizationTestTask.addImport(preparationTask, PreparationTask.OUTPUT_KEY,
                VectorizationTask.MAPPING_INPUT_KEY);
        vectorizationTrainTask.setAttribute(TC_TASK_TYPE, TcTaskType.VECTORIZATION_TEST.toString());

        List<ReportBase> reports = new ArrayList<>();
        reports.add(new BasicResultReport());
        
        learningTask = new DKProTcDeepTestTask(preparationTask, embeddingTask, vectorizationTrainTask, vectorizationTestTask,
                reports, experimentName);
        learningTask.setType(learningTask.getType() + "-" + experimentName);
        learningTask.setAttribute(TC_TASK_TYPE, TcTaskType.FACADE_TASK.toString());

        if (innerReports != null) {
            for (Report report : innerReports) {
                learningTask.addReport(report);
            }
        }
        

        // ================== CONFIG OF THE INNER BATCH TASK
        // =======================

        crossValidationTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN);
        crossValidationTask.setType(crossValidationTask.getType() + "-" + experimentName);
        crossValidationTask.addTask(preparationTask);
        crossValidationTask.addTask(embeddingTask);
        crossValidationTask.addTask(vectorizationTrainTask);
        crossValidationTask.addTask(vectorizationTestTask);
        crossValidationTask.addTask(learningTask);
        crossValidationTask.setExecutionPolicy(ExecutionPolicy.USE_EXISTING);
        // report of the inner batch task (sums up results for the folds)
        // we want to re-use the old CV report, we need to collect the
        // evaluation.bin files from
        // the test task here (with another report)
        crossValidationTask.addReport(InnerReport.class);
        crossValidationTask.setAttribute(TC_TASK_TYPE, TcTaskType.CROSS_VALIDATION.toString());

        // DKPro Lab issue 38: must be added as *first* task
        addTask(initTask);
        addTask(crossValidationTask);
    }

    /**
     * 
     * @param fileNames
     *            the file names
     * @return fold dimension bundle
     */
    protected FoldDimensionBundle<String> getFoldDim(String[] fileNames)
    {
        if (comparator != null) {
            return new FoldDimensionBundle<String>("files", Dimension.create("", fileNames),
                    aNumFolds, comparator);
        }
        return new FoldDimensionBundle<String>("files", Dimension.create("", fileNames), aNumFolds);
    }

    /**
     * Sets a comparator
     * 
     * @param aComparator
     *            the comparator
     */
    public void setComparator(Comparator<String> aComparator)
    {
        comparator = aComparator;
    }

}