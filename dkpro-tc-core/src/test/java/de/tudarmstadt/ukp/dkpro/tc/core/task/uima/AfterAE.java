package de.tudarmstadt.ukp.dkpro.tc.core.task.uima;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;

public class AfterAE
	extends JCasAnnotator_ImplBase
{

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Lemma dummy = new Lemma(aJCas, 0, 0);
		dummy.setValue("dummyTest");
		dummy.addToIndexes();
	}

}
