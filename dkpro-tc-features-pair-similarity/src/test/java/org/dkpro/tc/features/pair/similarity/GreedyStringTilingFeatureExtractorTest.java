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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.*;

import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.features.pair.similarity.GreedyStringTilingFeatureExtractor;


public class GreedyStringTilingFeatureExtractorTest {
	@Test
	public void greedyStringTilingFeatureExtractorTest() 
	        throws Exception {
	            
        AnalysisEngine engine = createEngine(NoOpAnnotator.class);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("1234567890");
        engine.process(jcas);
        
        JCas jcas2 = engine.newJCas();
        jcas2.setDocumentLanguage("en");
        jcas2.setDocumentText("12345");
        engine.process(jcas2);
       

        GreedyStringTilingFeatureExtractor extractor = new GreedyStringTilingFeatureExtractor();
        
        Set<Feature> features1 = extractor.extract(jcas, jcas2);
        assertEquals(1, features1.size());
        Feature f1 = features1.iterator().next();
        assertEquals(f1.getValue(), 0.5);
	}
}