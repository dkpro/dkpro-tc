package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.chunk;

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

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.IFeature;

public class DiffNounChunkTokenLengthTest {
	
	private JCas jcas1;
	private JCas jcas2;
	
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
		Chunk chunk1 = new Chunk(jcas1, 0, 4);
		chunk1.addToIndexes();
		
		Chunk chunk2 = new Chunk(jcas2, 0, 4);
		chunk2.addToIndexes();
		
		DiffNounChunkTokenLength extractor = new DiffNounChunkTokenLength();
        List<IFeature> features = extractor.extract(jcas1, jcas2);

        assertEquals(1, features.size());
        
        for (IFeature feature : features) {
            assertFeature("DiffNounPhraseTokenLength", 0.0, feature, 0.0001);
        }
    }

	@Test
    public void extractTest2()
        throws Exception
    {
		Chunk chunk1 = new Chunk(jcas1, 0, 4);
		chunk1.addToIndexes();
		
		Chunk chunk2 = new Chunk(jcas2, 0, 7);
		chunk2.addToIndexes();
		
		DiffNounChunkTokenLength extractor = new DiffNounChunkTokenLength();
        List<IFeature> features = extractor.extract(jcas1, jcas2);

        assertEquals(1, features.size());
        
        for (IFeature feature : features) {
            assertFeature("DiffNounPhraseTokenLength", -1.0, feature, 0.0001);
        }
    }

}
