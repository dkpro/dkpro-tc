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
package org.dkpro.tc.features.tcu;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.tcu.CurrentUnit;
import org.dkpro.tc.features.tcu.NextNextUnit;
import org.dkpro.tc.features.tcu.NextUnit;
import org.dkpro.tc.features.tcu.PrevPrevUnit;
import org.dkpro.tc.features.tcu.PrevUnit;

public class TestTextClassificationTargetTest {
	@Test
	public void testTokenFeatureExtractors() throws Exception {
		
		Object [] o = setUp();
		JCas jcas = (JCas) o[0];
		TextClassificationTarget tcu = (TextClassificationTarget)o[1];

		PrevPrevUnit pp = new PrevPrevUnit();
		Set<Feature> extract = pp.extract(jcas, tcu);
		assertEquals(1, extract.size());
		assertEquals("It", extract.iterator().next().getValue());
		
		PrevUnit p = new PrevUnit();
		extract= p.extract(jcas, tcu);
		assertEquals(1, extract.size());
		assertEquals("is", extract.iterator().next().getValue());
		
		CurrentUnit curr = new CurrentUnit();
		extract = curr.extract(jcas, tcu);
		assertEquals(1, extract.size());
		assertEquals("raining", extract.iterator().next().getValue());
		
		NextUnit n = new NextUnit();
		extract = n.extract(jcas, tcu);
		assertEquals(1, extract.size());
		assertEquals("all", extract.iterator().next().getValue());
		
		NextNextUnit nn = new NextNextUnit();
		extract = nn.extract(jcas, tcu);
		assertEquals(1, extract.size());
		assertEquals("day", extract.iterator().next().getValue());
		
	}

	private Object[] setUp() throws Exception {
		JCas jcas = JCasFactory.createJCas();
		jcas.setDocumentText("It is raining all day");
		
		DocumentMetaData dmd = new DocumentMetaData(jcas);
		dmd.setDocumentId("1");
		dmd.addToIndexes();

		AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);
		engine.process(jcas.getCas());

		ArrayList<Token> arrayList = new ArrayList<Token>(JCasUtil.select(
				jcas, Token.class));
		
		Token bb = arrayList.get(0);
		TextClassificationTarget tcbb = new TextClassificationTarget(jcas,
				bb.getBegin(), bb.getEnd());
		tcbb.addToIndexes();
		
		Token b = arrayList.get(1);
		TextClassificationTarget tcb = new TextClassificationTarget(jcas,
				b.getBegin(), b.getEnd());
		tcb.addToIndexes();
		
		Token c = arrayList.get(2);
		TextClassificationTarget tcu = new TextClassificationTarget(jcas,
				c.getBegin(), c.getEnd());
		tcu.addToIndexes();
		
		Token n = arrayList.get(3);
		TextClassificationTarget tcn = new TextClassificationTarget(jcas,
				n.getBegin(), n.getEnd());
		tcn.addToIndexes();
		
		Token nn = arrayList.get(4);
		TextClassificationTarget tcnn = new TextClassificationTarget(jcas,
				nn.getBegin(), nn.getEnd());
		tcnn.addToIndexes();
		
		return new Object[] {jcas, tcu};
	}
}