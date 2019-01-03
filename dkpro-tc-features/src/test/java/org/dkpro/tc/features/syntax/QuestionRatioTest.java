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
package org.dkpro.tc.features.syntax;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.dkpro.tc.features.syntax.QuestionsRatioFeatureExtractor.FN_QUESTION_RATIO;
import static org.dkpro.tc.testing.FeatureTestUtil.assertFeature;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.Assert;

public class QuestionRatioTest
{
    @Test
    public void questionRatioFeatureExtractorTest() throws Exception
    {
        AnalysisEngineDescription desc = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngine engine = createEngine(desc);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("Is he a tester???? Really?? He is a tester! Oh yes.");
        engine.process(jcas);

        TextClassificationTarget aTarget = new TextClassificationTarget(jcas, 0,
                jcas.getDocumentText().length());
        aTarget.addToIndexes();

        QuestionsRatioFeatureExtractor extractor = new QuestionsRatioFeatureExtractor();
        List<Feature> features = new ArrayList<Feature>(extractor.extract(jcas, aTarget));

        Assert.assertEquals(1, features.size());

        for (Feature feature : features) {
            assertFeature(FN_QUESTION_RATIO, 0.5, feature);
        }
    }
}