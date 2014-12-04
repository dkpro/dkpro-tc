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

package de.tudarmstadt.ukp.dkpro.tc.features.readability.util;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.NP;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.VP;
import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

public class ParsePatternUtilTest
{
    private static AnalysisEngine engine;

    private static void initializeEngine()
        throws ResourceInitializationException
    {
        engine = createEngine(createEngineDescription(
                createEngineDescription(OpenNlpSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class),
                createEngineDescription(BerkeleyParser.class)));
    }

    @Test
    public void testParseDepth()
        throws Exception
    {
        String[] texts = { "Okay.", "Peter eats.", "We see it.", "I use the pen and the paper.",
                "We use it when a girl in our dorm is acting like a spoiled and nervous child." };
        int[] depths = { 2, 3, 4, 5, 10 };
        initializeEngine();
        for (int i = 0; i < texts.length; i++) {
            JCas jcas = engine.newJCas();
            jcas.setDocumentLanguage("en");
            jcas.setDocumentText(texts[i]);
            engine.process(jcas);
            Assert.assertEquals(depths[i], ParsePatternUtils.getParseDepth((Sentence) JCasUtil
                    .select(jcas, Sentence.class).toArray()[0]));
        }

    }

    @Test
    public void testIsAppositive()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        String[] texts = { "The desire to succeed is what drives us.",
                "The insect, a cockroach, is crawling across the kitchen table." };
        boolean[] results = { true, false, true, true, false, false };

        initializeEngine();
        int i = 0;
        for (String text : texts) {
            JCas jcas = engine.newJCas();
            jcas.setDocumentLanguage("en");
            jcas.setDocumentText(text);
            engine.process(jcas);

            for (NP c : JCasUtil.select(jcas, NP.class)) {
                Assert.assertEquals(results[i], ParsePatternUtils.isAppositive(c));
                i++;
            }

        }
    }

    @Test
    @Ignore
    public void testIsComplexNominal()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        String[] texts = { "A girl in our dorm acts like a spoiled child.", "I know what I like",
                "Everybody knows that you like to read", "I know you like to read.",
                "Going to school is important.", "To be or not to be is the question." };
        boolean[] results = { false, false, true, false, false, false, false, false, true, false,
                false, false, false, true, false, false, false, false, false, false, false, false,
                true, false, false, false, false, false, false, false, false, false, false, true,
                false, false, false, false, false, false, false, false, false, true, false, false,
                false, false, false, false, false, true, false, false, false, false, false, false };
        initializeEngine();
        int i = 0;
        System.out.println("Complex Nominals: ");
        for (String text : texts) {
            JCas jcas = engine.newJCas();
            jcas.setDocumentLanguage("en");
            jcas.setDocumentText(text);
            engine.process(jcas);
            for (Constituent c : JCasUtil.select(jcas, Constituent.class)) {
                if (ParsePatternUtils.isComplexNominal(c)) {
                    System.out.println(c.getCoveredText());
                }
                Assert.assertEquals(results[i], ParsePatternUtils.isComplexNominal(c));
                i++;
            }

        }
    }

    @Test
    public void testIsVerbphrase()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        String text = "We use it when a girl in our dorm is acting like a spoiled child.";

        initializeEngine();
        System.out.println("Verb phrases");

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText(text);
        engine.process(jcas);
        int verbphrases = 0;
        for (VP c : JCasUtil.select(jcas, VP.class)) {
            if (ParsePatternUtils.isVerbPhrase(c)) {
                System.out.println(c.getCoveredText());
                verbphrases++;
                Assert.assertTrue(c.getCoveredText().equals(
                        "use it when a girl in our dorm is acting like a spoiled child")
                        || c.getCoveredText().equals("is acting like a spoiled child"));
            }
        }
        Assert.assertEquals(verbphrases, 2);
    }

    @Test
    public void testIsClause()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        String text = "We use it when a girl in our dorm is acting like a spoiled child.";

        initializeEngine();

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText(text);
        engine.process(jcas);
        int clauses = 0;
        for (Constituent c : JCasUtil.select(jcas, Constituent.class)) {
            if (ParsePatternUtils.isClause(c)) {
                clauses++;
                Assert.assertTrue("We use it when a girl in our dorm is acting like a spoiled child."
                        .equals(c.getCoveredText())
                        || "a girl in our dorm is acting like a spoiled child".equals(c
                                .getCoveredText()));
            }
        }
        Assert.assertEquals(2, clauses);
    }

    @Test
    public void testIsDependentClause()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        String text = "We use it when a girl in our dorm is acting like a spoiled child.";

        initializeEngine();

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText(text);
        engine.process(jcas);

        for (Constituent c : JCasUtil.select(jcas, Constituent.class)) {
            if (ParsePatternUtils.isDependentClause(c)) {
                Assert.assertTrue("when a girl in our dorm is acting like a spoiled child".equals(c
                        .getCoveredText()));
            }
        }

    }

    @Test
    public void testIsTunit()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        String text = "We use it when a girl in our dorm is acting like a spoiled child.";

        initializeEngine();

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText(text);
        engine.process(jcas);

        for (Constituent c : JCasUtil.select(jcas, Constituent.class)) {
            if (ParsePatternUtils.isTunit(c)) {
                Assert.assertTrue("We use it when a girl in our dorm is acting like a spoiled child."
                        .equals(c.getCoveredText()));
            }
        }
    }

    @Test
    public void testIsComplexTunit()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        String text = "We use it when a girl in our dorm is acting like a spoiled child.";

        initializeEngine();

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText(text);
        engine.process(jcas);

        for (Constituent c : JCasUtil.select(jcas, Constituent.class)) {
            if (ParsePatternUtils.isComplexTunit(c)) {
                Assert.assertTrue("We use it when a girl in our dorm is acting like a spoiled child."
                        .equals(c.getCoveredText()));
            }
        }
    }

    @Test
    @Ignore
    public void testIsCoord()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        String text = "It is a long and dark way home and we want to get there all safe and sound.";

        initializeEngine();

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText(text);
        engine.process(jcas);
        int coordinates = 0;
        for (Constituent c : JCasUtil.select(jcas, Constituent.class)) {

            if (ParsePatternUtils.isCoordinate(c)) {
                coordinates++;
                System.out.println(c.getCoveredText());
                Assert.assertTrue("long and dark".equals(c.getCoveredText())
                        || "safe and sound".equals(c.getCoveredText()));
            }
        }
        Assert.assertEquals(2, coordinates);
    }
}