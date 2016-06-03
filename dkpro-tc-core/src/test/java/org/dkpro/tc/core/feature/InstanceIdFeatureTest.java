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
package org.dkpro.tc.core.feature;


import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationUnit;
import org.junit.Test;



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

	        JCasId id = new JCasId(jcas);
	        id.setId(123);
	        id.addToIndexes();
	        
	        Feature feature = InstanceIdFeature.retrieve(jcas, unit1);
	        Feature feature2 = InstanceIdFeature.retrieve(jcas);
	        Feature feature3= InstanceIdFeature.retrieve(jcas, unit1, 5);
	        assertEquals(feature.getValue(),"123_0");
	        assertEquals(feature2.getValue(), "123");
	        assertEquals(feature3.getValue(), "123_5_0");   
			}
		}        
	        
	       
	   
		
		