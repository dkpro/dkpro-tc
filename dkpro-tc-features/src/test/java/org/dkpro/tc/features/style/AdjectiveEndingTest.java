/*******************************************************************************
 * Copyright 2017
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
package org.dkpro.tc.features.style;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeatures;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.style.AdjectiveEndingFeatureExtractor;
import org.junit.Assert;
public class AdjectiveEndingTest
{
    @Test
    public void adjectiveEndingFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngineDescription desc = createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE,
                        "en"));
        AnalysisEngine engine = createEngine(desc);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("Lovable phenomenal beautiful incredible fantastic gorgeous positive nice good mainly harmless.");
        engine.process(jcas);
        
        TextClassificationTarget target = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
        target.addToIndexes();

        AdjectiveEndingFeatureExtractor extractor = new AdjectiveEndingFeatureExtractor();
        Set<Feature> features = extractor.extract(jcas, target);

        Assert.assertEquals(9, features.size());

        assertFeatures(AdjectiveEndingFeatureExtractor.ADJ_ENDING1, 10.0, features, 0.001);
        assertFeatures(AdjectiveEndingFeatureExtractor.ADJ_ENDING2, 10.0, features, 0.001);
        assertFeatures(AdjectiveEndingFeatureExtractor.ADJ_ENDING3, 10.0, features, 0.001);
        assertFeatures(AdjectiveEndingFeatureExtractor.ADJ_ENDING4, 10.0, features, 0.001);
        assertFeatures(AdjectiveEndingFeatureExtractor.ADJ_ENDING5, 10.0, features, 0.001);
        assertFeatures(AdjectiveEndingFeatureExtractor.ADJ_ENDING6, 10.0, features, 0.001);
        assertFeatures(AdjectiveEndingFeatureExtractor.ADJ_ENDING7, 10.0, features, 0.001);
        assertFeatures(AdjectiveEndingFeatureExtractor.ADJ_ENDING8, 10.0, features, 0.001);
        assertFeatures(AdjectiveEndingFeatureExtractor.ADV_ENDING9, 100.0, features, 0.001);
   }
}