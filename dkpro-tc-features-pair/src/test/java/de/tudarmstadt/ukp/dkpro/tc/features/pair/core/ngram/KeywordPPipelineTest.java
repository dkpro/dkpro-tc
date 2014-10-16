/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeatureStore;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.JsonDataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.KeywordNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.LuceneKeywordPMetaCollector;

public class KeywordPPipelineTest
    extends PPipelineTestBase
{
    @Test
    public void testSize1Features()
        throws Exception
    {
        KeywordPPipelineTest test = new KeywordPPipelineTest();
        test.initialize();
        test.parameters = new Object[] {
                LuceneKeywordPFE.PARAM_KEYWORD_NGRAM_MIN_N_VIEW1, 1,
                LuceneKeywordPFE.PARAM_KEYWORD_NGRAM_MAX_N_VIEW1, 1,
                LuceneKeywordPFE.PARAM_KEYWORD_NGRAM_MIN_N_VIEW2, 1,
                LuceneKeywordPFE.PARAM_KEYWORD_NGRAM_MAX_N_VIEW2, 1,
                KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MIN_N, 1,
                KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MAX_N, 1,
                LuceneKeywordPFE.PARAM_USE_VIEW1_KEYWORD_NGRAMS_AS_FEATURES, true,
                LuceneKeywordPFE.PARAM_USE_VIEW2_KEYWORD_NGRAMS_AS_FEATURES, true,
                LuceneKeywordPFE.PARAM_USE_VIEWBLIND_KEYWORD_NGRAMS_AS_FEATURES,
                true, KeywordNGramFeatureExtractorBase.PARAM_NGRAM_KEYWORDS_FILE,
                "src/test/resources/data/keywordlist.txt",
                LuceneKeywordPFE.PARAM_LUCENE_DIR, test.lucenePath };
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
        KeywordPPipelineTest test = new KeywordPPipelineTest();
        test.initialize();
        test.parameters = new Object[] {
                LuceneKeywordPFE.PARAM_KEYWORD_NGRAM_MIN_N_VIEW1, 3,
                LuceneKeywordPFE.PARAM_KEYWORD_NGRAM_MAX_N_VIEW1, 3,
                LuceneKeywordPFE.PARAM_KEYWORD_NGRAM_MIN_N_VIEW2, 3,
                LuceneKeywordPFE.PARAM_KEYWORD_NGRAM_MAX_N_VIEW2, 3,
                KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MIN_N, 3,
                KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MAX_N, 3,
                LuceneKeywordPFE.PARAM_USE_VIEW1_KEYWORD_NGRAMS_AS_FEATURES, true,
                LuceneKeywordPFE.PARAM_USE_VIEW2_KEYWORD_NGRAMS_AS_FEATURES, true,
                LuceneKeywordPFE.PARAM_USE_VIEWBLIND_KEYWORD_NGRAMS_AS_FEATURES,
                true, KeywordNGramFeatureExtractorBase.PARAM_NGRAM_KEYWORDS_FILE,
                "src/test/resources/data/keywordlist.txt",
                LuceneKeywordPFE.PARAM_LUCENE_DIR, test.lucenePath };
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
                Constants.LM_SINGLE_LABEL, Constants.FM_PAIR, SimpleFeatureStore.class.getName(),
                false, false,
                LuceneKeywordPFE.class.getName());
    }

    // can be overwritten
    @Override
    protected void getMetaCollector(List<Object> parameterList)
        throws ResourceInitializationException
    {
        metaCollector = AnalysisEngineFactory.createEngineDescription(
                LuceneKeywordPMetaCollector.class, parameterList.toArray());
    }

}
