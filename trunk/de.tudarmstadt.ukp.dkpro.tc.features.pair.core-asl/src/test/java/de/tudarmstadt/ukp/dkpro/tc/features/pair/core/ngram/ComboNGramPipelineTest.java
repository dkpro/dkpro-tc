package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.JsonDataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;

public class ComboNGramPipelineTest
    extends PairNgramFETestBase
{
    @Test
    public void testComboFeatures_defaults()
        throws Exception
    {
        ComboNGramPipelineTest test = new ComboNGramPipelineTest();
        test.initialize();
        test.parameters = new Object[] {
                CombinedNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                CombinedNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                CombinedNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                CombinedNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath, };
        test.runPipeline();
        assertTrue(test.featureNames.get(0).startsWith("comboNG"));
        assertEquals(test.featureNames.size(), 65);
        assertTrue(test.featureNames.contains("comboNG_mice_birds"));
        assertTrue(test.featureNames.contains("comboNG_cats_eat_cats"));
        assertTrue(test.featureNames.contains("comboNG_cats_eat_birds_chase"));
        for (String value : test.instanceList.get(0)) {
            assertEquals(1, Integer.parseInt(value));
        }
        for (String f : test.featureNames) {
            int size = f.length() - f.replace("_", "").length();
            assertTrue(size >= 2);
            assertTrue(size <= 4);
        }
    }

    @Test
    public void testComboFeatures_size1()
        throws Exception
    {
        ComboNGramPipelineTest test = new ComboNGramPipelineTest();
        test.initialize();
        test.parameters = new Object[] {
                CombinedNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                CombinedNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                CombinedNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                CombinedNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath,
                CombinedNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_COMBO, 2 };
        test.runPipeline();
        assertTrue(test.featureNames.get(0).startsWith("comboNG"));
        assertEquals(test.featureNames.size(), 16);
        assertTrue(test.featureNames.contains("comboNG_mice_birds"));
        for (String value : test.instanceList.get(0)) {
            assertEquals(1, Integer.parseInt(value));
        }
    }

    @Test
    public void testComboFeatures_size3()
        throws Exception
    {
        ComboNGramPipelineTest test = new ComboNGramPipelineTest();
        test.initialize();
        test.parameters = new Object[] {
                CombinedNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                CombinedNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                CombinedNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                CombinedNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath,
                CombinedNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_COMBO, 6 };
        test.runPipeline();
        assertTrue(test.featureNames.get(0).startsWith("comboNG"));
        assertEquals(test.featureNames.size(), 81);
        assertTrue(test.featureNames.contains("comboNG_cats_eat_mice_birds_chase_cats"));
        for (String value : test.instanceList.get(0)) {
            assertEquals(1, Integer.parseInt(value));
        }
    }

    // TODO: Write a symmetry test. Note that features will be the same. Needs different dataset.

    @Test
    public void testNonBinaryFeatureValues()
        throws Exception
    {
        ComboNGramPipelineTest test = new ComboNGramPipelineTest();
        test.initialize();
        test.parameters = new Object[] {
                CombinedNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                CombinedNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                CombinedNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                CombinedNGramPairFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath,
                CombinedNGramPairFeatureExtractor.PARAM_NGRAM_BINARY_FEATURE_VALUES_COMBO, false,
                CombinedNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_COMBO, 2,
                CombinedNGramPairFeatureExtractor.PARAM_NGRAM_SYMMETRY_COMBO, true };
        test.runPipeline();
        int two = 0;
        int one = 0;
        int zero = 0;
        for (String value : test.instanceList.get(0)) {
            if (new Integer(value) == 2) {
                two++;
            }
            if (new Integer(value) == 1) {
                one++;
            }
            if (new Integer(value) == 0) {
                zero++;
            }
        }
        assertEquals(two, 4);
        assertEquals(one, 12);
        assertEquals(zero, 0);
    }

    @Override
    protected void getFeatureExtractorCollector(List<Object> parameterList)
        throws ResourceInitializationException
    {
        featExtractorConnector = TaskUtils.getFeatureExtractorConnector(parameterList,
                outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_SINGLE_LABEL, Constants.FM_DOCUMENT, false,
                CombinedNGramPairFeatureExtractor.class.getName());
    }
}
