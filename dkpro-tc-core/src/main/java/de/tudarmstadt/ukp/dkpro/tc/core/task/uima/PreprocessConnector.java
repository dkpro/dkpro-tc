package de.tudarmstadt.ukp.dkpro.tc.core.task.uima;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;

public class PreprocessConnector 
	extends JCasAnnotator_ImplBase
{

	private int nrofProcessCalls;
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException
	{
		super.initialize(context);
		
		nrofProcessCalls = 0;
	}


	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		nrofProcessCalls++;
	}

	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException
	{
		super.collectionProcessComplete();
		// no documents?
		if (nrofProcessCalls == 0) {
			throw new AnalysisEngineProcessException(new TextClassificationException("There are no documents to process."));
		}
	}
}