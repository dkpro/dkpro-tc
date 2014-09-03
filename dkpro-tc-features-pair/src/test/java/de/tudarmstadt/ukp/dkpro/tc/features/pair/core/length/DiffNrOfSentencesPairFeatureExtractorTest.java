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
package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.length;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;
import static de.tudarmstadt.ukp.dkpro.tc.testing.FeatureTestUtil.*;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.PairFeatureTestBase;

public class DiffNrOfSentencesPairFeatureExtractorTest
    extends PairFeatureTestBase
{

    @Test
    public void testExtract()
        throws ResourceInitializationException, AnalysisEngineProcessException,
        TextClassificationException
    {
        AnalysisEngineDescription desc = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngine engine = createEngine(desc);

        JCas jcas1 = engine.newJCas();
        jcas1.setDocumentLanguage("en");
        jcas1.setDocumentText("This is the text of view 1. And some more.");
        engine.process(jcas1);

        JCas jcas2 = engine.newJCas();
        jcas2.setDocumentLanguage("en");
        jcas2.setDocumentText("This is the text of view 2.");
        engine.process(jcas2);

        DiffNrOfSentencesPairFeatureExtractor extractor = new DiffNrOfSentencesPairFeatureExtractor();
        List<Feature> features = extractor.extract(jcas1, jcas2);

        assertEquals(1, features.size());

        for (Feature feature : features) {
            assertFeature("DiffNrOfSentences", 1, feature);
        }

    }

}