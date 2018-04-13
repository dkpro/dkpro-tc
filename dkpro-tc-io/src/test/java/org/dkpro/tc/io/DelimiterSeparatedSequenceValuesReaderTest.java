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

import static org.junit.Assert.assertEquals;

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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class DelimiterSeparatedSequenceValuesReaderTest
{

    @Test
    public void testReader() throws Exception
    {

        CollectionReader reader = CollectionReaderFactory.createReader(DelimiterSeparatedSequenceValuesReader.class,
                DelimiterSeparatedSequenceValuesReader.PARAM_SOURCE_LOCATION, "src/test/resources/sequence/",
                DelimiterSeparatedSequenceValuesReader.PARAM_PATTERNS, "posDummy.txt",
                DelimiterSeparatedSequenceValuesReader.PARAM_SEQUENCES_PER_CAS, 1);

        List<List<String>> readSequences = new ArrayList<>();
        List<List<String>> readOutcomes = new ArrayList<>();

        int seqTargets = 0;

        while (reader.hasNext()) {
            JCas theJCas = JCasFactory.createJCas();
            reader.getNext(theJCas.getCas());

            Collection<TextClassificationSequence> sequence = JCasUtil.select(theJCas,
                    TextClassificationSequence.class);
            for (TextClassificationSequence s : sequence) {
                List<TextClassificationTarget> targets = JCasUtil.selectCovered(theJCas,
                        TextClassificationTarget.class, s);
                List<String> tokens = new ArrayList<>();
                for (TextClassificationTarget target : targets) {
                    tokens.add(target.getCoveredText());
                }
                readSequences.add(tokens);
            }

            for (TextClassificationSequence s : sequence) {
                List<TextClassificationOutcome> outcomeAnnotations = JCasUtil.selectCovered(theJCas,
                        TextClassificationOutcome.class, s);
                List<String> outcomes = new ArrayList<>();
                for (TextClassificationOutcome o : outcomeAnnotations) {
                    outcomes.add(o.getOutcome());
                }
                readOutcomes.add(outcomes);
            }

            seqTargets += JCasUtil.select(theJCas, TextClassificationSequence.class).size();
        }

        assertEquals(3, seqTargets);
        assertEquals(3, readSequences.size());
        assertEquals(3, readOutcomes.size());

        assertEquals(4, readSequences.get(0).size());
        // 1 - tokens
        assertEquals("This", readSequences.get(0).get(0));
        assertEquals("is", readSequences.get(0).get(1));
        assertEquals("a", readSequences.get(0).get(2));
        assertEquals("test", readSequences.get(0).get(3));
        // 2 - outcomes
        assertEquals("DET", readOutcomes.get(0).get(0));
        assertEquals("VERB", readOutcomes.get(0).get(1));
        assertEquals("DET", readOutcomes.get(0).get(2));
        assertEquals("NOUN", readOutcomes.get(0).get(3));

        assertEquals(5, readSequences.get(1).size());
        // 2 - tokens
        assertEquals("This2", readSequences.get(1).get(0));
        assertEquals("is2", readSequences.get(1).get(1));
        assertEquals("a2", readSequences.get(1).get(2));
        assertEquals("#test2", readSequences.get(1).get(3));
        assertEquals("!", readSequences.get(1).get(4));
        // 2 - outcomes
        assertEquals("DET2", readOutcomes.get(1).get(0));
        assertEquals("VERB2", readOutcomes.get(1).get(1));
        assertEquals("DET2", readOutcomes.get(1).get(2));
        assertEquals("NOUN2", readOutcomes.get(1).get(3));
        assertEquals("PUNCT2", readOutcomes.get(1).get(4));

        assertEquals(6, readSequences.get(2).size());
        // 3 - tokens
        assertEquals("This3", readSequences.get(2).get(0));
        assertEquals("is3", readSequences.get(2).get(1));
        assertEquals("a3", readSequences.get(2).get(2));
        assertEquals("test3", readSequences.get(2).get(3));
        assertEquals("!", readSequences.get(2).get(4));
        assertEquals("!", readSequences.get(2).get(5));
        // 3 - outcomes
        assertEquals("DET3", readOutcomes.get(2).get(0));
        assertEquals("VERB3", readOutcomes.get(2).get(1));
        assertEquals("DET3", readOutcomes.get(2).get(2));
        assertEquals("NOUN3", readOutcomes.get(2).get(3));
        assertEquals("PUNCT3", readOutcomes.get(2).get(4));
        assertEquals("PUNCT3", readOutcomes.get(2).get(5));
    }

    @Test
    public void testReaderIndexParameter() throws Exception
    {

        CollectionReader reader = CollectionReaderFactory.createReader(DelimiterSeparatedSequenceValuesReader.class,
                DelimiterSeparatedSequenceValuesReader.PARAM_SOURCE_LOCATION, "src/test/resources/sequence/",
                DelimiterSeparatedSequenceValuesReader.PARAM_PATTERNS, "otherFormat.txt",
                DelimiterSeparatedSequenceValuesReader.PARAM_OUTCOME_INDEX, 1,
                DelimiterSeparatedSequenceValuesReader.PARAM_TOKEN_INDEX, 2);

        List<List<String>> readSequences = new ArrayList<>();
        List<List<String>> readOutcomes = new ArrayList<>();

        int seqTargets = 0;

        while (reader.hasNext()) {
            JCas theJCas = JCasFactory.createJCas();
            reader.getNext(theJCas.getCas());

            Collection<TextClassificationSequence> sequences = JCasUtil.select(theJCas,
                    TextClassificationSequence.class);
            for (TextClassificationSequence s : sequences) {
                List<TextClassificationTarget> targets = JCasUtil.selectCovered(theJCas,
                        TextClassificationTarget.class, s);
                List<String> tokens = new ArrayList<>();
                for (TextClassificationTarget target : targets) {
                    tokens.add(target.getCoveredText());
                }
                readSequences.add(tokens);
            }

            Collection<TextClassificationSequence> outcomeSequences = JCasUtil.select(theJCas,
                    TextClassificationSequence.class);
            for (TextClassificationSequence s : outcomeSequences) {
                List<TextClassificationOutcome> outcomeAnnotations = JCasUtil.selectCovered(theJCas,
                        TextClassificationOutcome.class, s);
                List<String> outcomes = new ArrayList<>();
                for (TextClassificationOutcome o : outcomeAnnotations) {
                    outcomes.add(o.getOutcome());
                }
                readOutcomes.add(outcomes);
            }

            seqTargets += JCasUtil.select(theJCas, TextClassificationSequence.class).size();
        }

        assertEquals(2, seqTargets);
        assertEquals(2, readSequences.size());
        assertEquals(2, readOutcomes.size());

        assertEquals(4, readSequences.get(0).size());
        // 1 - tokens
        assertEquals("This", readSequences.get(0).get(0));
        assertEquals("is", readSequences.get(0).get(1));
        assertEquals("a", readSequences.get(0).get(2));
        assertEquals("test", readSequences.get(0).get(3));
        // 2 - outcomes
        assertEquals("DET", readOutcomes.get(0).get(0));
        assertEquals("VERB", readOutcomes.get(0).get(1));
        assertEquals("DET", readOutcomes.get(0).get(2));
        assertEquals("NOUN", readOutcomes.get(0).get(3));

        assertEquals(5, readSequences.get(1).size());
        // 2 - tokens
        assertEquals("This2", readSequences.get(1).get(0));
        assertEquals("is2", readSequences.get(1).get(1));
        assertEquals("a2", readSequences.get(1).get(2));
        assertEquals("test2", readSequences.get(1).get(3));
        assertEquals("!2", readSequences.get(1).get(4));
        // 2 - outcomes
        assertEquals("DET2", readOutcomes.get(1).get(0));
        assertEquals("VERB2", readOutcomes.get(1).get(1));
        assertEquals("DET2", readOutcomes.get(1).get(2));
        assertEquals("NOUN2", readOutcomes.get(1).get(3));
        assertEquals("PUNCT2", readOutcomes.get(1).get(4));
    }

    @Test
    public void testSkipLineReader() throws Exception
    {

        CollectionReader reader = CollectionReaderFactory.createReader(DelimiterSeparatedSequenceValuesReader.class,
                DelimiterSeparatedSequenceValuesReader.PARAM_SOURCE_LOCATION,
                "src/test/resources/sequence/posDummy.txt",
                DelimiterSeparatedSequenceValuesReader.PARAM_SKIP_LINES_START_WITH_STRING, "#");

        List<List<String>> readSequences = new ArrayList<>();
        List<List<String>> readOutcomes = new ArrayList<>();

        while (reader.hasNext()) {
            JCas theJCas = JCasFactory.createJCas();
            reader.getNext(theJCas.getCas());

            Collection<TextClassificationSequence> sequence = JCasUtil.select(theJCas,
                    TextClassificationSequence.class);
            for (TextClassificationSequence s : sequence) {
                List<TextClassificationTarget> targets = JCasUtil.selectCovered(theJCas,
                        TextClassificationTarget.class, s);

                List<String> tokens = new ArrayList<>();
                for (TextClassificationTarget target : targets) {
                    tokens.add(target.getCoveredText());
                }
                readSequences.add(tokens);
            }

            Collection<TextClassificationOutcome> outcomeAnnotations = JCasUtil.select(theJCas,
                    TextClassificationOutcome.class);
            List<String> outcomes = new ArrayList<>();
            for (TextClassificationOutcome o : outcomeAnnotations) {
                outcomes.add(o.getOutcome());
            }
            readOutcomes.add(outcomes);
        }

        assertEquals(4, readSequences.get(1).size());
        // 2 - tokens
        assertEquals("This2", readSequences.get(1).get(0));
        assertEquals("is2", readSequences.get(1).get(1));
        assertEquals("a2", readSequences.get(1).get(2));
        assertEquals("!", readSequences.get(1).get(3));
    }

    @Test
    public void testNumberOfCas() throws Exception
    {

        // all in one
        CollectionReader reader = CollectionReaderFactory.createReader(DelimiterSeparatedSequenceValuesReader.class,
                DelimiterSeparatedSequenceValuesReader.PARAM_SOURCE_LOCATION,
                "src/test/resources/sequence/posDummy.txt",
                DelimiterSeparatedSequenceValuesReader.PARAM_SEQUENCES_PER_CAS, 10);

        int sentCount = 0;
        int tokenCount = 0;
        int createdCas = 0;
        while (reader.hasNext()) {
            JCas theJCas = JCasFactory.createJCas();
            reader.getNext(theJCas.getCas());
            sentCount += JCasUtil.select(theJCas, Sentence.class).size();
            tokenCount += JCasUtil.select(theJCas, Token.class).size();
            createdCas++;
        }
        assertEquals(1, createdCas);
        assertEquals(3, sentCount);
        assertEquals(15, tokenCount);

        // one per cas
        reader = CollectionReaderFactory.createReader(DelimiterSeparatedSequenceValuesReader.class,
                DelimiterSeparatedSequenceValuesReader.PARAM_SOURCE_LOCATION,
                "src/test/resources/sequence/posDummy.txt",
                DelimiterSeparatedSequenceValuesReader.PARAM_SEQUENCES_PER_CAS, 1);

        sentCount = 0;
        tokenCount = 0;
        createdCas = 0;
        while (reader.hasNext()) {
            JCas theJCas = JCasFactory.createJCas();
            reader.getNext(theJCas.getCas());
            sentCount += JCasUtil.select(theJCas, Sentence.class).size();
            tokenCount += JCasUtil.select(theJCas, Token.class).size();
            createdCas++;
        }
        assertEquals(3, createdCas);
        assertEquals(3, sentCount);
        assertEquals(15, tokenCount);

        // two in first one third in second one
        reader = CollectionReaderFactory.createReader(DelimiterSeparatedSequenceValuesReader.class,
                DelimiterSeparatedSequenceValuesReader.PARAM_SOURCE_LOCATION,
                "src/test/resources/sequence/posDummy.txt",
                DelimiterSeparatedSequenceValuesReader.PARAM_SEQUENCES_PER_CAS, 2);

        sentCount = 0;
        tokenCount = 0;
        createdCas = 0;
        while (reader.hasNext()) {
            JCas theJCas = JCasFactory.createJCas();
            reader.getNext(theJCas.getCas());
            sentCount += JCasUtil.select(theJCas, Sentence.class).size();
            tokenCount += JCasUtil.select(theJCas, Token.class).size();
            createdCas++;
        }
        assertEquals(2, createdCas);
        assertEquals(3, sentCount);
        assertEquals(15, tokenCount);
    }

}
