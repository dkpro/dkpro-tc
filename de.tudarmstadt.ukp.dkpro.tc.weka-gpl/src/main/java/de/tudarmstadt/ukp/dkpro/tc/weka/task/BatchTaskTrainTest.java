package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.reporting.Report;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ValidityCheckTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.OutcomeIDReport;

/**
 * Train-Test setup
 * 
 * @author daxenberger
 * 
 */
public class BatchTaskTrainTest
    extends BatchTask
{

    private String experimentName;
    private AnalysisEngineDescription aggregate;
    private String dataWriter;
    private Class<? extends Report> innerReport;
    private boolean isUnitClassification;

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
     * Preconfigured train-test setup which should work out-of-the-box. You might want to set a
     * report to collect the results.
     * 
     * @param aExperimentName
     *            name of the experiment
     * @param aReaderTrain
     *            collection reader for train data
     * @param aReaderTest
     *            collection reader for test data
     * @param aAggregate
     *            preprocessing analysis engine aggregate
     * @param aDataWriterClassName
     *            data writer class name
     */
    public BatchTaskTrainTest(String aExperimentName, AnalysisEngineDescription aAggregate,
            String aDataWriterClassName)
    {
        setExperimentName(aExperimentName);
        setAggregate(aAggregate);
        setDataWriter(aDataWriterClassName);
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
        if (experimentName == null || aggregate == null || dataWriter == null)

        {
            throw new IllegalStateException(
                    "You must set Experiment Name, DataWriter and Aggregate.");
        }
        
        // check the validity of the experiment setup first
        checkTask = new ValidityCheckTask();

        // preprocessing on training data
        preprocessTaskTrain = new PreprocessTask();
        preprocessTaskTrain.setAggregate(aggregate);
        preprocessTaskTrain.setTesting(false);
        preprocessTaskTrain.setUnitClassification(isUnitClassification);
        preprocessTaskTrain.setType(preprocessTaskTrain.getType() + "-Train-" + experimentName);
        preprocessTaskTrain.addImport(checkTask, ValidityCheckTask.DUMMY_KEY);

        // preprocessing on test data
        preprocessTaskTest = new PreprocessTask();
        preprocessTaskTest.setAggregate(aggregate);
        preprocessTaskTest.setTesting(true);
        preprocessTaskTest.setUnitClassification(isUnitClassification);
        preprocessTaskTest.setType(preprocessTaskTest.getType() + "-Test-" + experimentName);

        // get some meta data depending on the whole document collection that we need for training
        metaTask = new MetaInfoTask();
        metaTask.setType(metaTask.getType() + "-" + experimentName);
        metaTask.addImport(preprocessTaskTrain, PreprocessTask.OUTPUT_KEY_TRAIN,
                MetaInfoTask.INPUT_KEY);

        // feature extraction on training data
        featuresTrainTask = new ExtractFeaturesTask();
        featuresTrainTask.setAddInstanceId(true);
        featuresTrainTask.setDataWriter(dataWriter);
        featuresTrainTask.setType(featuresTrainTask.getType() + "-Train-" + experimentName);
        featuresTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);
        featuresTrainTask.addImport(preprocessTaskTrain, PreprocessTask.OUTPUT_KEY_TRAIN,
                ExtractFeaturesTask.INPUT_KEY);

        // feature extraction on test data
        featuresTestTask = new ExtractFeaturesTask();
        featuresTestTask.setAddInstanceId(true);
        featuresTestTask.setDataWriter(dataWriter);
        featuresTestTask.setType(featuresTestTask.getType() + "-Test-" + experimentName);
        featuresTestTask.addImport(metaTask, MetaInfoTask.META_KEY);
        featuresTestTask.addImport(preprocessTaskTest, PreprocessTask.OUTPUT_KEY_TEST,
                ExtractFeaturesTask.INPUT_KEY);

        // test task operating on the models of the feature extraction train and test tasks
        testTask = new TestTask();
        testTask.setType(testTask.getType() + "-" + experimentName);
        if (innerReport != null) {
            testTask.addReport(innerReport);
        }
        testTask.addReport(OutcomeIDReport.class);
        testTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
                TestTask.INPUT_KEY_TRAIN);
        testTask.addImport(featuresTestTask, ExtractFeaturesTask.OUTPUT_KEY,
                TestTask.INPUT_KEY_TEST);

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

    /**
     * Set this to true, if you want to classify more than one classification unit (instance) per
     * document (CAS). This requires a TextClassificationUnit annotation for all units to be
     * classified.
     * 
     * @param isUnitClassification
     *            if set to true, more than one instance per document will be expected
     */
    public void setUnitClassification(boolean isUnitClassification)
    {
        this.isUnitClassification = isUnitClassification;
    }

    /**
     * Sets the report for the test task
     * 
     * @param innerReport
     *            classification report or regression report
     */
    public void setInnerReport(Class<? extends Report> innerReport)
    {
        this.innerReport = innerReport;
    }
}
