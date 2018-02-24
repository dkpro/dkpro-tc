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
package org.dkpro.tc.io;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

public class SequenceOutcomeReaderTest {

	@Test
	public void testReader() throws Exception {

		CollectionReader reader = CollectionReaderFactory.createReader(SequenceOutcomeReader.class,
				SequenceOutcomeReader.PARAM_SOURCE_LOCATION, "src/test/resources/sequence/",
				SequenceOutcomeReader.PARAM_PATTERNS, "*.txt");

		List<List<String>> readSequences = new ArrayList<>();
		List<List<String>> readOutcomes = new ArrayList<>();

		int seqTargets=0;
		
		while (reader.hasNext()) {
			JCas theJCas = JCasFactory.createJCas();
			reader.getNext(theJCas.getCas());

			Collection<TextClassificationTarget> targets = JCasUtil.select(theJCas, TextClassificationTarget.class);
			List<String> tokens = new ArrayList<>();
			for(TextClassificationTarget target : targets){
				tokens.add(target.getCoveredText());
			}
			readSequences.add(tokens);
			
			Collection<TextClassificationOutcome> outcomeAnnotations = JCasUtil.select(theJCas, TextClassificationOutcome.class);
			List<String> outcomes = new ArrayList<>();
			for(TextClassificationOutcome o : outcomeAnnotations){
				outcomes.add(o.getOutcome());
			}
			readOutcomes.add(outcomes);
			
			seqTargets += JCasUtil.select(theJCas, TextClassificationSequence.class).size();
		}

		assertEquals(3, seqTargets);
		assertEquals(3, readSequences.size());
		assertEquals(3, readOutcomes.size());
		
		assertEquals(4, readSequences.get(0).size());
		//1 - tokens
		assertEquals("This", readSequences.get(0).get(0));
		assertEquals("is", readSequences.get(0).get(1));
		assertEquals("a", readSequences.get(0).get(2));
		assertEquals("test", readSequences.get(0).get(3));
		//2 - outcomes
		assertEquals("DET", readOutcomes.get(0).get(0));
		assertEquals("VERB", readOutcomes.get(0).get(1));
		assertEquals("DET", readOutcomes.get(0).get(2));
		assertEquals("NOUN", readOutcomes.get(0).get(3));
		
		assertEquals(5, readSequences.get(1).size());
		//2 - tokens
		assertEquals("This2", readSequences.get(1).get(0));
		assertEquals("is2", readSequences.get(1).get(1));
		assertEquals("a2", readSequences.get(1).get(2));
		assertEquals("#test2", readSequences.get(1).get(3));
		assertEquals("!", readSequences.get(1).get(4));
		//2 - outcomes		
		assertEquals("DET2", readOutcomes.get(1).get(0));
		assertEquals("VERB2", readOutcomes.get(1).get(1));
		assertEquals("DET2", readOutcomes.get(1).get(2));
		assertEquals("NOUN2", readOutcomes.get(1).get(3));
		assertEquals("PUNCT2", readOutcomes.get(1).get(4));
		
		assertEquals(6, readSequences.get(2).size());
		//3 - tokens
		assertEquals("This3", readSequences.get(2).get(0));
		assertEquals("is3", readSequences.get(2).get(1));
		assertEquals("a3", readSequences.get(2).get(2));
		assertEquals("test3", readSequences.get(2).get(3));
		assertEquals("!", readSequences.get(2).get(4));
		assertEquals("!", readSequences.get(2).get(5));
		//3 - outcomes
		assertEquals("DET3", readOutcomes.get(2).get(0));
		assertEquals("VERB3", readOutcomes.get(2).get(1));
		assertEquals("DET3", readOutcomes.get(2).get(2));
		assertEquals("NOUN3", readOutcomes.get(2).get(3));
		assertEquals("PUNCT3", readOutcomes.get(2).get(4));
		assertEquals("PUNCT3", readOutcomes.get(2).get(5));
	}
	
	@Test
	public void testSkipLineReader() throws Exception {

		CollectionReader reader = CollectionReaderFactory.createReader(SequenceOutcomeReader.class,
				SequenceOutcomeReader.PARAM_SOURCE_LOCATION, "src/test/resources/sequence/",
				SequenceOutcomeReader.PARAM_SKIP_LINES_START_WITH_STRING, "#",
				SequenceOutcomeReader.PARAM_PATTERNS, "*.txt");

		List<List<String>> readSequences = new ArrayList<>();
		List<List<String>> readOutcomes = new ArrayList<>();

		int seqTargets=0;
		
		while (reader.hasNext()) {
			JCas theJCas = JCasFactory.createJCas();
			reader.getNext(theJCas.getCas());

			Collection<TextClassificationTarget> targets = JCasUtil.select(theJCas, TextClassificationTarget.class);
			List<String> tokens = new ArrayList<>();
			for(TextClassificationTarget target : targets){
				tokens.add(target.getCoveredText());
			}
			readSequences.add(tokens);
			
			Collection<TextClassificationOutcome> outcomeAnnotations = JCasUtil.select(theJCas, TextClassificationOutcome.class);
			List<String> outcomes = new ArrayList<>();
			for(TextClassificationOutcome o : outcomeAnnotations){
				outcomes.add(o.getOutcome());
			}
			readOutcomes.add(outcomes);
			
			seqTargets += JCasUtil.select(theJCas, TextClassificationSequence.class).size();
		}
		
		assertEquals(4, readSequences.get(1).size());
		//2 - tokens
		assertEquals("This2", readSequences.get(1).get(0));
		assertEquals("is2", readSequences.get(1).get(1));
		assertEquals("a2", readSequences.get(1).get(2));
		assertEquals("!", readSequences.get(1).get(3));
	}

}
