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

package org.dkpro.tc.features.readability;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.Ignore;

import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationUnit;
import org.dkpro.tc.features.readability.LexicalVariationExtractor;

public class LexicalVariationExtractorTest
{
    @Ignore
    public void testLexicalVariationExtractor()
        throws Exception
    {
        String text = FileUtils
                .readFileToString(new File("src/test/resources/test_document_en.txt"));

        AnalysisEngineDescription desc = createEngineDescription(
                createEngineDescription(OpenNlpSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class),
                createEngineDescription(ClearNlpLemmatizer.class));
        AnalysisEngine engine = createEngine(desc);
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText(text);
        engine.process(jcas);
        
        TextClassificationUnit target = new TextClassificationUnit(jcas, 0, text.length());
        target.addToIndexes();

        LexicalVariationExtractor extractor = new LexicalVariationExtractor();
        Set<Feature> features = extractor.extract(jcas,target);

        Assert.assertEquals(14, features.size());

        Iterator<Feature> featIter = features.iterator();
        Assert.assertEquals((double) featIter.next().getValue(), 4.2, 0.1);
        Assert.assertEquals((double) featIter.next().getValue(), 1.6, 0.1);
        Assert.assertEquals((double) featIter.next().getValue(), 0.9, 0.1);
        Assert.assertEquals((double) featIter.next().getValue(), 3.7, 0.1);
        Assert.assertEquals((double) featIter.next().getValue(), 0.4, 0.1);
        Assert.assertEquals((double) featIter.next().getValue(), 0.4, 0.1);
        Assert.assertEquals((double) featIter.next().getValue(), 1.75, 0.1);
        Assert.assertEquals((double) featIter.next().getValue(), 7.4, 0.1);
        Assert.assertEquals((double) featIter.next().getValue(), 0.15, 0.1);
        Assert.assertEquals((double) featIter.next().getValue(), 0.1, 0.1);
        Assert.assertEquals((double) featIter.next().getValue(), 0.2, 0.1);
        Assert.assertEquals((double) featIter.next().getValue(), 0.6, 0.1);
        Assert.assertEquals((double) featIter.next().getValue(), 1.5, 0.1);
        Assert.assertEquals((double) featIter.next().getValue(), 110.3, 0.1);
    }
}