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
package de.tudarmstadt.ukp.dkpro.tc.features.readability;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.Ignore;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class TraditionalReadabilityMeasuresExtractorTest
{
    public static class Annotator
        extends JCasAnnotator_ImplBase
    {
        final static String MODEL_KEY = "ReadabilityMeasureExtractorResource";
        @ExternalResource(key = MODEL_KEY)
        private TraditionalReadabilityMeasuresFeatureExtractor model;

        @Override
        public void process(JCas aJCas)
            throws AnalysisEngineProcessException
        {
            // System.out.println(model.getClass().getName());
            List<Feature> features;

            try {
                features = model.extract(aJCas);
                // System.out.println(features);
                Assert.assertEquals(7.7, (double) features.get(0).getValue(), 0.1);
                Assert.assertEquals(11.6, (double) features.get(1).getValue(), 0.1);
            }
            catch (TextClassificationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    @Ignore
    public void readabilityFeatureExtractorTest()
        throws Exception
    {
        // HashMap<String, Double> correctResult = new HashMap<String, Double>();
        // correctResult.put("kincaid", 7.6);
        // correctResult.put("ari", 9.1);
        // correctResult.put("coleman_liau", 11.6);
        // correctResult.put("flesch", 70.6);
        // correctResult.put("lix", 5.0);
        // correctResult.put("smog", 9.9);
        // correctResult.put("fog", 10.6);

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/test_document_en.txt");
        AnalysisEngineDescription desc = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription extractor = createEngineDescription(
                Annotator.class,
                Annotator.MODEL_KEY,
                createExternalResourceDescription(TraditionalReadabilityMeasuresFeatureExtractor.class),
                TraditionalReadabilityMeasuresFeatureExtractor.PARAM_ADD_COLEMANLIAU, "true");
        JCasIterable pipeline = new JCasIterable(reader, desc, extractor);

        for (JCas jcas : pipeline) {

        }

    }
}