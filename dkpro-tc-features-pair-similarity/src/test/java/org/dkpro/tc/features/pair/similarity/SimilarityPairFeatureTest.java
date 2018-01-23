/*******************************************************************************
 * Copyright 2018
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

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Iterator;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.features.pair.similarity.SimilarityPairFeatureExtractor;

import dkpro.similarity.algorithms.lexical.uima.string.GreedyStringTilingMeasureResource;

public class SimilarityPairFeatureTest
{
    private static final String VIEW1 = "view1";
    private static final String VIEW2 = "view2";

    @Test
    public void similarityPairFeatureTest()
        throws Exception
    {
        ExternalResourceDescription gstResource = ExternalResourceFactory
                .createExternalResourceDescription(GreedyStringTilingMeasureResource.class,
                        GreedyStringTilingMeasureResource.PARAM_MIN_MATCH_LENGTH, "3");

        AnalysisEngineDescription desc = createEngineDescription(NoOpAnnotator.class);
        AnalysisEngine engine = createEngine(desc);
        JCas jcas = engine.newJCas();
        TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class,
                Sentence.class);

        JCas view1 = jcas.createView(VIEW1);
        view1.setDocumentLanguage("en");
        tb.buildTokens(view1, "This is a test .");

        JCas view2 = jcas.createView(VIEW2);
        view2.setDocumentLanguage("en");
        tb.buildTokens(view2, "Test is this .");

        engine.process(jcas);
        
        SimilarityPairFeatureExtractor extractor = FeatureUtil.createResource(
        		SimilarityPairFeatureExtractor.class,
        		SimilarityPairFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
        		SimilarityPairFeatureExtractor.PARAM_SEGMENT_FEATURE_PATH, Token.class.getName(),
                SimilarityPairFeatureExtractor.PARAM_TEXT_SIMILARITY_RESOURCE, gstResource);

        Set<Feature> features = extractor.extract(jcas.getView(VIEW1), jcas.getView(VIEW2));

        Assert.assertEquals(1, features.size());
        Iterator<Feature> iter = features.iterator();
        assertFeature("SimilarityGreedyStringTiling_3", 0.8125, iter.next(), 0.0001);
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}