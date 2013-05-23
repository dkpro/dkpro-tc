package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.SingleLabelInstanceExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.CVReport;


public class BatchTaskCV
    extends BatchTask
{    
	private String experimentName;
	private CollectionReaderDescription reader;
	private AnalysisEngineDescription aggregate;	
	
    private PreprocessTask preprocessTask;
    private MetaInfoTask metaTask;
    private ExtractFeaturesTask trainTask;
    private CrossValidationTask cvTask;
    
    public BatchTaskCV(){/*needed for Groovy*/}
    
    public BatchTaskCV(String aExperimentName, CollectionReaderDescription aReader, AnalysisEngineDescription aAggregate)
    {
    	setExperimentName(aExperimentName);
    	setReader(aReader);
    	setAggregate(aAggregate);
    }
    
    /**
	 * Initializes the experiment. This is called automatically before
	 * execution. It's not done directly in the constructor, because we want to
	 * be able to use setters instead of the three-argument constructor.
	 * 
	 * @throws IllegalStateException if not all necessary arguments have been set.
	 */
    private void init() throws IllegalStateException{
    	
    	if(experimentName==null||reader==null||aggregate==null){
    		throw new IllegalStateException("You must set Experiment Name, Test Reader, Training Reader and Aggregate.");
    	}
    	
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
    
    @Override
	public void execute(TaskContext aContext) throws Exception{
    	init();
		super.execute(aContext);				
	}


	public void setExperimentName(String experimentName) {
		this.experimentName = experimentName;
	}


	public void setReader(CollectionReaderDescription reader) {
		this.reader = reader;
	}


	public void setAggregate(AnalysisEngineDescription aggregate) {
		this.aggregate = aggregate;
	}
}
