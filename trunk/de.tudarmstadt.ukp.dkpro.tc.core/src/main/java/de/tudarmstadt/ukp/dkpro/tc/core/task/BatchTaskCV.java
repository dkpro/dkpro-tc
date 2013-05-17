package de.tudarmstadt.ukp.dkpro.tc.core.task;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;

import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.SingleLabelInstanceExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.report.CVReport;

public class BatchTaskCV
    extends BatchTask
{
    
    private PreprocessTask preprocessTask;
    private MetaInfoTask metaTask;
    private ExtractFeaturesTask trainTask;
    private CrossValidationTask cvTask;
    
    public BatchTaskCV(String experimentName, CollectionReaderDescription reader, AnalysisEngineDescription aggregate)
    {        
        preprocessTask = new PreprocessTask();
        preprocessTask.setReader(reader);
        preprocessTask.setAggregate(aggregate);
        preprocessTask.setType(preprocessTask.getType() + "-" + experimentName);

        // get some meta data depending on the whole document collection that we need for training
        metaTask = new MetaInfoTask();
        metaTask.setType(metaTask.getType() + "-" + experimentName);

        // Define the base task which generates an arff instances file
        trainTask = new ExtractFeaturesTask();
        trainTask.setAddInstanceId(false);
        trainTask.setInstanceExtractor(SingleLabelInstanceExtractor.class);
        trainTask.setType(trainTask.getType() + "-" + experimentName);
        
        // Define the cross-validation task which operates on the results of the the train task
        cvTask = new CrossValidationTask();
        cvTask.setType(cvTask.getType() + "-" + experimentName);
        cvTask.addReport(CVReport.class);
        
        // wiring
        metaTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY,
                preprocessTask.getType());
        trainTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY,
                preprocessTask.getType());
        trainTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());
        cvTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());
        cvTask.addImportLatest(CrossValidationTask.INPUT_KEY, ExtractFeaturesTask.OUTPUT_KEY,
                trainTask.getType());
        
        addTask(preprocessTask);
        addTask(metaTask);
        addTask(trainTask);
        addTask(cvTask);
    }
}
