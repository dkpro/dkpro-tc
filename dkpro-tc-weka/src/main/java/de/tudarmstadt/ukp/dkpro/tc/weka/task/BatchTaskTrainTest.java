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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.reporting.Report;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ValidityCheckTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.ClassificationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.OutcomeIDReport;

/**
 * Train-Test setup
 * 
 * @author daxenberger
 * @author zesch
 * 
 */
public class BatchTaskTrainTest
    extends BatchTask
{

    private String experimentName;
    private AnalysisEngineDescription preprocessingPipeline;
    private List<String> operativeViews;
    private List<Class<? extends Report>> innerReports;

    private ValidityCheckTask checkTask;
    private PreprocessTask preprocessTaskTrain;
    private PreprocessTask preprocessTaskTest;
    private MetaInfoTask metaTask;
    private ExtractFeaturesTask featuresTrainTask;
    private ExtractFeaturesTask featuresTestTask;
    private TestTask testTask;

    public BatchTaskTrainTest()
    {/* needed for Groovy */
    }

    /**
     * Preconfigured train-test setup.
     * 
     * @param aExperimentName
     *            name of the experiment
     * @param preprocessingPipeline
     *            preprocessing analysis engine aggregate
     */
    public BatchTaskTrainTest(String aExperimentName,
            AnalysisEngineDescription preprocessingPipeline)
    {
        setExperimentName(aExperimentName);
        setPreprocessingPipeline(preprocessingPipeline);
        // set name of overall batch task
        setType("Evaluation-" + experimentName);
    }

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        init();
        super.execute(aContext);
    }

    /**
     * Initializes the experiment. This is called automatically before execution. It's not done
     * directly in the constructor, because we want to be able to use setters instead of the
     * three-argument constructor.
     * 
     * @throws IllegalStateException
     *             if not all necessary arguments have been set.
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void init()
    {
        if (experimentName == null || preprocessingPipeline == null)

        {
            throw new IllegalStateException(
                    "You must set Experiment Name, DataWriter and Aggregate.");
        }

        // check the validity of the experiment setup first
        checkTask = new ValidityCheckTask();

        // preprocessing on training data
        preprocessTaskTrain = new PreprocessTask();
        preprocessTaskTrain.setPreprocessingPipeline(preprocessingPipeline);
        preprocessTaskTrain.setOperativeViews(operativeViews);
        preprocessTaskTrain.setTesting(false);
        preprocessTaskTrain.setType(preprocessTaskTrain.getType() + "-Train-" + experimentName);

        // preprocessing on test data
        preprocessTaskTest = new PreprocessTask();
        preprocessTaskTest.setPreprocessingPipeline(preprocessingPipeline);
        preprocessTaskTest.setOperativeViews(operativeViews);
        preprocessTaskTest.setTesting(true);
        preprocessTaskTest.setType(preprocessTaskTest.getType() + "-Test-" + experimentName);

        // get some meta data depending on the whole document collection that we need for training
        metaTask = new MetaInfoTask();
        metaTask.setOperativeViews(operativeViews);
        metaTask.setType(metaTask.getType() + "-" + experimentName);

        metaTask.addImport(preprocessTaskTrain, PreprocessTask.OUTPUT_KEY_TRAIN,
                MetaInfoTask.INPUT_KEY);

        // feature extraction on training data
        featuresTrainTask = new ExtractFeaturesTask();
        featuresTrainTask.setType(featuresTrainTask.getType() + "-Train-" + experimentName);
        featuresTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);
        featuresTrainTask.addImport(preprocessTaskTrain, PreprocessTask.OUTPUT_KEY_TRAIN,
                ExtractFeaturesTask.INPUT_KEY);

        // feature extraction on test data
        featuresTestTask = new ExtractFeaturesTask();
        featuresTestTask.setType(featuresTestTask.getType() + "-Test-" + experimentName);
        featuresTestTask.addImport(metaTask, MetaInfoTask.META_KEY);
        featuresTestTask.addImport(preprocessTaskTest, PreprocessTask.OUTPUT_KEY_TEST,
                ExtractFeaturesTask.INPUT_KEY);

        // test task operating on the models of the feature extraction train and test tasks
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

        testTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
                TestTask.TEST_TASK_INPUT_KEY_TRAINING_DATA);
        testTask.addImport(featuresTestTask, ExtractFeaturesTask.OUTPUT_KEY,
                TestTask.TEST_TASK_INPUT_KEY_TEST_DATA);

        // DKPro Lab issue 38: must be added as *first* task
        addTask(checkTask);
        addTask(preprocessTaskTrain);
        addTask(preprocessTaskTest);
        addTask(metaTask);
        addTask(featuresTrainTask);
        addTask(featuresTestTask);
        addTask(testTask);
    }

    public void setExperimentName(String experimentName)
    {
        this.experimentName = experimentName;
    }

    public void setPreprocessingPipeline(AnalysisEngineDescription preprocessingPipeline)
    {
        this.preprocessingPipeline = preprocessingPipeline;
    }

    public void setOperativeViews(List<String> operativeViews)
    {
        this.operativeViews = operativeViews;
    }

    /**
     * Sets the report for the test task
     * 
     * @param innerReport
     *            classification report or regression report
     */
    public void addInnerReport(Class<? extends Report> innerReport)
    {
        if (innerReports == null) {
            innerReports = new ArrayList<Class<? extends Report>>();
        }
        this.innerReports.add(innerReport);
    }
}
