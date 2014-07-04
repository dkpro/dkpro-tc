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
package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ne;

import static de.tudarmstadt.ukp.dkpro.tc.core.util.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class SharedNEsFeatureExtractorTest {
	
	JCas jcas1;
	JCas jcas2;

	@Before
	public void setUp() throws ResourceInitializationException, AnalysisEngineProcessException {
        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);
        
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
    public void extractTest1()
        throws Exception
    {
		NamedEntity ne1 = new NamedEntity(jcas1, 0, 4);
		ne1.addToIndexes();
		
        SharedNEsFeatureExtractor extractor = new SharedNEsFeatureExtractor();
        List<Feature> features = extractor.extract(jcas1, jcas2);

        assertEquals(1, features.size());
        
        for (Feature feature : features) {
            assertFeature("SharedNEs", false, feature);
        }
        
		NamedEntity ne2 = new NamedEntity(jcas2, 0, 4);
		ne2.addToIndexes();
		
        features = extractor.extract(jcas1, jcas2);

        assertEquals(1, features.size());
        
        for (Feature feature : features) {
            assertFeature("SharedNEs", true, feature);
        }
    }

}
