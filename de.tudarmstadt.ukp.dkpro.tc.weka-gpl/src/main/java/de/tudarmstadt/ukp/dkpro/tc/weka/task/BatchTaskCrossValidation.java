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
import de.tudarmstadt.ukp.dkpro.tc.weka.report.TrainTestReport;

public class BatchTaskCrossValidation
    extends BatchTask
{

    private String experimentName;
    private CollectionReaderDescription reader;
    private AnalysisEngineDescription aggregate;
    private String dataWriter;
    private int numFolds;
    private boolean isRegressionExperiment = false;
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

    public BatchTaskCrossValidation(String aExperimentName, CollectionReaderDescription aReader,
            AnalysisEngineDescription aAggregate, String aDataWriterClassName, int aNumFolds)
    {
        setExperimentName(aExperimentName);
        setReader(aReader);
        setAggregate(aAggregate);
        setDataWriter(aDataWriterClassName);
        setNumFolds(aNumFolds);
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

        if (experimentName == null || reader == null || aggregate == null) {
            throw new IllegalStateException("You must set experiment name, reader and aggregate.");
        }

        // preprocessing on the entire data set and only once
        preprocessTask = new PreprocessTask();
        preprocessTask.setReader(reader);
        preprocessTask.setAggregate(aggregate);
        preprocessTask.setType(preprocessTask.getType() + "-" + experimentName);

        // collecting meta features only on the training data (numFolds times)
        metaTask = new MetaInfoTask();
        metaTask.setType(metaTask.getType() + experimentName);

        // extracting features from training data (numFolds times)
        extractFeaturesTrainTask = new ExtractFeaturesTask();
        extractFeaturesTrainTask.setAddInstanceId(addInstanceId);
        extractFeaturesTrainTask.setRegressionExperiment(isRegressionExperiment);
        extractFeaturesTrainTask.setDataWriter(dataWriter);
        extractFeaturesTrainTask.setTesting(false);
        extractFeaturesTrainTask.setType(extractFeaturesTrainTask.getType() + "-Train-"
                + experimentName);
        extractFeaturesTrainTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY,
                metaTask.getType());

        // extracting features from test data (numFolds times)
        extractFeaturesTestTask = new ExtractFeaturesTask();
        extractFeaturesTestTask.setAddInstanceId(addInstanceId);
        extractFeaturesTestTask.setRegressionExperiment(isRegressionExperiment);
        extractFeaturesTestTask.setDataWriter(dataWriter);
        extractFeaturesTestTask.setTesting(true);
        extractFeaturesTestTask.setType(extractFeaturesTestTask.getType() + "-Test-"
                + experimentName);
        extractFeaturesTestTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY,
                metaTask.getType());

        // classification (numFolds times)
        testTask = new TestTask();
        testTask.setType(testTask.getType() + "-" + experimentName);
        if (innerReport != null) {
            testTask.addReport(innerReport);
        }
        testTask.addImportLatest(TestTask.INPUT_KEY_TRAIN, ExtractFeaturesTask.OUTPUT_KEY,
                extractFeaturesTrainTask.getType());
        testTask.addImportLatest(TestTask.INPUT_KEY_TEST, ExtractFeaturesTask.OUTPUT_KEY,
                extractFeaturesTestTask.getType());

        // inner batch task (carried out numFolds times)
        BatchTask crossValidationTask = new BatchTask()
        {
            public void execute(TaskContext aContext)
                throws Exception
            {
                File xmiPathRoot = aContext.getStorageLocation(PreprocessTask.OUTPUT_KEY_TRAIN,
                        AccessMode.READONLY);
                Collection<File> files = FileUtils.listFiles(xmiPathRoot,
                        new String[] { "ser.gz" },
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
        crossValidationTask.addImportLatest(PreprocessTask.OUTPUT_KEY_TRAIN,
                PreprocessTask.OUTPUT_KEY_TRAIN,
                preprocessTask.getType());
        crossValidationTask.setType(crossValidationTask.getType() + experimentName);
        crossValidationTask.addTask(metaTask);
        crossValidationTask.addTask(extractFeaturesTrainTask);
        crossValidationTask.addTask(extractFeaturesTestTask);
        crossValidationTask.addTask(testTask);
        if (innerReport != null) {
            crossValidationTask.addReport(TrainTestReport.class);
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

    public void setRegressionExperiment(boolean isRegressionExperiment)
    {
        this.isRegressionExperiment = isRegressionExperiment;
    }

    public void setAddInstanceId(boolean addInstanceId)
    {
        this.addInstanceId = addInstanceId;
    }

    public void setInnerReport(Class<? extends Report> innerReport)
    {
        this.innerReport = innerReport;
    }
}