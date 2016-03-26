/*******************************************************************************
 * Copyright 2015
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

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ART;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.CONJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NP;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;

import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationUnit;
import org.dkpro.tc.features.wordDifficulty.PosTypeUFE;


public class PosTypeUFETest {
		@Test
		public void posTypeUFETest() 
		        throws Exception {
		            
	        AnalysisEngine engine = createEngine(NoOpAnnotator.class);

	        JCas jcas = engine.newJCas();
	        jcas.setDocumentLanguage("en");
	        jcas.setDocumentText("01234567890123456789");
	        engine.process(jcas);
	        
	        
	        TextClassificationUnit unit1 = new TextClassificationUnit(jcas, 0, 1);
	        new ADJ(jcas, 0, 1).addToIndexes();
	      
	        TextClassificationUnit unit2 = new TextClassificationUnit(jcas, 2, 3);
	        new ADV(jcas, 2, 3).addToIndexes();
	        
	        TextClassificationUnit unit3 = new TextClassificationUnit(jcas, 4, 5);
	        new ART(jcas, 4, 5).addToIndexes();
	        
	        TextClassificationUnit unit4 = new TextClassificationUnit(jcas, 6, 7);
	        new CONJ(jcas, 6, 7).addToIndexes();
	        
	        TextClassificationUnit unit5 = new TextClassificationUnit(jcas, 8, 9);
	        new NN(jcas, 8, 9).addToIndexes();
	        
	        TextClassificationUnit unit6 = new TextClassificationUnit(jcas, 10, 11);
	        new NP(jcas, 10, 11).addToIndexes();

	        TextClassificationUnit unit7 = new TextClassificationUnit(jcas, 12, 13);
	        new PP(jcas, 12, 13).addToIndexes();
	        
	        TextClassificationUnit unit8 = new TextClassificationUnit(jcas, 14, 15);
	        new PR(jcas, 14, 15).addToIndexes();
	        
	        TextClassificationUnit unit9 = new TextClassificationUnit(jcas, 16, 17);
	        new V(jcas, 16, 17).addToIndexes();
	    

	        PosTypeUFE extractor = new PosTypeUFE();
	        
	        testFeatures(extractor.extract(jcas, unit1), 9, true,  false, false, false, false, false, false, false, false);// adj
	        testFeatures(extractor.extract(jcas, unit2), 9, false,  true, false, false, false, false, false, false, false);//adv
	        testFeatures(extractor.extract(jcas, unit3), 9, false,  false, true, false, false, false, false, false, false);//art
	        testFeatures(extractor.extract(jcas, unit4), 9, false,  false, false, true, false, false, false, false, false);//conj
	        testFeatures(extractor.extract(jcas, unit5), 9, false,  false, false, false, true, false, false, false, false);//noun
	        testFeatures(extractor.extract(jcas, unit6), 9, false,  false, false, false, false, true, false, false, false);//np
	        testFeatures(extractor.extract(jcas, unit7), 9, false,  false, false, false, false, false, true, false, false);//pp
	        testFeatures(extractor.extract(jcas, unit8), 9, false,  false, false, false, false, false, false, true, false);//pr
	        testFeatures(extractor.extract(jcas, unit9), 9, false,  false, false, false, false, false, false, false, true);//v
	       

		}
		
		private void testFeatures(Set<Feature> features, int nrOfFeatures, boolean ... expectedValues) {
			assertEquals(nrOfFeatures, features.size());
			for (Feature f : features) {
			
	        	if (f.getName().equals("IsADJ")) {
	        		assertEquals(expectedValues[0], f.getValue());
	        	}
	        	if(f.getName().equals("IsADV")) {
	        		assertEquals(expectedValues[1], f.getValue());
	        	}
	        	if(f.getName().equals("IsART")){
	        		assertEquals(expectedValues[2], f.getValue());
	        	}
	        	if(f.getName().equals("IsCONJ")){
	        		assertEquals(expectedValues[3], f.getValue());
	        	}
	        	if(f.getName().equals("IsNN")){
	        		assertEquals(expectedValues[4], f.getValue());	
	        	}
	        	if(f.getName().equals("IsNP")){
	        		assertEquals(expectedValues[5], f.getValue());
	        	}
	        	if(f.getName().equals("IsPP")){
	        		assertEquals(expectedValues[6], f.getValue());
	        	}
	        	if(f.getName().equals("IsPR")){
	        		assertEquals(expectedValues[7], f.getValue());	
	        	}
	        	if(f.getName().equals("IsV")){
	        		assertEquals(expectedValues[8], f.getValue());
	        		
	        	}
			}
		}
	}


