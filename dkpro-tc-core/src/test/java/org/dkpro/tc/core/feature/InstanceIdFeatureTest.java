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
package org.dkpro.tc.core.feature;


import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.*;

import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
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
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationUnit;
import org.dkpro.tc.core.feature.InstanceIdFeature;



public class InstanceIdFeatureTest {
		@Test
		public void instanceIdFeatureTest() 
		        throws Exception {
		            
			AnalysisEngine engine = createEngine(NoOpAnnotator.class);

	        JCas jcas = engine.newJCas();
	        jcas.setDocumentLanguage("en");
	        engine.process(jcas);
	        
	        TextClassificationUnit unit1 = new TextClassificationUnit(jcas, 0, 1);
	        unit1.setId(0);
	        unit1.addToIndexes();
	        
	        DocumentMetaData dmd = DocumentMetaData.create(jcas);
	        dmd.setDocumentId("document_123");
	        
	        InstanceIdFeature retriever = new InstanceIdFeature();
	        Feature feature = retriever.retrieve(jcas, unit1);
	        Feature feature2 = retriever.retrieve(jcas);
	        Feature feature3= retriever.retrieve(jcas, unit1, 5);
	        assertEquals(feature.getValue(),"document_0");
	        assertEquals(feature2.getValue(), "document_123");
	        assertEquals(feature3.getValue(), "document_5_0");   
			}
		}        
	        
	       
	   
		
		