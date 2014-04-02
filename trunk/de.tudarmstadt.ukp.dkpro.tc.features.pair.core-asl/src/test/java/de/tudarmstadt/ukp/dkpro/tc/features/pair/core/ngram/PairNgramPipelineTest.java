package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.JsonDataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;

public class PairNgramPipelineTest
    extends PairNgramFETestBase
{
    /**
     * Tests if just View1 ngrams are being extracted as features.
     * 
     * @throws Exception
     */
    @Test
    public void testView1Features()
        throws Exception
    {
        PairNgramPipelineTest test = new PairNgramPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW1,
                1, LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_VIEW1, 1,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, true,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath };
        test.runPipeline();
        assertTrue(test.featureNames.get(0).startsWith("view1NG"));
        assertEquals(test.featureNames.size(), 4);
        assertTrue(test.featureNames.contains("view1NG_mice"));
    }

    /**
     * Tests if just View2 ngrams are being extracted as features
     * 
     * @throws Exception
     */
    @Test
    public void testView2Features()
        throws Exception
    {
        PairNgramPipelineTest test = new PairNgramPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW2,
                1, LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_VIEW2, 1,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, true,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath };
        test.runPipeline();
        assertTrue(test.featureNames.get(0).startsWith("view2NG"));
        assertEquals(test.featureNames.size(), 4);
        assertTrue(test.featureNames.contains("view2NG_birds"));
    }

    @Test
    public void testViewBlindFeatures()
        throws Exception
    {
        PairNgramPipelineTest test = new PairNgramPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N, 1,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N, 1,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, true,
                LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath };
        test.runPipeline();
        assertTrue(test.featureNames.get(0).startsWith("allNG"));
        assertEquals(test.featureNames.size(), 6);
        assertTrue(test.featureNames.contains("allNG_mice"));
        assertTrue(test.featureNames.contains("allNG_birds"));
    }

    @Test
    public void testViewBlindFeaturesMarkedWithLocalView()
        throws Exception
    {
        PairNgramPipelineTest test = new PairNgramPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N, 1,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N, 1,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, true,
                LuceneNGramPairFeatureExtractor.PARAM_MARK_VIEWBLIND_NGRAMS_WITH_LOCAL_VIEW, true,
                LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath };
        test.runPipeline();
        assertTrue(test.featureNames.get(0).startsWith("view1allNG")
                || test.featureNames.get(0).startsWith("view2allNG"));
        assertEquals(test.featureNames.size(), 12);
        assertTrue(test.featureNames.contains("view1allNG_mice"));
        assertTrue(test.featureNames.contains("view2allNG_mice"));
        int pos = 0;
        int neg = 0;
        for (Feature feature : test.instanceList.get(0).getFeatures()) {
            Integer value = ((Double) feature.getValue()).intValue();
            if (value == 1) {
                pos++;
            }
            else {
                neg++;
            }
        }
        assertEquals(pos, 8);
        assertEquals(neg, 4);
    }

    /**
     * Tests non-default minN and maxN size values
     * 
     * @throws Exception
     */
    @Test
    public void testNonDefaultSizes()
        throws Exception
    {
        PairNgramPipelineTest test = new PairNgramPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW1,
                4, LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_VIEW1, 4,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW2, 4,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_VIEW2, 4,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N, 4,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N, 4,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, true,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, true,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, true,
                LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath };
        test.runPipeline();
        assertEquals(test.featureNames.size(), 4);
    }

    /**
     * Tests ngramLowerCase
     * 
     * @throws Exception
     */
    @Test
    public void testAlphabeticalCase()
        throws Exception
    {
        PairNgramPipelineTest test = new PairNgramPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPairFeatureExtractor.PARAM_NGRAM_LOWER_CASE,
                false, LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW1, 1,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_VIEW1, 1,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, true,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath };
        test.runPipeline();
        assertTrue(test.featureNames.contains("view1NG_Cats"));

        test = new PairNgramPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPairFeatureExtractor.PARAM_NGRAM_LOWER_CASE,
                true, LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW1, 1,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_VIEW1, 1,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, true,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath };
        test.runPipeline();
        assertTrue(test.featureNames.contains("view1NG_cats"));
    }

    /**
     * Tests the stopword list to make sure stopwords are not in ngrams. First part of test shows
     * ngrams with stopwords are present, and second part of test shows they have been removed.
     * 
     * @throws Exception
     */
    @Test
    public void testStopwordRemoval()
        throws Exception
    {
        PairNgramPipelineTest test = new PairNgramPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N, 1,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N, 1,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, true,
                LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath };
        test.runPipeline();
        assertEquals(test.featureNames.size(), 6);
        assertTrue(test.featureNames.contains("allNG_cats"));

        test = new PairNgramPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N, 1,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N, 1,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, true,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_STOPWORDS_FILE,
                "src/test/resources/data/stopwords.txt",
                LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath };
        test.runPipeline();
        assertEquals(test.featureNames.size(), 5);
        assertTrue(!test.featureNames.contains("allNG_cats"));
    }

    @Override
    protected void getFeatureExtractorCollector(List<Object> parameterList)
        throws ResourceInitializationException
    {
        featExtractorConnector = TaskUtils.getFeatureExtractorConnector(parameterList,
                outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_SINGLE_LABEL, Constants.FM_PAIR, false,
                LuceneNGramPairFeatureExtractor.class.getName());
    }
}
