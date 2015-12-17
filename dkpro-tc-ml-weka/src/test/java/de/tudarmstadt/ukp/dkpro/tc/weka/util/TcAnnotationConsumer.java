package de.tudarmstadt.ukp.dkpro.tc.weka.util;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;

/**
 * Prints outcome annotations to the console.
 */
public class TcAnnotationConsumer extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		for(TextClassificationOutcome outcome : JCasUtil.select(aJCas, TextClassificationOutcome.class)){
			System.out.println(outcome.getOutcome());
		}
	}
}
