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
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.CONJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.wordDifficulty.IsNounCompoundExtractor;

public class IsNounCompoundExtractorTest {
	@Test
	public void posTypeUFETest() 
	        throws Exception {
	            
        AnalysisEngine engine = createEngine(NoOpAnnotator.class);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("Autohändler und Steine");
        engine.process(jcas);
        
        TextClassificationTarget unit1 = new TextClassificationTarget(jcas, 0, 11);
        assertEquals("Autohändler", unit1.getCoveredText());
        new NN(jcas, 0, 11).addToIndexes();
        Lemma l1= new Lemma(jcas, 0, 11);
        Token t1 = new Token(jcas, 0,11);
        POS pos1 = JCasUtil.selectCovered(jcas, POS.class, l1).get(0);
        addAnnotations(l1,t1, pos1, jcas, "Autohändler", 0, 11);
        
      
        TextClassificationTarget unit2 = new TextClassificationTarget(jcas, 12, 15);
        assertEquals("und", unit2.getCoveredText());
        new CONJ(jcas, 12, 14).addToIndexes();
        Lemma l2= new Lemma(jcas, 12, 14);
        Token t2 = new Token(jcas, 12,14);
        POS pos2 = JCasUtil.selectCovered(jcas, POS.class, l2).get(0);
        addAnnotations(l2,t2, pos2, jcas, "und", 12, 14);
        
      
        
        TextClassificationTarget unit3 = new TextClassificationTarget(jcas, 16, 22);
        assertEquals("Steine", unit3.getCoveredText());
        new NN(jcas, 16, 22).addToIndexes();
        Lemma l3 = new Lemma(jcas, 16, 22);
        Token t3 = new Token(jcas, 16,22);
        POS pos3 = JCasUtil.selectCovered(jcas, POS.class, l3).get(0);
        addAnnotations(l3, t3, pos3, jcas, "Steine", 16, 22);
       
        
        IsNounCompoundExtractor extractor = FeatureUtil.createResource(
        		IsNounCompoundExtractor.class,
        		IsNounCompoundExtractor.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
        		IsNounCompoundExtractor.DICTIONARY_LOCATON, "src/test/resources/dictionary/dict_de_nouns"
        );

        Set<Feature> features1 = extractor.extract(jcas, unit1);
        Set<Feature> features2 = extractor.extract(jcas, unit2);
        Set<Feature> features3 = extractor.extract(jcas, unit3);
        
        assertEquals(1, features1.size());
        assertEquals(1, features2.size());
        assertEquals(1, features3.size());
       
        Feature f1 = features1.iterator().next();
        Feature f2 = features2.iterator().next();
        Feature f3 = features3.iterator().next();
        
        assertTrue((Boolean)f1.getValue());
        assertFalse((Boolean)f2.getValue());
        assertFalse((Boolean)f3.getValue());
	}
        
     
	private void addAnnotations(Lemma lemmaName, Token tokenName, POS posName, JCas jcas, String lemma, int start, int end) {
		lemmaName = new Lemma(jcas, start, end);
        lemmaName.setValue(lemma);
        lemmaName.addToIndexes();
        tokenName.setPos(posName);
        tokenName.addToIndexes();
		
	}
	

}
