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
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;

import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationUnit;
import org.dkpro.tc.features.wordDifficulty.IsInflectedWordUFE;


public class IsInflectedWordUFETest {
	
	@Test
	public void isInflectedWordUFETest() 
	        throws Exception
	{
	            
        AnalysisEngine engine = createEngine(NoOpAnnotator.class);
       
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");	
        jcas.setDocumentText("the happiest animals were there.");
        engine.process(jcas);
        
        TextClassificationUnit unit1 = new TextClassificationUnit(jcas, 0, 3);
        new ART(jcas, 0, 3).addToIndexes();
        Lemma l1 =new Lemma(jcas, 0, 3);
        addAnnotations(l1, jcas, "the", 0, 3);
        
        TextClassificationUnit unit2 = new TextClassificationUnit(jcas, 4, 12);
        new ADJ(jcas, 4, 12).addToIndexes();
        Lemma l2 = new Lemma(jcas, 4, 12);
        addAnnotations(l2, jcas, "happy", 4, 12);
        
        TextClassificationUnit unit3 = new TextClassificationUnit(jcas, 13, 20);
        new NN(jcas, 13, 20).addToIndexes();
        Lemma l3= new Lemma(jcas, 13, 20);
        addAnnotations(l3, jcas, "animal", 13, 20);
        
        TextClassificationUnit unit4 = new TextClassificationUnit(jcas, 21, 25);
        new V(jcas, 21, 25).addToIndexes();
        Lemma l4=new Lemma(jcas, 21, 25);
        addAnnotations(l4, jcas, "be", 21, 25);
        
        TextClassificationUnit unit5 = new TextClassificationUnit(jcas, 26, 31);
        new ADV(jcas, 26, 31).addToIndexes();
        Lemma l5= new Lemma(jcas, 26, 31);
        addAnnotations(l5, jcas, "there", 26, 31);
        

        IsInflectedWordUFE extractor = new IsInflectedWordUFE();
        
        //                                              lemma  noun  verb   adj   derivedAdj
        testFeatures(extractor.extract(jcas, unit1), 4, true,  false, false, false, false);// the
        testFeatures(extractor.extract(jcas, unit2), 4, false, false, false, true, false);//happiest
        testFeatures(extractor.extract(jcas, unit3), 4, false, true, false, false, false);//animals
        testFeatures(extractor.extract(jcas, unit4), 4, false, false, true, false, false);//were
        testFeatures(extractor.extract(jcas, unit5), 4, true, false, false, false, false);//there
        
    
	}
	
	private void testFeatures(Set<Feature> features, int nrOfFeatures, boolean ... expectedValues) {
		assertEquals(nrOfFeatures, features.size());
		for (Feature f : features) {
		
        	if (f.getName().equals(IsInflectedWordUFE.IS_LEMMA)) {
        		assertEquals(expectedValues[0], f.getValue());
        	}
        	if(f.getName().equals(IsInflectedWordUFE.INFLECTED_NOUN)) {
        		assertEquals(expectedValues[1], f.getValue());
        	}
        	if(f.getName().equals(IsInflectedWordUFE.INFLECTED_VERB)){
        		assertEquals(expectedValues[2], f.getValue());
        	}
        	if(f.getName().equals(IsInflectedWordUFE.INFLECTED_ADJ)){
        		assertEquals(expectedValues[3], f.getValue());
        	}
        	if(f.getName().equals(IsInflectedWordUFE.DERIVED_ADJ)){
        		assertEquals(expectedValues[4], f.getValue());
        	}
		}
	}
	
	
	private void addAnnotations(Lemma lemmaName, JCas jcas,String lemma, int start, int end) {
		lemmaName = new Lemma(jcas, start, end);
        lemmaName.setValue(lemma);
        lemmaName.addToIndexes();	
		
	}
}