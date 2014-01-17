package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ne;

import static de.tudarmstadt.ukp.dkpro.tc.features.util.FeatureTestUtil.assertFeature;
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

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.IFeature;

public class SharedNEsFeatureExtractorTest {
	
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
        jcas1.setDocumentText("This is the text of view 1");
        engine.process(jcas1);
        
        jcas2 = engine.newJCas();
        jcas2.setDocumentLanguage("en");
        jcas2.setDocumentText("This is the text of view 2");
        engine.process(jcas2);
	}

	@Test
    public void extractTest1()
        throws Exception
    {
		NamedEntity ne1 = new NamedEntity(jcas1, 0, 4);
		ne1.addToIndexes();
		
        SharedNEsFeatureExtractor extractor = new SharedNEsFeatureExtractor();
        List<IFeature> features = extractor.extract(jcas1, jcas2);

        assertEquals(1, features.size());
        
        for (IFeature feature : features) {
            assertFeature("SharedNEs", false, feature);
        }
        
		NamedEntity ne2 = new NamedEntity(jcas2, 0, 4);
		ne2.addToIndexes();
		
        features = extractor.extract(jcas1, jcas2);

        assertEquals(1, features.size());
        
        for (IFeature feature : features) {
            assertFeature("SharedNEs", true, feature);
        }
    }

}
