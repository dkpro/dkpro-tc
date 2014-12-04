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
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class TraditionalReadabilityMeasuresExtractorTest
{

    @Test
    public void readabilityFeatureExtractorTest()
        throws Exception
    {
        HashMap<String, Double> correctResult = new HashMap<String, Double>();
        correctResult.put("kincaid", 7.6);
        correctResult.put("ari", 9.1);
        correctResult.put("coleman_liau", 11.6);
        correctResult.put("flesch", 70.6);
        correctResult.put("lix", 5.0);
        correctResult.put("smog", 9.9);
        correctResult.put("fog", 10.6);
        double EPSILON = 0.1;
        String text = FileUtils
                .readFileToString(new File("src/test/resources/test_document_en.txt"));

        AnalysisEngineDescription desc = createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class));
        AnalysisEngine engine = createEngine(desc);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText(text);
        engine.process(jcas);

        TraditionalReadabilityMeasuresFeatureExtractor extractor = new TraditionalReadabilityMeasuresFeatureExtractor();
        List<Feature> features = extractor.extract(jcas);

        assertEquals(7, features.size());

        for (Feature feature : features) {

            assertEquals(correctResult.get(feature.getName()), (double) feature.getValue(), EPSILON);

        }
    }
}