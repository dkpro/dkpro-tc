/*******************************************************************************
 * Copyright 2019
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.features.pair.core.ngram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.JsonDataWriter;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.features.pair.core.ngram.meta.LuceneNGramPMetaCollector;
import org.junit.Test;

public class NGramPPipelineTest
    extends PPipelineTestBase
{
    /**
     * Tests if just View1 ngrams are being extracted as features.
     * 
     * @throws Exception
     */
    @Test
    public void testView1Features() throws Exception
    {
        NGramPPipelineTest test = new NGramPPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPFE.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                LuceneNGramPFE.PARAM_NGRAM_MIN_N_VIEW1, 1, LuceneNGramPFE.PARAM_NGRAM_MAX_N_VIEW1,
                1, LuceneNGramPFE.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, true,
                LuceneNGramPFE.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramPFE.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                LuceneNGramPFE.PARAM_SOURCE_LOCATION, test.lucenePath,
                LuceneNGramPMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath };
        test.runPipeline();
        assertTrue(test.featureNames.first().startsWith("view1NG"));
        assertEquals(test.featureNames.size(), 4);
        assertTrue(test.featureNames.contains("view1NG_mice"));
    }

    /**
     * Tests if just View2 ngrams are being extracted as features
     * 
     * @throws Exception
     */
    @Test
    public void testView2Features() throws Exception
    {
        NGramPPipelineTest test = new NGramPPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPFE.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                LuceneNGramPFE.PARAM_NGRAM_MIN_N_VIEW2, 1, LuceneNGramPFE.PARAM_NGRAM_MAX_N_VIEW2,
                1, LuceneNGramPFE.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                LuceneNGramPFE.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, true,
                LuceneNGramPFE.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                LuceneNGramPFE.PARAM_SOURCE_LOCATION, test.lucenePath,
                LuceneNGramPMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath };
        test.runPipeline();
        assertTrue(test.featureNames.first().startsWith("view2NG"));
        assertEquals(test.featureNames.size(), 4);
        assertTrue(test.featureNames.contains("view2NG_birds"));
    }

    @Test
    public void testViewBlindFeatures() throws Exception
    {
        NGramPPipelineTest test = new NGramPPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPFE.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                LuceneNGramPFE.PARAM_NGRAM_MIN_N, 1, LuceneNGramPFE.PARAM_NGRAM_MAX_N, 1,
                LuceneNGramPFE.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                LuceneNGramPFE.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramPFE.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, true,
                LuceneNGramPFE.PARAM_SOURCE_LOCATION, test.lucenePath,
                LuceneNGramPMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath };
        test.runPipeline();
        assertTrue(test.featureNames.first().startsWith("allNG"));
        assertEquals(test.featureNames.size(), 6);
        assertTrue(test.featureNames.contains("allNG_mice"));
        assertTrue(test.featureNames.contains("allNG_birds"));
    }

    @Test
    public void testViewBlindFeaturesMarkedWithLocalView() throws Exception
    {
        NGramPPipelineTest test = new NGramPPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPFE.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                LuceneNGramPFE.PARAM_NGRAM_MIN_N, 1, LuceneNGramPFE.PARAM_NGRAM_MAX_N, 1,
                LuceneNGramPFE.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                LuceneNGramPFE.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramPFE.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, true,
                LuceneNGramPFE.PARAM_MARK_VIEWBLIND_NGRAMS_WITH_LOCAL_VIEW, true,
                LuceneNGramPFE.PARAM_SOURCE_LOCATION, test.lucenePath,
                LuceneNGramPMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath };
        test.runPipeline();
        assertTrue(test.featureNames.first().startsWith("view1allNG")
                || test.featureNames.first().startsWith("view2allNG"));
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
    public void testNonDefaultSizes() throws Exception
    {
        NGramPPipelineTest test = new NGramPPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPFE.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                LuceneNGramPFE.PARAM_NGRAM_MIN_N_VIEW1, 4, LuceneNGramPFE.PARAM_NGRAM_MAX_N_VIEW1,
                4, LuceneNGramPFE.PARAM_NGRAM_MIN_N_VIEW2, 4,
                LuceneNGramPFE.PARAM_NGRAM_MAX_N_VIEW2, 4, LuceneNGramPFE.PARAM_NGRAM_MIN_N, 4,
                LuceneNGramPFE.PARAM_NGRAM_MAX_N, 4,
                LuceneNGramPFE.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, true,
                LuceneNGramPFE.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, true,
                LuceneNGramPFE.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, true,
                LuceneNGramPFE.PARAM_SOURCE_LOCATION, test.lucenePath,
                LuceneNGramPMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath };
        test.runPipeline();
        assertEquals(test.featureNames.size(), 4);
    }

    /**
     * Tests ngramLowerCase
     * 
     * @throws Exception
     */
    @Test
    public void testAlphabeticalCase() throws Exception
    {
        NGramPPipelineTest test = new NGramPPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPFE.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                LuceneNGramPFE.PARAM_NGRAM_LOWER_CASE, false,
                LuceneNGramPFE.PARAM_NGRAM_MIN_N_VIEW1, 1, LuceneNGramPFE.PARAM_NGRAM_MAX_N_VIEW1,
                1, LuceneNGramPFE.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, true,
                LuceneNGramPFE.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramPFE.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                LuceneNGramPFE.PARAM_SOURCE_LOCATION, test.lucenePath,
                LuceneNGramPMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath };
        test.runPipeline();
        assertTrue(test.featureNames.contains("view1NG_Cats"));

        test = new NGramPPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPFE.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                LuceneNGramPFE.PARAM_NGRAM_LOWER_CASE, true, LuceneNGramPFE.PARAM_NGRAM_MIN_N_VIEW1,
                1, LuceneNGramPFE.PARAM_NGRAM_MAX_N_VIEW1, 1,
                LuceneNGramPFE.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, true,
                LuceneNGramPFE.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramPFE.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                LuceneNGramPFE.PARAM_SOURCE_LOCATION, test.lucenePath,
                LuceneNGramPMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath };
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
    public void testStopwordRemoval() throws Exception
    {
        NGramPPipelineTest test = new NGramPPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPFE.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                LuceneNGramPFE.PARAM_NGRAM_MIN_N, 1, LuceneNGramPFE.PARAM_NGRAM_MAX_N, 1,
                LuceneNGramPFE.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                LuceneNGramPFE.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramPFE.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, true,
                LuceneNGramPFE.PARAM_SOURCE_LOCATION, test.lucenePath,
                LuceneNGramPMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath };
        test.runPipeline();
        assertEquals(test.featureNames.size(), 6);
        assertTrue(test.featureNames.contains("allNG_cats"));

        test = new NGramPPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramPFE.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                LuceneNGramPFE.PARAM_NGRAM_MIN_N, 1, LuceneNGramPFE.PARAM_NGRAM_MAX_N, 1,
                LuceneNGramPFE.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                LuceneNGramPFE.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramPFE.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, true,
                LuceneNGramPFE.PARAM_NGRAM_STOPWORDS_FILE, "src/test/resources/data/stopwords.txt",
                LuceneNGramPFE.PARAM_SOURCE_LOCATION, test.lucenePath,
                LuceneNGramPMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath };
        test.runPipeline();
        assertEquals(test.featureNames.size(), 5);
        assertTrue(!test.featureNames.contains("allNG_cats"));
    }

    @Override
    protected void getFeatureExtractorCollector(List<Object> parameterList)
        throws ResourceInitializationException
    {

        ExternalResourceDescription featureExtractor = ExternalResourceFactory
                .createExternalResourceDescription(LuceneNGramPFE.class,
                        toString(parameterList.toArray()));
        List<ExternalResourceDescription> fes = new ArrayList<>();
        fes.add(featureExtractor);

        featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_SINGLE_LABEL, Constants.FM_PAIR, false, false, false, false,
                Collections.emptyList(), fes, new String[] {});
    }

    private Object[] toString(Object[] array)
    {
        List<Object> out = new ArrayList<>();
        for (Object o : array) {
            out.add(o.toString());
        }

        return out.toArray();
    }
}
