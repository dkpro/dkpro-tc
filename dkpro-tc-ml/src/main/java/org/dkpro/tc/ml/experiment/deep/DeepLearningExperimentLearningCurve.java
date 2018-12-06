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
package org.dkpro.tc.ml.experiment.deep;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
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
import org.dkpro.tc.core.task.DKProTcDeepTestTask;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.task.TcTaskType;
import org.dkpro.tc.core.task.deep.EmbeddingTask;
import org.dkpro.tc.core.task.deep.InitTaskDeep;
import org.dkpro.tc.core.task.deep.PreparationTask;
import org.dkpro.tc.core.task.deep.VectorizationTask;
import org.dkpro.tc.ml.FoldUtil;
import org.dkpro.tc.ml.base.Experiment_ImplBase;
import org.dkpro.tc.ml.experiment.dim.LearningCurveDimBundleCrossValidation;
import org.dkpro.tc.ml.report.BasicResultReport;
import org.dkpro.tc.ml.report.deeplearning.DeepLearningInnerReport;

public class DeepLearningExperimentLearningCurve
    extends Experiment_ImplBase
    implements Constants
{

    protected Comparator<String> comparator;
    protected int numFolds = 10;

    protected InitTaskDeep initTask;
    protected PreparationTask preparationTask;
    protected EmbeddingTask embeddingTask;
    protected VectorizationTask vectorizationTrainTask;
    protected VectorizationTask vectorizationTestTask;
    protected TaskBase learningTask;
	private int stageLimit=-1;

    public DeepLearningExperimentLearningCurve()
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
    public DeepLearningExperimentLearningCurve(String aExperimentName, int aNumFolds)
        throws TextClassificationException
    {
        setExperimentName(aExperimentName);
        setNumFolds(aNumFolds);
        // set name of overall batch task
        setType("Evaluation-" + experimentName);
    }

    /**
	 * Cross-validation experiment
	 * 
	 * @param aExperimentName Name of the experiment
	 * @param aNumFolds       number of folds
	 * @param aLimitPerStage  limits the number of runs on each stage of the
	 *                        learning curve to the provided number, which must be a
	 *                        positive non-zero integer value
	 * @throws TextClassificationException in case of errors
	 */
    public DeepLearningExperimentLearningCurve(String aExperimentName, int aNumFolds, int aLimitPerStage)
        throws TextClassificationException
    {
        stageLimit = aLimitPerStage;
        setExperimentName(aExperimentName);
        setNumFolds(aNumFolds);
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

        if (numFolds < 2 && numFolds != -1) {
        	throw new IllegalStateException(
                    "Number of folds is not configured correctly. Number of folds needs to be at "
                            + "least [2] or [-1 (leave one out cross validation)] but was [" + numFolds + "]");
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
                Collection<File> files = FileUtils.listFiles(xmiPathRoot, new String[] { "bin" },
                        true);
                String[] fileNames = new String[files.size()];
                int i = 0;
                for (File f : files) {
                    // adding file paths, not names
                    fileNames[i] = f.getAbsolutePath();
                    i++;
                }
                Arrays.sort(fileNames);
                if (numFolds == LEAVE_ONE_OUT) {
                    numFolds = fileNames.length;
                }

                // is executed if we have less CAS than requested folds and
                // manual mode is turned off
                if (!useCrossValidationManualFolds && fileNames.length < numFolds) {
                    xmiPathRoot = createRequestedNumberOfCas(xmiPathRoot, fileNames.length,
                            featureMode);
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
                LearningCurveDimBundleCrossValidation foldDim = getFoldDim(fileNames);
                Dimension<File> filesRootDim = Dimension.create(DIM_FILES_ROOT, xmiPathRoot);

                ParameterSpace pSpace = new ParameterSpace(foldDim, filesRootDim);
                setParameterSpace(pSpace);
            }

            /**
             * creates required number of CAS
             * 
             * @param xmiPathRoot
             *            input path
             * @param numAvailableJCas
             *            all CAS
             * @param featureMode
             *            the feature mode
             * @return a file
             */
            private File createRequestedNumberOfCas(File xmiPathRoot, int numAvailableJCas,
                    String featureMode)
            {

                try {
                    File outputFolder = FoldUtil.createMinimalSplit(xmiPathRoot.getAbsolutePath(),
                            numFolds, numAvailableJCas, FM_SEQUENCE.equals(featureMode));

                    if (outputFolder == null) {
                        throw new NullPointerException("Output folder is null");
                    }

                    verfiyThatNeededNumberOfCasWasCreated(outputFolder);

                    return outputFolder;
                }
                catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }

            private void verfiyThatNeededNumberOfCasWasCreated(File outputFolder)
            {
                int numCas = 0;

                File[] listFiles = outputFolder.listFiles();
                if (listFiles == null) {
                    throw new NullPointerException(
                            "Retrieving files in folder led to a NullPointer");
                }

                for (File f : listFiles) {
                    if (f.getName().contains(".bin")) {
                        numCas++;
                    }
                }

                if (numCas < numFolds) {
                    throw new IllegalStateException(
                            "Not enough TextClassificationUnits found to create at least ["
                                    + numFolds + "] folds");
                }
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
        crossValidationTask.addReport(DeepLearningInnerReport.class);
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
    protected LearningCurveDimBundleCrossValidation getFoldDim(String[] fileNames)
    {
        return new LearningCurveDimBundleCrossValidation("files", Dimension.create("", fileNames), numFolds, stageLimit);
    }

    /**
     * sets the number of folds
     * 
     * @param numFolds
     *            folds
     */
    public void setNumFolds(int numFolds)
    {
        this.numFolds = numFolds;
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