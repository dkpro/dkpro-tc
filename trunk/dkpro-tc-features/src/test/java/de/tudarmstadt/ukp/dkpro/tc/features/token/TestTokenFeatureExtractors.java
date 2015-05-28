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
package de.tudarmstadt.ukp.dkpro.tc.features.token;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public class TestTokenFeatureExtractors {
	@Test
	public void testTokenFeatureExtractors() throws Exception {
		
		Object [] o = setUp();
		JCas jcas = (JCas) o[0];
		TextClassificationUnit tcu = (TextClassificationUnit)o[1];

		PreviousToken prev = new PreviousToken();
		List<Feature> extract = prev.extract(jcas, tcu);
		assertEquals(1, extract.size());
		assertEquals("It", extract.get(0).getValue());
		
		CurrentToken curr = new CurrentToken();
		extract = curr.extract(jcas, tcu);
		assertEquals(1, extract.size());
		assertEquals("is", extract.get(0).getValue());
		
		NextToken next = new NextToken();
		extract = next.extract(jcas, tcu);
		assertEquals(1, extract.size());
		assertEquals("raining", extract.get(0).getValue());
		
	}

	private Object[] setUp() throws Exception {
		JCas jcas = JCasFactory.createJCas();
		jcas.setDocumentText("It is raining.");
		
		DocumentMetaData dmd = new DocumentMetaData(jcas);
		dmd.setDocumentId("1");
		dmd.addToIndexes();

		AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);
		engine.process(jcas.getCas());

		ArrayList<Token> arrayList = new ArrayList<Token>(JCasUtil.select(
				jcas, Token.class));
		Token is = arrayList.get(1);
		TextClassificationUnit tcu = new TextClassificationUnit(jcas,
				is.getBegin(), is.getEnd());
		tcu.addToIndexes();
		return new Object[] {jcas, tcu};
	}
}