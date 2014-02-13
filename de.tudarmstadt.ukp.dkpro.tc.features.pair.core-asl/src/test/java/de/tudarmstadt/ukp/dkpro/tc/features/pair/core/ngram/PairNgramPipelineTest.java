package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PairNgramPipelineTest
	extends PairNgramFETestBase
{
	@Test
	public void testView1Features() throws Exception{
		PairNgramPipelineTest test = new PairNgramPipelineTest();
		test.initialize();
        test.parameters = new Object[] {
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW1, 1,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_VIEW1, 1,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, true,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES,false,
                LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath
        };
        test.runPipeline();
        assertTrue(test.featureNames != null);
        assertTrue(test.featureNames.size() > 0);
        assertTrue(test.featureNames.get(0).startsWith("view1NG"));
	}
	@Test
	public void testView2Features() throws Exception{
		PairNgramPipelineTest test = new PairNgramPipelineTest();
		test.initialize();
        test.parameters = new Object[] {
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW2, 1,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_VIEW2, 1,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, true,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES,false,
                LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath
        };
        test.runPipeline();
        assertTrue(test.featureNames != null);
        assertTrue(test.featureNames.size() > 0);
        assertTrue(test.featureNames.get(0).startsWith("view2NG"));
	}
	
	
	
	
}
