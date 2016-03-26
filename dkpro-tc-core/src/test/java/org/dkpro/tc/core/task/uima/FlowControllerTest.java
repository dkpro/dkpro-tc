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
package org.dkpro.tc.core.task.uima;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.FlowControllerFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationUnit;
import org.dkpro.tc.core.io.ClassificationUnitCasMultiplier;
import org.dkpro.tc.core.task.uima.CasDropFlowController;

public class FlowControllerTest {

	@Test
	public void flowControllerTest_valid() throws Exception {
		
		JCas jcas = run("test", true);
		
		for (Lemma lemma : JCasUtil.selectCovered(jcas, Lemma.class, new Token(jcas, 0, 0))) {
			assertTrue(lemma.getValue().equals("dummyTest"));
		}
	}
	
	@Test
	public void flowControllerTest_noDocText() throws Exception {
		
		JCas jcas = run("", true);
		
		for (Lemma lemma : JCasUtil.selectCovered(jcas, Lemma.class, new Token(jcas, 0, 0))) {
			assertFalse(lemma.getValue().equals("dummyTest"));
		}
	}
	
	private JCas run(String text, boolean setOutcome) 
		throws Exception
	{
		AnalysisEngineDescription desc = AnalysisEngineFactory.createEngineDescription(
		         FlowControllerFactory.createFlowControllerDescription(CasDropFlowController.class),
		         AnalysisEngineFactory.createEngineDescription(ClassificationUnitCasMultiplier.class),
                 AnalysisEngineFactory.createEngineDescription(AfterAE.class)
		);

		AnalysisEngine engine = AnalysisEngineFactory.createEngine(desc);
		JCas jcas = engine.newJCas();
		jcas.setDocumentText(text);

		if (setOutcome) {
			TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, 0, 0);
			outcome.addToIndexes();			
		}
		TextClassificationUnit tcu = new TextClassificationUnit(jcas, 0, 0);
		tcu.addToIndexes();
		
		DocumentMetaData.create(jcas);
		
		engine.process(jcas);
		
		return jcas;
	}
}
