/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.features.entityrecognition;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationUnit;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Person;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

public class NEFeatureExtractorTest
{
    @Test
    public void nEFeatureExtractorTest()
        throws Exception
    {

        AnalysisEngine engine = createEngine(NoOpAnnotator.class);

        JCas jcas = engine.newJCas();
        engine.process(jcas);

        TextClassificationUnit target = new TextClassificationUnit(jcas, 0, 22);
        target.addToIndexes();

        Location l1 = new Location(jcas, 0, 5);
        Person p1 = new Person(jcas, 0, 5);
        Organization o1 = new Organization(jcas, 0, 5);
        Sentence s1 = new Sentence(jcas, 0, 15);
        Sentence s2 = new Sentence(jcas, 15, 22);
        l1.addToIndexes();
        p1.addToIndexes();
        o1.addToIndexes();
        s1.addToIndexes();
        s2.addToIndexes();

        NEFeatureExtractor extractor = new NEFeatureExtractor();

        Set<Feature> features1 = extractor.extract(jcas, target);
        assertEquals(6, features1.size());

        testFeatures(features1, 1, 1, 1, 0.5f, 0.5f, 0.5f);

    }

    private void testFeatures(Set<Feature> features, int expectedValue1, int expectedValue2,
            int expectedValue3, float... expectedValues4)
    {

        for (Feature f : features) {

            if (f.getName().equals("NrOfOrganizationEntities")) {
                assertEquals(expectedValue1, f.getValue());
            }
            if (f.getName().equals("NrOfPersonEntities")) {
                assertEquals(expectedValue2, f.getValue());
            }
            if (f.getName().equals("NrOfLocationEntities")) {
                assertEquals(expectedValue3, f.getValue());
            }
            if (f.getName().equals("NrOfOrganizationEntitiesPerSent")) {
                assertEquals(expectedValues4[0], f.getValue());
            }
            if (f.getName().equals("NrOfPersonEntitiesPerSent")) {
                assertEquals(expectedValues4[1], f.getValue());
            }
            if (f.getName().equals("NrOfLocationEntitiesPerSent")) {
                assertEquals(expectedValues4[2], f.getValue());
            }

        }

    }
}