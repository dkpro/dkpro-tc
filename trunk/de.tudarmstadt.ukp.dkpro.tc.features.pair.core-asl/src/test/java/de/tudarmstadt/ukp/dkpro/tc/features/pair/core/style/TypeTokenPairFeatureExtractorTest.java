package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.style;

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
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class TypeTokenPairFeatureExtractorTest {

	JCas jcas1;
	JCas jcas2;

	@Before
	public void setUp() throws ResourceInitializationException, AnalysisEngineProcessException{
        AnalysisEngineDescription desc = createAggregateDescription(
                createPrimitiveDescription(BreakIteratorSegmenter.class)
        );
        AnalysisEngine engine = createPrimitive(desc);
        
        jcas1 = engine.newJCas();
        jcas1.setDocumentLanguage("en");
        jcas1.setDocumentText("This is text");
        engine.process(jcas1);
        
        Lemma lemma1 = new Lemma(jcas1, 0, 4);
        lemma1.setValue("text");
        lemma1.addToIndexes();
        
        Lemma lemma2 = new Lemma(jcas1, 5, 7);
        lemma2.setValue("is");
        lemma2.addToIndexes();
        
        Lemma lemma3 = new Lemma(jcas1, 8, 10);
        lemma3.setValue("text");
        lemma3.addToIndexes();
        
        jcas2 = engine.newJCas();
        jcas2.setDocumentLanguage("en");
        jcas2.setDocumentText("Text is text");
        engine.process(jcas2);
        
        Lemma lemma4 = new Lemma(jcas2, 0, 4);
        lemma4.setValue("text");
        lemma4.addToIndexes();
        
        Lemma lemma5 = new Lemma(jcas2, 8, 10);
        lemma5.setValue("text");
        lemma5.addToIndexes();
        

	}

	@Test
	public void testExtract() throws TextClassificationException {
		TypeTokenPairFeatureExtractor extractor = new TypeTokenPairFeatureExtractor();
		List<Feature> features = extractor.extract(jcas1, jcas2);

		assertEquals(1, features.size());

		for (Feature feature : features) {
		    assertFeature("DiffTypeTokenRatio", 1.33, feature, 0.1);
		}

	}

}
