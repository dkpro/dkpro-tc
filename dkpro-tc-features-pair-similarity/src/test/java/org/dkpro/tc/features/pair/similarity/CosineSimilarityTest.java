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
package org.dkpro.tc.features.pair.similarity;

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
import org.junit.Test;

public class CosineSimilarityTest
    extends PPipelineTestBase
{
    private static final double epsilon = 0.0001;

    /**
     * Tests TFIDF Cosine Similarity with TF weight FREQUENCY_LOGPLUSONE, IDF weight PASSTHROUGH,
     * and normalization mode L2. <br />
     * 
     * Answer 0.2 for Tokens confirmed by following equation 15.2, pg 541, in Manning and Schuetze.
     * <br />
     * Vector1 = 1,.5,0,1,.5,0 <br />
     * Vector2 = 0,.5,1,0,.5,1 <br />
     * Sum of vector products (svp) = (1x0)+(.5x.5)+(0x1)+(1x0)+(.5x.5)+(0x1) =.5 <br />
     * normVector1 = sqrt(sum(i in finalVector1, ^2)) = sqrt(1+.25+0+1+.25+0) = 1.58 <br />
     * normVector2 = sqrt(sum(i in finalVector2, ^2)) = sqrt(0+.25+1+0+.25+1) = 1.58 <br />
     * CosSim = svp/(normVector1*normVector2) = 0.5 / (1.58*1.58) = 0.2 <br />
     * 
     * @throws Exception
     */
    @Test
    public void testCosSimDefaultTfIdf() throws Exception
    {
        CosineSimilarityTest test = new CosineSimilarityTest();
        test.initialize();
        test.parameters = new Object[] { CosineFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                CosineFeatureExtractor.PARAM_SOURCE_LOCATION, test.lucenePath.toString(),
                IdfPairMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath.toString(), };
        test.runPipeline();
        assertTrue(test.featureNames.first().equals("SimilarityCosineSimilarity"));
        assertEquals(test.featureNames.size(), 1);

        for (Feature feat : test.instanceList.get(0).getFeatures()) {
            assertEquals(0.2, (double) feat.getValue(), epsilon);
            // System.out.println("CosSim score: " + (double)feat.getValue());
        }
    }

    @Test
    public void testCosSimWithStems() throws Exception
    {
        CosineSimilarityTest test = new CosineSimilarityTest();
        test.initialize();
        test.parameters = new Object[] { CosineFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                CosineFeatureExtractor.PARAM_SOURCE_LOCATION, test.lucenePath.toString(),
                IdfPairMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath.toString(),
                CosineFeatureExtractor.PARAM_NGRAM_ANNO_TYPE,
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem" };
        test.runPipeline();
        assertTrue(test.featureNames.first().equals("SimilarityCosineSimilarity"));
        assertEquals(test.featureNames.size(), 1);

        for (Feature feat : test.instanceList.get(0).getFeatures()) {
            assertEquals(0.2, (double) feat.getValue(), epsilon);
        }
    }

    @Test
    public void testCosSimWithLemmas() throws Exception
    {
        CosineSimilarityTest test = new CosineSimilarityTest();
        test.initialize();
        test.parameters = new Object[] { CosineFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                CosineFeatureExtractor.PARAM_SOURCE_LOCATION, test.lucenePath.toString(),
                IdfPairMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath.toString(),
                CosineFeatureExtractor.PARAM_NGRAM_ANNO_TYPE,
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" };
        test.runPipeline();
        assertTrue(test.featureNames.first().equals("SimilarityCosineSimilarity"));
        assertEquals(test.featureNames.size(), 1);

        for (Feature feat : test.instanceList.get(0).getFeatures()) {
            assertEquals(0.2, (double) feat.getValue(), epsilon);
        }
    }

    @Test
    public void testCosSimWithPosTags() throws Exception
    {
        CosineSimilarityTest test = new CosineSimilarityTest();
        test.initialize();
        test.parameters = new Object[] { CosineFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                CosineFeatureExtractor.PARAM_SOURCE_LOCATION, test.lucenePath.toString(),
                IdfPairMetaCollector.PARAM_TARGET_LOCATION, test.lucenePath.toString(),
                CosineFeatureExtractor.PARAM_NGRAM_ANNO_TYPE,
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" };
        test.runPipeline();
        assertTrue(test.featureNames.first().equals("SimilarityCosineSimilarity"));
        assertEquals(test.featureNames.size(), 1);

        for (Feature feat : test.instanceList.get(0).getFeatures()) {
            assertEquals(0.2, (double) feat.getValue(), epsilon);
        }
    }

    @Override
    protected void getFeatureExtractorCollector(List<Object> parameterList)
        throws ResourceInitializationException
    {
        ExternalResourceDescription featureExtractor = ExternalResourceFactory
                .createExternalResourceDescription(CosineFeatureExtractor.class, parameters);
        List<ExternalResourceDescription> fes = new ArrayList<>();
        fes.add(featureExtractor);

        featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_REGRESSION, Constants.FM_PAIR, false, false, false, false,
                Collections.emptyList(), fes, new String[0]);

    }

    @Override
    protected void getMetaCollector(List<Object> parameterList)
        throws ResourceInitializationException
    {
        metaCollector = AnalysisEngineFactory.createEngineDescription(IdfPairMetaCollector.class,
                parameterList.toArray());
    }
}