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

import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.JsonDataWriter;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.features.pair.core.ngram.meta.LuceneNGramCPMetaCollector;
import org.dkpro.tc.features.pair.core.ngram.meta.LuceneNGramPMetaCollector;
import org.junit.Test;

public class NGramCPPipelineTest
    extends PPipelineTestBase
{
    @Test
    public void testComboFeatures_defaults() throws Exception
    {
        NGramCPPipelineTest test = new NGramCPPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramCPFE.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                LuceneNGramCPFE.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                LuceneNGramCPFE.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramCPFE.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                LuceneNGramCPFE.PARAM_SOURCE_LOCATION, test.lucenePath,
                LuceneNGramPMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath };
        test.runPipeline();
        assertTrue(test.featureNames.first().startsWith("comboNG"));
        assertEquals(test.featureNames.size(), 65);
        assertTrue(test.featureNames.contains("comboNG_mice_ANDbirds"));
        assertTrue(test.featureNames.contains("comboNG_cats_eat_ANDcats"));
        assertTrue(test.featureNames.contains("comboNG_cats_eat_ANDbirds_chase"));

        for (String f : test.featureNames) {
            int size = f.length() - f.replace("_", "").length();
            assertTrue(size >= 2);
            assertTrue(size <= 4);
        }
    }

    @Test
    public void testComboFeatures_size1() throws Exception
    {
        NGramCPPipelineTest test = new NGramCPPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramCPFE.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                LuceneNGramCPFE.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                LuceneNGramCPFE.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramCPFE.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                LuceneNGramCPFE.PARAM_SOURCE_LOCATION, test.lucenePath,
                LuceneNGramPMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath,
                LuceneNGramCPFE.PARAM_NGRAM_MAX_N_COMBO, 2 };
        test.runPipeline();
        assertTrue(test.featureNames.first().startsWith("comboNG"));
        assertEquals(test.featureNames.size(), 16);
        assertTrue(test.featureNames.contains("comboNG_mice_ANDbirds"));
    }

    @Test
    public void testComboFeatures_size3() throws Exception
    {
        NGramCPPipelineTest test = new NGramCPPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramCPFE.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                LuceneNGramCPFE.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                LuceneNGramCPFE.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramCPFE.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                LuceneNGramCPFE.PARAM_SOURCE_LOCATION, test.lucenePath,
                LuceneNGramPMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath,
                LuceneNGramCPFE.PARAM_NGRAM_MAX_N_COMBO, 6 };
        test.runPipeline();
        assertTrue(test.featureNames.first().startsWith("comboNG"));
        assertEquals(test.featureNames.size(), 81);
        assertTrue(test.featureNames.contains("comboNG_cats_eat_mice_ANDbirds_chase_cats"));
    }

    // TODO: Write a symmetry test. Note that features will be the same. Needs different dataset.

    @Test
    public void testNonBinaryFeatureValues() throws Exception
    {
        NGramCPPipelineTest test = new NGramCPPipelineTest();
        test.initialize();
        test.parameters = new Object[] { LuceneNGramCPFE.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                LuceneNGramCPFE.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, false,
                LuceneNGramCPFE.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, false,
                LuceneNGramCPFE.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                LuceneNGramCPFE.PARAM_SOURCE_LOCATION, test.lucenePath,
                LuceneNGramPMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath,
                LuceneNGramCPFE.PARAM_NGRAM_BINARY_FEATURE_VALUES_COMBO, false,
                LuceneNGramCPFE.PARAM_NGRAM_MAX_N_COMBO, 2,
                LuceneNGramCPFE.PARAM_NGRAM_SYMMETRY_COMBO, true };
        test.runPipeline();
        int two = 0;
        int one = 0;
        int zero = 0;
        for (Feature feature : test.instanceList.get(0).getFeatures()) {
            Integer value = ((Double) feature.getValue()).intValue();
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
        ExternalResourceDescription featureExtractor = ExternalResourceFactory
                .createExternalResourceDescription(LuceneNGramCPFE.class,
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

    @Override
    protected void getMetaCollector(List<Object> parameterList)
        throws ResourceInitializationException
    {
        metaCollector = AnalysisEngineFactory
                .createEngineDescription(LuceneNGramCPMetaCollector.class, parameterList.toArray());
    }
}
