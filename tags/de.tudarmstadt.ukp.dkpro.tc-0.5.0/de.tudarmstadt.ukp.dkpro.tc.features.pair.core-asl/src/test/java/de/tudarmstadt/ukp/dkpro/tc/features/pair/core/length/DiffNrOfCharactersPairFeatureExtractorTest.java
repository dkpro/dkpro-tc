package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.length;

import static de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.junit.Assert.assertEquals;

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

public class DiffNrOfCharactersPairFeatureExtractorTest extends PairFeatureTestBase{

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
		jcas2.setDocumentText("This is the text of view 2");
		engine.process(jcas2);
		
		DiffNrOfCharactersPairFeatureExtractor extractor = new DiffNrOfCharactersPairFeatureExtractor();
		List<Feature> features = extractor.extract(jcas1, jcas2);

		assertEquals(1, features.size());

		for (Feature feature : features) {
		    assertFeature("DiffNrOfCharacters", 16, feature);
		}

	}

}