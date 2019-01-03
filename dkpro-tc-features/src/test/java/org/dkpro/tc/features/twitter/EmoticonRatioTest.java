/*******************************************************************************
 * Copyright 2019
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
package org.dkpro.tc.features.twitter;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.tweet.POS_EMO;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.twitter.EmoticonRatio;

public class EmoticonRatioTest
{
    @Test
    public void emoticonRatioFeatureExtractorTest() throws Exception
    {
        AnalysisEngineDescription desc = createEngineDescription(NoOpAnnotator.class);
        AnalysisEngine engine = createEngine(desc);

        TokenBuilder<Token, Sentence> builder = TokenBuilder.create(Token.class, Sentence.class);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        builder.buildTokens(jcas, "This is a very emotional tweet ;-)");
        POS_EMO emo = new POS_EMO(jcas);
        emo.setBegin(31);
        emo.setEnd(34);
        emo.addToIndexes();

        engine.process(jcas);

        TextClassificationTarget aTarget = new TextClassificationTarget(jcas, 0,
                jcas.getDocumentText().length());
        aTarget.addToIndexes();

        EmoticonRatio extractor = new EmoticonRatio();
        List<Feature> features = new ArrayList<Feature>(extractor.extract(jcas, aTarget));

        Assert.assertEquals(1, features.size());

        for (Feature feature : features) {
            assertFeature(EmoticonRatio.class.getSimpleName(), 0.14, feature, 0.01);
        }
    }
}
