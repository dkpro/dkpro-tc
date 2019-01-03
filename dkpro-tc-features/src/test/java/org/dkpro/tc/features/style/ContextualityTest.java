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
package org.dkpro.tc.features.style;

import static org.dkpro.tc.features.style.ContextualityMeasureFeatureExtractor.CONTEXTUALITY_MEASURE_FN;
import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.style.ContextualityMeasureFeatureExtractor;

/*
 * Heylighen & Dewaele (2002): Variation in the contextuality of language
 * The contextuality measure can reach values 0-100
 * The higher value, the more formal (male) style the text is,
 * i.e. contains many nouns, verbs, determiners.
 * The lower value, the more contextual (female) style the text is,
 * i.e. contains many adverbs, pronouns and such.
 */

public class ContextualityTest
{
    @Test
    public void posContextFeatureExtractorTest() throws Exception
    {
        AnalysisEngineDescription desc = createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class), createEngineDescription(
                        OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE, "en"));
        AnalysisEngine engine = createEngine(desc);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("This is a test.");
        engine.process(jcas);

        TextClassificationTarget aTarget = new TextClassificationTarget(jcas, 0,
                jcas.getDocumentText().length());
        aTarget.addToIndexes();

        ContextualityMeasureFeatureExtractor extractor = new ContextualityMeasureFeatureExtractor();
        List<Feature> features = new ArrayList<Feature>(extractor.extract(jcas, aTarget));

        Assert.assertEquals(8, features.size());

        for (Feature feature : features) {
            if (feature.getName().equals(CONTEXTUALITY_MEASURE_FN)) {
                assertFeature(CONTEXTUALITY_MEASURE_FN, 50.2, feature);
            }
        }
    }
}