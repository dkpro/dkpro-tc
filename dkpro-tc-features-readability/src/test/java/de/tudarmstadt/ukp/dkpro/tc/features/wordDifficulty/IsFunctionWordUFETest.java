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
package de.tudarmstadt.ukp.dkpro.tc.features.wordDifficulty;


import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.*;

import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ART;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.CONJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;


public class IsFunctionWordUFETest {
	@Test
	public void isFunctionWordUFETest() 
	        throws Exception {
	            
        AnalysisEngine engine = createEngine(NoOpAnnotator.class);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("12345678901");
        engine.process(jcas);
        
        
        TextClassificationUnit unit1 = new TextClassificationUnit(jcas, 0, 2);
        new PR(jcas, 0, 2).addToIndexes();
      
        TextClassificationUnit unit2 = new TextClassificationUnit(jcas, 3, 4);
        new NN(jcas, 3, 4).addToIndexes();
        
        TextClassificationUnit unit3 = new TextClassificationUnit(jcas, 5, 6);
        new PP(jcas, 5, 6).addToIndexes();
        
        TextClassificationUnit unit4 = new TextClassificationUnit(jcas, 7, 8);
        new CONJ(jcas, 7, 8).addToIndexes();
        
        TextClassificationUnit unit5 = new TextClassificationUnit(jcas, 9, 10);
        new ART(jcas, 9, 10).addToIndexes();
        
        
        

        IsFunctionWordUFE extractor = new IsFunctionWordUFE();
        

        Set<Feature> features1 = extractor.extract(jcas, unit1);
        Set<Feature> features2 = extractor.extract(jcas, unit2);
        Set<Feature> features3 = extractor.extract(jcas, unit3);
        Set<Feature> features4 = extractor.extract(jcas, unit4);
        Set<Feature> features5 = extractor.extract(jcas, unit5);
        
        assertEquals(1, features1.size());
        assertEquals(1, features2.size());
        assertEquals(1, features3.size());
        assertEquals(1, features4.size());
        assertEquals(1, features5.size());
        
        
        Feature f1 = features1.iterator().next();
        Feature f2 = features2.iterator().next();
        Feature f3 = features3.iterator().next();
        Feature f4 = features4.iterator().next();
        Feature f5 = features5.iterator().next();
        
        assertTrue((Boolean)f1.getValue());
        assertFalse((Boolean)f2.getValue());
        assertTrue((Boolean)f3.getValue());
        assertTrue((Boolean)f4.getValue());
        assertTrue((Boolean)f5.getValue());
	}
}