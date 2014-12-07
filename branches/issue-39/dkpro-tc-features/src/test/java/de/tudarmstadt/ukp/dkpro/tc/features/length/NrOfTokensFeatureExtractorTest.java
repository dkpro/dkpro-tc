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
package de.tudarmstadt.ukp.dkpro.tc.features.length;

import static de.tudarmstadt.ukp.dkpro.tc.testing.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class NrOfTokensFeatureExtractorTest
{
    @Test
    public void nrOfTokensFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("This is a test.");
        engine.process(jcas);

        NrOfTokensDFE extractor = new NrOfTokensDFE();
        List<Feature> features = extractor.extract(jcas);

        Assert.assertEquals(1, features.size());

        Iterator<Feature> iter = features.iterator();
        assertFeature(NrOfTokensDFE.FN_NR_OF_TOKENS, 5., iter.next());
    }

    @Test
    public void nrOfTokensExteremeFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("");
        engine.process(jcas);

        NrOfTokensDFE extractor = new NrOfTokensDFE();
        List<Feature> features = extractor.extract(jcas);

        Assert.assertEquals(1, features.size());

        Iterator<Feature> iter = features.iterator();
        assertFeature(NrOfTokensDFE.FN_NR_OF_TOKENS, 0., iter.next());
    }
}