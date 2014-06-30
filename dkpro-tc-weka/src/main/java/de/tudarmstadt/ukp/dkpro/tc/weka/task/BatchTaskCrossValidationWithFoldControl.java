/**
 * Copyright 2014
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.reporting.Report;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.FoldDimensionBundle;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ValidityCheckTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.ClassificationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.OutcomeIDReport;

/**
 * Crossvalidation setup
 * 
 * @author daxenberger
 * @author zesch
 * @author jamison
 * 
 */
public class BatchTaskCrossValidationWithFoldControl
    extends BatchTaskCrossValidation
{

    protected Comparator<String> comparator;// EJ

    public BatchTaskCrossValidationWithFoldControl()
    {/* needed for Groovy */
    }

    /**
     * EJ added
     * 
     * @param aExperimentName
     * @param preprocessingPipeline
     * @param aNumFolds
     * @param comparator
     */
    public BatchTaskCrossValidationWithFoldControl(String aExperimentName,
            AnalysisEngineDescription preprocessingPipeline,
            int aNumFolds, Comparator<String> aComparator)
    {
        setExperimentName(aExperimentName);
        setPreprocessingPipeline(preprocessingPipeline);
        setNumFolds(aNumFolds);
        setComparator(aComparator);
        // set name of overall batch task
        setType("Evaluation-" + experimentName);
    }

    @Override
    protected FoldDimensionBundle<String> getFoldDim(String[] fileNames)
    {
        return new FoldDimensionBundle<String>("files", Dimension.create("", fileNames), numFolds,
                comparator);
        
    }

    public void setComparator(Comparator<String> aComparator)
    {
        this.comparator = aComparator;
    }

    // This method is only 1 line different (FoldDimensionBundle) from its parent!
    // Almost entirely duplicate code. Should be refactored.
    @Override
    protected void init()
        throws IllegalStateException, InstantiationException, IllegalAccessException,
        ClassNotFoundException
    {

        if (experimentName == null || preprocessingPipeline == null) {
            throw new IllegalStateException(
                    "You must set experiment name, datawriter and aggregate.");
        }

        if (numFolds < 2) {
            throw new IllegalStateException(
                    "Number of folds is not configured correctly. Number of folds needs to be at least 2.");
        }

        // check the validity of the experiment setup first
        checkTask = new ValidityCheckTask();

        // preprocessing on the entire data set and only once
        preprocessTask = new PreprocessTask();
        preprocessTask.setPreprocessingPipeline(preprocessingPipeline);
        preprocessTask.setOperativeViews(operativeViews);
        preprocessTask.setType(preprocessTask.getType() + "-" + experimentName);

        // inner batch task (carried out numFolds times)
        BatchTask crossValidationTask = new BatchTask()
        {
            @Override
            public void execute(TaskContext aContext)
                throws Exception
            {
                File xmiPathRoot = aContext.getStorageLocation(PreprocessTask.OUTPUT_KEY_TRAIN,
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
                FoldDimensionBundle<String> foldDim = new FoldDimensionBundle<String>("files",
                        Dimension.create("", fileNames), numFolds, comparator);
                Dimension<File> filesRootDim = Dimension.create("filesRoot", xmiPathRoot);

                ParameterSpace pSpace = new ParameterSpace(foldDim, filesRootDim);
                setParameterSpace(pSpace);

                super.execute(aContext);
            }
        };

        // ================== SUBTASKS OF THE INNER BATCH TASK =======================

        // collecting meta features only on the training data (numFolds times)
        metaTask = new MetaInfoTask();
        metaTask.setOperativeViews(operativeViews);
        metaTask.setType(metaTask.getType() + experimentName);

        // extracting features from training data (numFolds times)
        extractFeaturesTrainTask = new ExtractFeaturesTask();
        extractFeaturesTrainTask.setTesting(false);
        extractFeaturesTrainTask.setType(extractFeaturesTrainTask.getType() + "-Train-"
                + experimentName);
        extractFeaturesTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);

        // extracting features from test data (numFolds times)
        extractFeaturesTestTask = new ExtractFeaturesTask();
        extractFeaturesTestTask.setTesting(true);
        extractFeaturesTestTask.setType(extractFeaturesTestTask.getType() + "-Test-"
                + experimentName);
        extractFeaturesTestTask.addImport(metaTask, MetaInfoTask.META_KEY);

        // classification (numFolds times)
        testTask = new TestTask();
        testTask.setType(testTask.getType() + "-" + experimentName);

        if (innerReports != null) {
            for (Class<? extends Report> report : innerReports) {
                testTask.addReport(report);
            }
        }
        else {
            // add default report
            testTask.addReport(ClassificationReport.class);
        }
        // always add OutcomeIdReport
        testTask.addReport(OutcomeIDReport.class);

        testTask.addImport(extractFeaturesTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
                TestTask.TEST_TASK_INPUT_KEY_TRAINING_DATA);
        testTask.addImport(extractFeaturesTestTask, ExtractFeaturesTask.OUTPUT_KEY,
                TestTask.TEST_TASK_INPUT_KEY_TEST_DATA);

        // ================== CONFIG OF THE INNER BATCH TASK =======================

        crossValidationTask.addImport(preprocessTask, PreprocessTask.OUTPUT_KEY_TRAIN);
        crossValidationTask.setType(crossValidationTask.getType() + experimentName);
        crossValidationTask.addTask(metaTask);
        crossValidationTask.addTask(extractFeaturesTrainTask);
        crossValidationTask.addTask(extractFeaturesTestTask);
        crossValidationTask.addTask(testTask);
        // report of the inner batch task (sums up results for the folds)
        // we want to re-use the old CV report, we need to collect the evaluation.bin files from
        // the test task here (with another report)
        crossValidationTask.addReport(BatchTrainTestReport.class);

        // DKPro Lab issue 38: must be added as *first* task
        addTask(checkTask);
        addTask(preprocessTask);
        addTask(crossValidationTask);
    }
}