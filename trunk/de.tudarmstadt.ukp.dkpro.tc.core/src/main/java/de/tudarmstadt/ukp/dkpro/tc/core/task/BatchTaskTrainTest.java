package de.tudarmstadt.ukp.dkpro.tc.core.task;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;

import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.SingleLabelInstanceExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.report.OutcomeReport;
import de.tudarmstadt.ukp.dkpro.tc.core.report.TestReport;

public class BatchTaskTrainTest
    extends BatchTask
{
    
    private PreprocessTask preprocessTaskTrain;
    private PreprocessTask preprocessTaskTest;
    private MetaInfoTask metaTask;
    private ExtractFeaturesTask featuresTrainTask;
    private ExtractFeaturesTask featuresTestTask;
    private TestTask testTask;
    
    public BatchTaskTrainTest(String experimentName, CollectionReaderDescription readerTrain, CollectionReaderDescription readerTest, AnalysisEngineDescription aggregate)
    {        
        preprocessTaskTrain = new PreprocessTask();
        preprocessTaskTrain.setReader(readerTrain);
        preprocessTaskTrain.setAggregate(aggregate);
        preprocessTaskTrain.setType(preprocessTaskTrain.getType() + "-train-" + experimentName);

        preprocessTaskTest = new PreprocessTask();
        preprocessTaskTest.setReader(readerTest);
        preprocessTaskTest.setAggregate(aggregate);
        preprocessTaskTrain.setType(preprocessTaskTest.getType() + "-test-" + experimentName);

        // get some meta data depending on the whole document collection that we need for training
        metaTask = new MetaInfoTask();
        metaTask.setType(metaTask.getType() + "-" + experimentName);

        featuresTrainTask = new ExtractFeaturesTask();
        featuresTrainTask.setAddInstanceId(true);
        featuresTrainTask.setInstanceExtractor(SingleLabelInstanceExtractor.class);
        featuresTrainTask.setType(featuresTrainTask.getType() + "-train-" + experimentName);

        featuresTestTask = new ExtractFeaturesTask();
        featuresTestTask.setAddInstanceId(true);
        featuresTestTask.setInstanceExtractor(SingleLabelInstanceExtractor.class);
                featuresTestTask.setType(featuresTestTask.getType() + "-test-" + experimentName);

        // Define the test task which operates on the results of the the train task
        testTask = new TestTask();
        testTask.setType(testTask.getType() + "-" + experimentName);
        testTask.addReport(TestReport.class);
        testTask.addReport(OutcomeReport.class);
        
        
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
}
