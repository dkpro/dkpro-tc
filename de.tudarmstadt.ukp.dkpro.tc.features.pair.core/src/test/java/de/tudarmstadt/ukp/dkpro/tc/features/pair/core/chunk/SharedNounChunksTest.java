package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.chunk;

import static de.tudarmstadt.ukp.dkpro.tc.features.pair.core.FeatureTestUtil.assertFeature;
import static org.junit.Assert.*;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

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
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class SharedNounChunksTest {

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
    public void testExtract1()
        throws Exception
    {
		Chunk chunk1 = new Chunk(jcas1, 0, 4);
		chunk1.addToIndexes();
		
		Chunk chunk2 = new Chunk(jcas2, 0, 4);
		chunk2.addToIndexes();
		
		SharedNounChunks extractor = new SharedNounChunks(true);
        List<Feature> features = extractor.extract(jcas1, jcas2);

        assertEquals(1, features.size());
        
        for (Feature feature : features) {
            assertFeature("SharedNounChunkView1", 1.0, feature, 0.0001);
        }
        
		Chunk chunk3 = new Chunk(jcas1, 5, 7);
		chunk3.addToIndexes();
		
		features = extractor.extract(jcas1, jcas2);

        assertEquals(1, features.size());
        
        for (Feature feature : features) {
            assertFeature("SharedNounChunkView1", 0.5, feature, 0.0001);
        }

    }
	@Test
    public void testExtract2()
        throws Exception
    {
		Chunk chunk1 = new Chunk(jcas1, 0, 4);
		chunk1.addToIndexes();
		
		Chunk chunk2 = new Chunk(jcas2, 0, 4);
		chunk2.addToIndexes();
		
		SharedNounChunks extractor = new SharedNounChunks(false);
        List<Feature> features = extractor.extract(jcas1, jcas2);

        assertEquals(1, features.size());
        
        for (Feature feature : features) {
            assertFeature("SharedNounChunkView2", 1.0, feature, 0.0001);
        }
        
		Chunk chunk3 = new Chunk(jcas1, 5, 7);
		chunk3.addToIndexes();
		
		features = extractor.extract(jcas1, jcas2);

        assertEquals(1, features.size());
        
        for (Feature feature : features) {
            assertFeature("SharedNounChunkView2", 1, feature, 0.0001);
        }

    }

}
