package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.reporting.Report;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.FoldDimensionBundle;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;

/**
 * Crossvalidation setup
 * 
 * @author daxenberger
 * 
 */
public class BatchTaskCrossValidation
    extends BatchTask
{

    private String experimentName;
    private CollectionReaderDescription reader;
    private AnalysisEngineDescription aggregate;
    private String dataWriter;
    private int numFolds = 10;
    private boolean addInstanceId = false;
    private Class<? extends Report> innerReport;

    private PreprocessTask preprocessTask;
    private MetaInfoTask metaTask;
    private ExtractFeaturesTask extractFeaturesTrainTask;
    private ExtractFeaturesTask extractFeaturesTestTask;
    private TestTask testTask;

    public BatchTaskCrossValidation()
    {/* needed for Groovy */
    }

    /**
     * Preconfigured crossvalidation setup which should work out-of-the-box. You might want to set a
     * report to collect the results.
     * 
     * @param aExperimentName
     *            name of the experiment
     * @param aReader
     *            collection reader for input data
     * @param aAggregate
     *            preprocessing analysis engine aggregate
     * @param aDataWriterClassName
     *            data writer class name
     * @param aNumFolds
     *            the number of folds for crossvalidation (default 10)
     */
    public BatchTaskCrossValidation(String aExperimentName, CollectionReaderDescription aReader,
            AnalysisEngineDescription aAggregate, String aDataWriterClassName, int aNumFolds)
    {
        setExperimentName(aExperimentName);
        setReader(aReader);
        setAggregate(aAggregate);
        setDataWriter(aDataWriterClassName);
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
     *             if not all necessary arguments have been set.
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void init()
        throws IllegalStateException, InstantiationException, IllegalAccessException,
        ClassNotFoundException
    {

        if (experimentName == null || reader == null || aggregate == null || dataWriter == null) {
            throw new IllegalStateException(
                    "You must set experiment name, reader, datawriter and aggregate.");
        }

        // preprocessing on the entire data set and only once
        preprocessTask = new PreprocessTask();
        preprocessTask.setReader(reader);
        preprocessTask.setAggregate(aggregate);
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
                Collection<File> files = FileUtils.listFiles(xmiPathRoot,
                        new String[] { "bin" },
                        true);
                String[] fileNames = new String[files.size()];
                int i = 0;
                for (File f : files) {
                    fileNames[i] = f.getName();
                    i++;
                }
                Arrays.sort(fileNames);

                // don't change any names!!
                FoldDimensionBundle<String> foldDim = new FoldDimensionBundle<String>("files",
                        Dimension.create("", fileNames), numFolds);
                Dimension<File> filesRootDim = Dimension.create("filesRoot", xmiPathRoot);

                ParameterSpace pSpace = new ParameterSpace(foldDim, filesRootDim);
                setParameterSpace(pSpace);

                super.execute(aContext);
            }
        };

        // ================== SUBTASKS OF THE INNER BATCH TASK =======================

        // collecting meta features only on the training data (numFolds times)
        metaTask = new MetaInfoTask();
        metaTask.setType(metaTask.getType() + experimentName);

        // extracting features from training data (numFolds times)
        extractFeaturesTrainTask = new ExtractFeaturesTask();
        extractFeaturesTrainTask.setAddInstanceId(addInstanceId);
        extractFeaturesTrainTask.setDataWriter(dataWriter);
        extractFeaturesTrainTask.setTesting(false);
        extractFeaturesTrainTask.setType(extractFeaturesTrainTask.getType() + "-Train-"
                + experimentName);
        extractFeaturesTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);

        // extracting features from test data (numFolds times)
        extractFeaturesTestTask = new ExtractFeaturesTask();
        extractFeaturesTestTask.setAddInstanceId(addInstanceId);
        extractFeaturesTestTask.setDataWriter(dataWriter);
        extractFeaturesTestTask.setTesting(true);
        extractFeaturesTestTask.setType(extractFeaturesTestTask.getType() + "-Test-"
                + experimentName);
        extractFeaturesTestTask.addImport(metaTask, MetaInfoTask.META_KEY);

        // classification (numFolds times)
        testTask = new TestTask();
        testTask.setType(testTask.getType() + "-" + experimentName);
        if (innerReport != null) {
            testTask.addReport(innerReport);
        }
        testTask.addImport(extractFeaturesTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
                TestTask.INPUT_KEY_TRAIN);
        testTask.addImport(extractFeaturesTestTask, ExtractFeaturesTask.OUTPUT_KEY,
                TestTask.INPUT_KEY_TEST);

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
        if (innerReport != null) {
            crossValidationTask.addReport(BatchTrainTestReport.class);
        }

        addTask(preprocessTask);
        addTask(crossValidationTask);
    }

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        init();
        super.execute(aContext);
    }

    public void setExperimentName(String experimentName)
    {
        this.experimentName = experimentName;
    }

    public void setReader(CollectionReaderDescription reader)
    {
        this.reader = reader;
    }

    public void setAggregate(AnalysisEngineDescription aggregate)
    {
        this.aggregate = aggregate;
    }

    public String getDataWriter()
    {
        return dataWriter;
    }

    public void setDataWriter(String dataWriter)
    {
        this.dataWriter = dataWriter;
    }

    public void setNumFolds(int numFolds)
    {
        this.numFolds = numFolds;
    }

    public void setAddInstanceId(boolean addInstanceId)
    {
        this.addInstanceId = addInstanceId;
    }

    /**
     * Sets the report for the inner test task
     * 
     * @param innerReport
     *            classification report or regression report
     */
    public void setInnerReport(Class<? extends Report> innerReport)
    {
        this.innerReport = innerReport;
    }
}