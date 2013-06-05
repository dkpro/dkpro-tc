package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.length;

import static de.tudarmstadt.ukp.dkpro.tc.features.pair.core.FeatureTestUtil.assertFeature;
import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.PairFeatureTestBase;

public class DiffNrOfSentencesPairFeatureExtractorTest extends PairFeatureTestBase{

	@Test
	public void testExtract() throws ResourceInitializationException, AnalysisEngineProcessException, TextClassificationException {
		AnalysisEngineDescription desc = createAggregateDescription(
		        createPrimitiveDescription(BreakIteratorSegmenter.class)
		);
		AnalysisEngine engine = createPrimitive(desc);

		JCas jcas1 = engine.newJCas();
		jcas1.setDocumentLanguage("en");
		jcas1.setDocumentText("This is the text of view 1. And some more.");
		engine.process(jcas1);

		JCas jcas2 = engine.newJCas();
		jcas2.setDocumentLanguage("en");
		jcas2.setDocumentText("This is the text of view 2.");
		engine.process(jcas2);
		
		DiffNrOfSentencesPairFeatureExtractor extractor = new DiffNrOfSentencesPairFeatureExtractor();
		List<Feature> features = extractor.extract(jcas1, jcas2);

		assertEquals(1, features.size());

		for (Feature feature : features) {
		    assertFeature("DiffNrOfSentences", 1, feature);
		}

	}

}