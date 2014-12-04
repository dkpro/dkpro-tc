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

package de.tudarmstadt.ukp.dkpro.tc.features.readability;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class ParsePatternExtractorTest
{
    @Test
    public void testPhrasePatternExtractor()
        throws Exception
    {
        String text = "We use it when a girl in our dorm is acting like a spoiled and nervous child.";
        AnalysisEngineDescription desc = createEngineDescription(
                createEngineDescription(OpenNlpSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class),
                createEngineDescription(BerkeleyParser.class));
        AnalysisEngine engine = createEngine(desc);
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText(text);
        engine.process(jcas);

        ParsePatternExtractor extractor = new ParsePatternExtractor();
        List<Feature> features = extractor.extract(jcas);
        System.out.println(features);
        double[] results = { 6.0, 3.0, 2.0, 1.0, 2.0, 1.0, 1.0, 1.0, 1.0, 10.5, 51.3, 21.5, 77.0,
                69.0, 10.0, 2.0, 1.0, 1.0, 2.0, 2.0, 1.0, 0.5, 0.5, 1.0, 6.0, 3.0, 2.0, 1.0, 2.0,
                1.0, 1.0, 1.0, 1.0, 10.5, 51.3, 21.5, 77.0, 69.0, 10.0, 2.0, 1.0, 1.0, 2.0, 2.0,
                1.0, 0.5, 0.5, 1.0, 6.0, 3.0, 2.0, 1.0, 2.0, 1.0, 1.0, 1.0, 1.0, 10.5, 51.3, 21.5,
                77.0, 69.0, 10.0, 2.0, 1.0, 1.0, 2.0, 2.0, 1.0, 0.5, 0.5, 1.0 };
        for (int i = 0; i < features.size(); i++) {
            Assert.assertEquals(results[i], (double) features.get(i).getValue(), 0.1);
        }
    }

}