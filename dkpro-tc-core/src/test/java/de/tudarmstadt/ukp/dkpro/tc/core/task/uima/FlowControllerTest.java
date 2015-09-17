package de.tudarmstadt.ukp.dkpro.tc.core.task.uima;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.FlowControllerFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;

public class FlowControllerTest {

	@Test
	public void flowControllerTest_valid() throws Exception {
		
		JCas jcas = run("test", true);
		
		for (Lemma lemma : JCasUtil.selectCovered(jcas, Lemma.class, new Token(jcas, 0, 0))) {
			assertTrue(lemma.getValue().equals("dummyTest"));
		}
	}
	
	@Test
	public void flowControllerTest_noDocText() throws Exception {
		
		JCas jcas = run("", true);
		
		for (Lemma lemma : JCasUtil.selectCovered(jcas, Lemma.class, new Token(jcas, 0, 0))) {
			assertFalse(lemma.getValue().equals("dummyTest"));
		}
	}
	
	private JCas run(String text, boolean setOutcome) 
		throws Exception
	{
		AnalysisEngineDescription desc = AnalysisEngineFactory.createEngineDescription(
		         FlowControllerFactory.createFlowControllerDescription(CasDropFlowController.class),
                 AnalysisEngineFactory.createEngineDescription(AfterAE.class)
		);

		AnalysisEngine engine = AnalysisEngineFactory.createEngine(desc);
		JCas jcas = engine.newJCas();
		jcas.setDocumentText(text);

		if (setOutcome) {
			TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
			outcome.addToIndexes();			
		}
		
		engine.process(jcas);
		
		return jcas;
	}
}
