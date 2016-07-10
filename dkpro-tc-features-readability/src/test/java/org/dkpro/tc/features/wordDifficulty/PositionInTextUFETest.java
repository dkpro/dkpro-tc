/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.features.wordDifficulty;


import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.*;

import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.wordDifficulty.PositionInTextUFE;


public class PositionInTextUFETest {
		@Test
		public void positionInTextUFETest() 
		        throws Exception {
		            
	        AnalysisEngine engine = createEngine(NoOpAnnotator.class);

	        JCas jcas = engine.newJCas();
	        jcas.setDocumentLanguage("en");
	        jcas.setDocumentText("a b c d");
	        engine.process(jcas);
	        
	        
	        TextClassificationTarget unit1 = new TextClassificationTarget(jcas, 0, 1);
	        Token t1 = new Token(jcas, 0,1);
	        t1.addToIndexes();	
	        unit1.addToIndexes();
	        
	        TextClassificationTarget unit2 = new TextClassificationTarget(jcas, 2, 3);
	        Token t2 = new Token(jcas, 2,3);
	        t2.addToIndexes();	
	        unit2.addToIndexes();
	        
	        TextClassificationTarget unit3 = new TextClassificationTarget(jcas, 4, 5);
	        Token t3 = new Token(jcas, 4,5);
	        t3.addToIndexes();	
	        unit3.addToIndexes();
	        
	        TextClassificationTarget unit4 = new TextClassificationTarget(jcas, 6, 7);
	        Token t4 = new Token(jcas, 6,7);
	        t4.addToIndexes();	
	        unit4.addToIndexes();


	        PositionInTextUFE extractor = new PositionInTextUFE();
	        Set<Feature> features = extractor.extract(jcas, unit1);
	        Set<Feature> features2 = extractor.extract(jcas, unit2);
	        Set<Feature> features3 = extractor.extract(jcas, unit3);
	        Set<Feature> features4 = extractor.extract(jcas, unit4);
	        
	        Feature f1 = features.iterator().next();
	        assertEquals(f1.getValue(),1);
	        features.remove(f1);
	        Feature f2 = features.iterator().next();
	        assertEquals(f2.getValue(), 0.25);
	        
	        Feature f3 = features2.iterator().next();
	        assertEquals(f3.getValue(), 2);
	        features2.remove(f3);
	        Feature f4 = features2.iterator().next();
	        assertEquals(f4.getValue(), 0.5);
	        
	        Feature f5 = features3.iterator().next();
	        assertEquals(f5.getValue(),3);
	        features3.remove(f5);
	        Feature f6 = features3.iterator().next();
	        assertEquals(f6.getValue(), 0.75);
	        
	        Feature f7 = features4.iterator().next();
	        assertEquals(f7.getValue(),4);
	        features4.remove(f7);
	        Feature f8 = features4.iterator().next();
	        assertEquals(f8.getValue(), 1.0);

	        
	       

		}
	}


