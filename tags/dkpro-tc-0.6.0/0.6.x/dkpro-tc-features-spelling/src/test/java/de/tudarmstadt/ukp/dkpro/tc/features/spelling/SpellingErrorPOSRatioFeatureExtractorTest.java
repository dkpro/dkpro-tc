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
package de.tudarmstadt.ukp.dkpro.tc.features.spelling;

import static de.tudarmstadt.ukp.dkpro.tc.core.util.FeatureTestUtil.assertFeature;
import static de.tudarmstadt.ukp.dkpro.tc.features.spelling.SpellingErrorPOSRatioFeatureExtractor.FN_ART_ERROR_RATIO;
import static de.tudarmstadt.ukp.dkpro.tc.features.spelling.SpellingErrorPOSRatioFeatureExtractor.FN_N_ERROR_RATIO;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.jazzy.SpellChecker;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class SpellingErrorPOSRatioFeatureExtractorTest
{
    @Test
    public void posContextFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngineDescription desc = createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE,
                        "en"),
                createEngineDescription(SpellChecker.class, SpellChecker.PARAM_MODEL_LOCATION,
                        "src/test/resources/dictionary/en_US_dict.txt"));
        AnalysisEngine engine = createEngine(desc);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("As tthe pope leavess the Vatican for the papal residenze of Castel Gandolfo – and becomes the first pontiff to resign in 600 years – the operation to choose his successor begins.");
        engine.process(jcas);

        SpellingErrorPOSRatioFeatureExtractor extractor = new SpellingErrorPOSRatioFeatureExtractor();
        List<Feature> features = extractor.extract(jcas);

        Assert.assertEquals(11, features.size());

        // for (SpellingAnomaly anomaly : JCasUtil.select(jcas, SpellingAnomaly.class)) {
        // System.out.println(anomaly);
        // for (POS pos : JCasUtil.selectCovered(jcas, POS.class, anomaly)) {
        // System.out.println(pos);
        // }
        // }

        for (Feature feature : features) {
            if (feature.getName().equals(FN_ART_ERROR_RATIO)) {
                assertFeature(FN_ART_ERROR_RATIO, 0.1111, feature, 0.0001);
            }
            else if (feature.getName().equals(FN_N_ERROR_RATIO)) {
                assertFeature(FN_N_ERROR_RATIO, 0.3333, feature, 0.0001);
            }
        }
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}