package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.JsonDataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.KeywordNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.KeywordNGramPairMetaCollector;

public class PairKeywordNgramsTest
    extends PairNgramFETestBase
{
    @Test
    public void testSize1Features()
        throws Exception
    {
        PairKeywordNgramsTest test = new PairKeywordNgramsTest();
        test.initialize();
        test.parameters = new Object[] {
                KeywordNGramPairFeatureExtractor.PARAM_KEYWORD_NGRAM_MIN_N_VIEW1, 1,
                KeywordNGramPairFeatureExtractor.PARAM_KEYWORD_NGRAM_MAX_N_VIEW1, 1,
                KeywordNGramPairFeatureExtractor.PARAM_KEYWORD_NGRAM_MIN_N_VIEW2, 1,
                KeywordNGramPairFeatureExtractor.PARAM_KEYWORD_NGRAM_MAX_N_VIEW2, 1,
                KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MIN_N, 1,
                KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MAX_N, 1,
                KeywordNGramPairFeatureExtractor.PARAM_USE_VIEW1_KEYWORD_NGRAMS_AS_FEATURES, true,
                KeywordNGramPairFeatureExtractor.PARAM_USE_VIEW2_KEYWORD_NGRAMS_AS_FEATURES, true,
                KeywordNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_KEYWORD_NGRAMS_AS_FEATURES,
                true, KeywordNGramFeatureExtractorBase.PARAM_NGRAM_KEYWORDS_FILE,
                "src/test/resources/data/keywordlist.txt",
                KeywordNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath };
        test.runPipeline();
        assertEquals(test.featureNames.size(), 16);
        assertTrue(test.featureNames.contains("keyNG1_peach"));
        assertTrue(test.featureNames.contains("keyNG2_SB"));
        assertTrue(test.featureNames.contains("keyNG_nectarine"));
        assertTrue(!test.featureNames.contains("keyNG2_cherry"));
    }

    @Test
    public void testSize3Features()
        throws Exception
    {
        PairKeywordNgramsTest test = new PairKeywordNgramsTest();
        test.initialize();
        test.parameters = new Object[] {
                KeywordNGramPairFeatureExtractor.PARAM_KEYWORD_NGRAM_MIN_N_VIEW1, 3,
                KeywordNGramPairFeatureExtractor.PARAM_KEYWORD_NGRAM_MAX_N_VIEW1, 3,
                KeywordNGramPairFeatureExtractor.PARAM_KEYWORD_NGRAM_MIN_N_VIEW2, 3,
                KeywordNGramPairFeatureExtractor.PARAM_KEYWORD_NGRAM_MAX_N_VIEW2, 3,
                KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MIN_N, 3,
                KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MAX_N, 3,
                KeywordNGramPairFeatureExtractor.PARAM_USE_VIEW1_KEYWORD_NGRAMS_AS_FEATURES, true,
                KeywordNGramPairFeatureExtractor.PARAM_USE_VIEW2_KEYWORD_NGRAMS_AS_FEATURES, true,
                KeywordNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_KEYWORD_NGRAMS_AS_FEATURES,
                true, KeywordNGramFeatureExtractorBase.PARAM_NGRAM_KEYWORDS_FILE,
                "src/test/resources/data/keywordlist.txt",
                KeywordNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath };
        test.runPipeline();
        assertEquals(test.featureNames.size(), 12);
        assertTrue(test.featureNames.contains("keyNG1_peach_nectarine_SB"));
        assertTrue(test.featureNames.contains("keyNG2_apricot_peach_nectarine"));
        assertTrue(test.featureNames.contains("keyNG_SB_crabapple_SB"));
    }

    @Override
    protected String setTestPairsLocation()
    {
        return "src/test/resources/data/keywordNgramsData.txt";
    }

    @Override
    protected void getFeatureExtractorCollector(List<Object> parameterList)
        throws ResourceInitializationException
    {
        featExtractorConnector = TaskUtils.getFeatureExtractorConnector(parameterList,
                outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_SINGLE_LABEL, Constants.FM_PAIR, false,
                KeywordNGramPairFeatureExtractor.class.getName());
    }

    // can be overwritten
    @Override
    protected void getMetaCollector(List<Object> parameterList)
        throws ResourceInitializationException
    {
        metaCollector = AnalysisEngineFactory.createEngineDescription(
                KeywordNGramPairMetaCollector.class, parameterList.toArray());
    }

}
