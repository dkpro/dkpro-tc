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
package de.tudarmstadt.ukp.dkpro.tc.features.twitter;

import static de.tudarmstadt.ukp.dkpro.tc.core.util.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.tweet.EMO;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class EmoticonRatioDFETest
{
    @Test
    public void emoticonRatioFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngineDescription desc = createEngineDescription(
                NoOpAnnotator.class);
        AnalysisEngine engine = createEngine(desc);

        TokenBuilder<Token, Sentence> builder = TokenBuilder.create(Token.class, Sentence.class);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        builder.buildTokens(jcas, "This is a very emotional tweet ;-)");
        EMO emo = new EMO(jcas);
        emo.setBegin(31);
        emo.setEnd(34);
        emo.addToIndexes();

        engine.process(jcas);

        EmoticonRatioDFE extractor = new EmoticonRatioDFE();
        List<Feature> features = extractor.extract(jcas);

        Assert.assertEquals(1, features.size());

        for (Feature feature : features) {
            assertFeature(EmoticonRatioDFE.class.getSimpleName(), 0.14, feature, 0.01);
        }
    }
}
