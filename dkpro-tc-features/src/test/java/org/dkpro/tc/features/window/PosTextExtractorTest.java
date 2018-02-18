/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.features.window;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class PosTextExtractorTest {
	@Test
	public void numberOfHashTagsFeatureExtractorTest() throws Exception {
		
		AggregateBuilder ab = new AggregateBuilder();
		ab.add(createEngineDescription(BreakIteratorSegmenter.class));
		ab.add(createEngineDescription(OpenNlpPosTagger.class));
		AnalysisEngine engine = ab.createAggregate();
		
		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("en");
		jcas.setDocumentText("This is a long story");
		engine.process(jcas);

		TextClassificationTarget aTarget = new TextClassificationTarget(jcas, 8, 9);
		aTarget.addToIndexes();

		TcFeature feature = TcFeatureFactory.create(POSExtractor.class,
				POSExtractor.PARAM_NUM_PRECEEDING, 1, POSExtractor.PARAM_NUM_FOLLOWING, 1);

		POSExtractor r = FeatureUtil.createResource(feature);
		List<Feature> extract = new ArrayList<Feature>(r.extract(jcas, aTarget));

		assertEquals(3, extract.size());
		assertEquals("POSu910u93", extract.get(0).getName());
		assertEquals("DT", extract.get(0).getValue());
		assertEquals("POSu91u451u93", extract.get(1).getName());
		assertEquals("VBZ", extract.get(1).getValue());
		assertEquals("POSu911u93", extract.get(2).getName());
		assertEquals("JJ", extract.get(2).getValue());
	}
}
