package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.tc.core.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.FeatureValuesReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.OutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.TrainTestReport;

public class BatchTaskTrainTest
    extends BatchTask
{

    private String experimentName;
    private CollectionReaderDescription readerTrain;
    private CollectionReaderDescription readerTest;
    private AnalysisEngineDescription aggregate;
    private List<Class<? extends MetaCollector>> metaCollectorClasses;
    private String dataWriter;

    private PreprocessTask preprocessTaskTrain;
    private PreprocessTask preprocessTaskTest;
    private MetaInfoTask metaTask;
    private ExtractFeaturesTask featuresTrainTask;
    private ExtractFeaturesTask featuresTestTask;
    private TestTask testTask;

    public BatchTaskTrainTest()
    {/* needed for Groovy */
    }

    public BatchTaskTrainTest(String aExperimentName, CollectionReaderDescription aReaderTrain,
            CollectionReaderDescription aReaderTest, AnalysisEngineDescription aAggregate,
            List<Class<? extends MetaCollector>> metaCollectorClasses, String aDataWriterClassName)
    {
        setExperimentName(aExperimentName);
        setReaderTrain(aReaderTrain);
        setReaderTest(aReaderTest);
        setAggregate(aAggregate);
        setDataWriter(aDataWriterClassName);
        setMetaCollectorClasses(metaCollectorClasses);
    }

    /**
     * Initializes the experiment. This is called automatically before execution. It's not done
     * directly in the constructor, because we want to be able to use setters instead of the
     * three-argument constructor.
     * 
     * @throws IllegalStateException
     *             if not all necessary arguments have been set.
     */
    private void init()
        throws IllegalStateException
    {

        if (experimentName == null || readerTrain == null || readerTest == null
                || aggregate == null) {
            throw new IllegalStateException(
                    "You must set Experiment Name, Test Reader, Training Reader and Aggregate.");
        }

        preprocessTaskTrain = new PreprocessTask();
        preprocessTaskTrain.setReader(readerTrain);
        preprocessTaskTrain.setAggregate(aggregate);
        preprocessTaskTrain.setType(preprocessTaskTrain.getType() + "-Train-" + experimentName);

        preprocessTaskTest = new PreprocessTask();
        preprocessTaskTest.setReader(readerTest);
        preprocessTaskTest.setAggregate(aggregate);
        preprocessTaskTest.setType(preprocessTaskTest.getType() + "-Test-" + experimentName);

        // get some meta data depending on the whole document collection that we need for training
        metaTask = new MetaInfoTask();
        metaTask.setType(metaTask.getType() + "-" + experimentName);
        metaTask.setMetaCollectorClasses(getMetaCollectorClasses());

        featuresTrainTask = new ExtractFeaturesTask();
        featuresTrainTask.setAddInstanceId(true);
        featuresTrainTask.setDataWriter(dataWriter);
        featuresTrainTask.setType(featuresTrainTask.getType() + "-Train-" + experimentName);
        featuresTrainTask.setMetaCollectorClasses(getMetaCollectorClasses());

        featuresTestTask = new ExtractFeaturesTask();
        featuresTestTask.setAddInstanceId(true);
        featuresTestTask.setDataWriter(dataWriter);
        featuresTestTask.setType(featuresTestTask.getType() + "-Test-" + experimentName);
        featuresTestTask.setMetaCollectorClasses(getMetaCollectorClasses());

        // Define the test task which operates on the results of the the train task
        testTask = new TestTask();
        testTask.setType(testTask.getType() + "-" + experimentName);
        testTask.addReport(FeatureValuesReport.class);
        testTask.addReport(TrainTestReport.class);
        testTask.addReport(OutcomeIDReport.class);

        // wiring
        metaTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY,
                preprocessTaskTrain.getType());
        featuresTrainTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY,
                preprocessTaskTrain.getType());
        featuresTrainTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY,
                metaTask.getType());
        featuresTestTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY,
                preprocessTaskTest.getType());
        featuresTestTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY,
                metaTask.getType());
        testTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());
        testTask.addImportLatest(TestTask.INPUT_KEY_TRAIN, ExtractFeaturesTask.OUTPUT_KEY,
                featuresTrainTask.getType());
        testTask.addImportLatest(TestTask.INPUT_KEY_TEST, ExtractFeaturesTask.OUTPUT_KEY,
                featuresTestTask.getType());

        addTask(preprocessTaskTrain);
        addTask(preprocessTaskTest);
        addTask(metaTask);
        addTask(featuresTrainTask);
        addTask(featuresTestTask);
        addTask(testTask);
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

    public void setReaderTest(CollectionReaderDescription aReader)
    {
        this.readerTest = aReader;
    }

    public void setReaderTrain(CollectionReaderDescription aReader)
    {
        this.readerTrain = aReader;
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

    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        return metaCollectorClasses;
    }

    public void setMetaCollectorClasses(List<Class<? extends MetaCollector>> metaCollectorClasses)
    {
        this.metaCollectorClasses = metaCollectorClasses;
    }
}
