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
package org.dkpro.tc.features.pair.core.chunk;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.features.pair.core.chunk.DiffNounChunkCharacterLength;
import org.dkpro.tc.testing.FeatureTestUtil;

public class DiffNounChunkCharacterLengthTest
{
    private JCas jcas1;
    private JCas jcas2;

    @Before
    public void setUp() throws ResourceInitializationException, AnalysisEngineProcessException
    {
        AnalysisEngineDescription desc = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngine engine = createEngine(desc);

        jcas1 = engine.newJCas();
        jcas1.setDocumentLanguage("en");
        jcas1.setDocumentText("This is the text of view 1");
        engine.process(jcas1);

        jcas2 = engine.newJCas();
        jcas2.setDocumentLanguage("en");
        jcas2.setDocumentText("This is the text of view 2");
        engine.process(jcas2);

    }

    @Test
    public void extractTest1() throws Exception
    {
        Chunk chunk1 = new Chunk(jcas1, 0, 4);
        chunk1.addToIndexes();

        Chunk chunk2 = new Chunk(jcas2, 0, 4);
        chunk2.addToIndexes();

        DiffNounChunkCharacterLength extractor = new DiffNounChunkCharacterLength();
        Set<Feature> features = extractor.extract(jcas1, jcas2);

        assertEquals(1, features.size());

        for (Feature feature : features) {
            FeatureTestUtil.assertFeature("DiffNounPhraseCharacterLength", 0.0, feature, 0.0001);
        }
    }

    @Test
    public void extractTest2() throws Exception
    {
        Chunk chunk1 = new Chunk(jcas1, 0, 4);
        chunk1.addToIndexes();

        Chunk chunk2 = new Chunk(jcas2, 0, 7);
        chunk2.addToIndexes();

        DiffNounChunkCharacterLength extractor = new DiffNounChunkCharacterLength();
        Set<Feature> features = extractor.extract(jcas1, jcas2);

        assertEquals(1, features.size());

        for (Feature feature : features) {
            assertFeature("DiffNounPhraseCharacterLength", -3.0, feature, 0.0001);
        }
    }

}
