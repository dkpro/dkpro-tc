/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.tc.ml;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.reporting.Report;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.DefaultBatchTask;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.FoldDimensionBundle;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.TaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.InitTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;

/**
 * Crossvalidation setup
 * 
 */
public class ExperimentCrossValidation
    extends DefaultBatchTask
{

    protected String experimentName;
    protected AnalysisEngineDescription preprocessing;
    protected List<String> operativeViews;
    protected Comparator<String> comparator;
    protected int numFolds = 10;
    protected List<Class<? extends Report>> innerReports;
    protected TCMachineLearningAdapter mlAdapter;

    protected InitTask initTask;
    protected MetaInfoTask metaTask;
    protected ExtractFeaturesTask extractFeaturesTrainTask;
    protected ExtractFeaturesTask extractFeaturesTestTask;
    protected TaskBase testTask;

    public ExperimentCrossValidation()
    {/* needed for Groovy */
    }

    /**
     * Preconfigured crossvalidation setup.  Pseudo-random assignment of
     * instances to folds.
     * 
     * @param aExperimentName
     *            name of the experiment
     * @param preprocessing
     *            preprocessing analysis engine aggregate
     * @param aNumFolds
     *            the number of folds for crossvalidation (default 10)
     */
	public ExperimentCrossValidation(String aExperimentName,
			Class<? extends TCMachineLearningAdapter> mlAdapter,
			AnalysisEngineDescription preprocessing, int aNumFolds)
			throws TextClassificationException
	{
		this(aExperimentName,
				mlAdapter,
				preprocessing,
				aNumFolds,
				null
				);
	}
	/**
	 * Use this constructor for CV fold control.  The Comparator is
	 * used to determine which instances must occur together in the same
	 * CV fold.
	 * 
	 * @param aExperimentName
	 * @param mlAdapter
	 * @param preprocessing
	 * @param aNumFolds
	 * @param aComparator
	 * @throws TextClassificationException
	 */
	public ExperimentCrossValidation(String aExperimentName,
			Class<? extends TCMachineLearningAdapter> mlAdapter,
			AnalysisEngineDescription preprocessing, int aNumFolds,
			Comparator<String> aComparator)
			throws TextClassificationException
	{
		setExperimentName(aExperimentName);
		setMachineLearningAdapter(mlAdapter);
		setPreprocessing(preprocessing);
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
     *             if no or invalid arguments have been provided 
     */
    protected void init()
        throws IllegalStateException    {

        if (experimentName == null || preprocessing == null) {
            throw new IllegalStateException(
                    "You must set experiment name, datawriter and preprocessing aggregate.");
        }

        if (numFolds < 2) {
            throw new IllegalStateException(
                    "Number of folds is not configured correctly. Number of folds needs to be at " +
                            "least 2 (but was " + numFolds + ")");
        }

        // initialize the setup
        initTask = new InitTask();
        initTask.setMlAdapter(mlAdapter);
        initTask.setPreprocessing(preprocessing);
        initTask.setOperativeViews(operativeViews);
        initTask.setType(initTask.getType() + "-" + experimentName);

        // inner batch task (carried out numFolds times)
        DefaultBatchTask crossValidationTask = new DefaultBatchTask()
        {
            @Override
            public void initialize(TaskContext aContext)
            {
                super.initialize(aContext);
                
                File xmiPathRoot = aContext.getStorageLocation(InitTask.OUTPUT_KEY_TRAIN,
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
                if (numFolds == Constants.LEAVE_ONE_OUT) {
                    numFolds = fileNames.length;
                }
                // don't change any names!!
                FoldDimensionBundle<String> foldDim = getFoldDim(fileNames);
                Dimension<File> filesRootDim = Dimension.create("filesRoot", xmiPathRoot);

                ParameterSpace pSpace = new ParameterSpace(foldDim, filesRootDim);
                setParameterSpace(pSpace);
            }
        };

        // ================== SUBTASKS OF THE INNER BATCH TASK =======================

        // collecting meta features only on the training data (numFolds times)
        metaTask = new MetaInfoTask();
        metaTask.setOperativeViews(operativeViews);
        metaTask.setType(metaTask.getType() + "-" + experimentName);

        // extracting features from training data (numFolds times)
        extractFeaturesTrainTask = new ExtractFeaturesTask();
        extractFeaturesTrainTask.setTesting(false);
        extractFeaturesTrainTask.setType(extractFeaturesTrainTask.getType() + "-Train-"
                + experimentName);
        extractFeaturesTrainTask.setMlAdapter(mlAdapter);
        extractFeaturesTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);

        // extracting features from test data (numFolds times)
        extractFeaturesTestTask = new ExtractFeaturesTask();
        extractFeaturesTestTask.setTesting(true);
        extractFeaturesTestTask.setType(extractFeaturesTestTask.getType() + "-Test-"
                + experimentName);
        extractFeaturesTestTask.setMlAdapter(mlAdapter);
        extractFeaturesTestTask.addImport(metaTask, MetaInfoTask.META_KEY);
        extractFeaturesTestTask.addImport(extractFeaturesTrainTask, ExtractFeaturesTask.OUTPUT_KEY);
        
        // classification (numFolds times)
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

        testTask.addImport(extractFeaturesTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
                Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
        testTask.addImport(extractFeaturesTestTask, ExtractFeaturesTask.OUTPUT_KEY,
                Constants.TEST_TASK_INPUT_KEY_TEST_DATA);

        // ================== CONFIG OF THE INNER BATCH TASK =======================

        crossValidationTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN);
        crossValidationTask.setType(crossValidationTask.getType() + experimentName);
        crossValidationTask.addTask(metaTask);
        crossValidationTask.addTask(extractFeaturesTrainTask);
        crossValidationTask.addTask(extractFeaturesTestTask);
        crossValidationTask.addTask(testTask);
        crossValidationTask.setExecutionPolicy(ExecutionPolicy.USE_EXISTING);
        // report of the inner batch task (sums up results for the folds)
        // we want to re-use the old CV report, we need to collect the evaluation.bin files from
        // the test task here (with another report)
        crossValidationTask.addReport(mlAdapter.getBatchTrainTestReportClass());

        // DKPro Lab issue 38: must be added as *first* task
        addTask(initTask);
        addTask(crossValidationTask);
    }

    @Override
    public void initialize(TaskContext aContext)
    {
        super.initialize(aContext);
        init();
    }

    protected FoldDimensionBundle<String> getFoldDim(String[] fileNames)
    {
    	if(comparator != null){
    		return new FoldDimensionBundle<String>("files", Dimension.create("", fileNames), numFolds,
                    comparator);
    	}
        return new FoldDimensionBundle<String>("files", Dimension.create("", fileNames), numFolds);
    }

    public void setExperimentName(String experimentName)
    {
        this.experimentName = experimentName;
    }

    public void setMachineLearningAdapter(Class<? extends TCMachineLearningAdapter> mlAdapter)
        throws IllegalArgumentException
    {
        try {
			this.mlAdapter = mlAdapter.newInstance();
		} catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
		} catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
		}
    }

    public void setPreprocessing(AnalysisEngineDescription preprocessing)
    {
        this.preprocessing = preprocessing;
    }

    public void setOperativeViews(List<String> operativeViews)
    {
        this.operativeViews = operativeViews;
    }

    public void setNumFolds(int numFolds)
    {
        this.numFolds = numFolds;
    }
    public void setComparator(Comparator<String> aComparator)
    {
        comparator = aComparator;
    }

    /**
     * Adds a report for the inner test task
     * 
     * @param innerReport
     *            classification report or regression report
     */
    public void addInnerReport(Class<? extends Report> innerReport)
    {
        if (innerReports == null) {
            innerReports = new ArrayList<Class<? extends Report>>();
        }
        innerReports.add(innerReport);
    }
}