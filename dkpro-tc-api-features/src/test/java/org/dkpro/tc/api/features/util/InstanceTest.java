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
package org.dkpro.tc.api.features.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.Instance;
import org.junit.Test;

public class InstanceTest
{

    @Test
    public void instanceInitializationByListTest() throws Exception
    {
        Feature f1 = new Feature("feature1", "value1", FeatureType.STRING);
        Feature f2 = new Feature("feature2", "value1", FeatureType.STRING);
        List<Feature> features = new ArrayList<>();
        features.add(f1);
        features.add(f2);
        Instance instance = new Instance(features, "outcome");

        assertEquals(2, instance.getFeatures().size());
    }

    @Test
    public void instanceInitializationWithArrayOfOutcomes() throws Exception
    {
        Feature f1 = new Feature("feature1", "value1", FeatureType.STRING);
        Feature f2 = new Feature("feature2", "value1", FeatureType.STRING);
        List<Feature> features = new ArrayList<>();
        features.add(f1);
        features.add(f2);
        Instance instance = new Instance(features, "outcome", "outcome2");

        assertEquals(2, instance.getFeatures().size());
        assertEquals(2, instance.getOutcomes().size());
    }

    @Test
    public void instanceInitializationWithListOfOutcomes() throws Exception
    {
        Feature f1 = new Feature("feature1", "value1", FeatureType.STRING);
        Feature f2 = new Feature("feature2", "value1", FeatureType.STRING);
        List<Feature> features = new ArrayList<>();
        features.add(f1);
        features.add(f2);
        List<String> outcomes = new ArrayList<>();
        outcomes.add("outcome1");
        outcomes.add("outcome2");
        Instance instance = new Instance(features, outcomes);

        assertEquals(2, instance.getFeatures().size());
        assertEquals(2, instance.getOutcomes().size());
    }

    @Test
    public void instanceInitializationBySetTest() throws Exception
    {
        Feature f1 = new Feature("feature1", "value1", FeatureType.STRING);
        Feature f2 = new Feature("feature2", "value1", FeatureType.STRING);
        Set<Feature> features = new HashSet<Feature>();
        features.add(f1);
        features.add(f2);
        Instance instance = new Instance(features, "outcome");

        assertEquals(2, instance.getFeatures().size());
    }

    @Test
    public void instanceEmptyInitializationTest() throws Exception
    {
        Instance instance = new Instance();

        assertEquals(0, instance.getFeatures().size());

        Feature f3 = new Feature("feature3", "value1", FeatureType.STRING);
        instance.addFeature(f3);
        assertEquals(1, instance.getFeatures().size());
    }

    @Test
    public void instanceAddSingleFeatureTest() throws Exception
    {
        Feature f1 = new Feature("feature1", "value1", FeatureType.STRING);
        Feature f2 = new Feature("feature2", "value1", FeatureType.STRING);
        List<Feature> features = new ArrayList<>();
        features.add(f1);
        features.add(f2);
        Instance instance = new Instance(features, "outcome");

        Feature f3 = new Feature("feature3", "value1", FeatureType.STRING);
        instance.addFeature(f3);
        assertEquals(3, instance.getFeatures().size());
    }

    @Test
    public void instanceAddFeatureSetTest() throws Exception
    {
        Feature f1 = new Feature("feature1", "value1", FeatureType.STRING);
        Feature f2 = new Feature("feature2", "value1", FeatureType.STRING);
        List<Feature> features = new ArrayList<>();
        features.add(f1);
        features.add(f2);
        Instance instance = new Instance(features, "outcome");

        Set<Feature> s = new HashSet<Feature>();
        Feature f3 = new Feature("feature3", "value3", FeatureType.STRING);
        Feature f4 = new Feature("feature4", "value4", FeatureType.STRING);
        s.add(f3);
        s.add(f4);

        instance.addFeatures(s);

        assertEquals(4, instance.getFeatures().size());

    }

    @Test
    public void instanceAddFeatureListTest() throws Exception
    {
        Feature f1 = new Feature("feature1", "value1", FeatureType.STRING);
        Feature f2 = new Feature("feature2", "value1", FeatureType.STRING);
        List<Feature> features = new ArrayList<>();
        features.add(f1);
        features.add(f2);
        Instance instance = new Instance(features, "outcome");

        List<Feature> s = new ArrayList<Feature>();
        Feature f3 = new Feature("feature3", "value3", FeatureType.STRING);
        Feature f4 = new Feature("feature4", "value4", FeatureType.STRING);
        s.add(f3);
        s.add(f4);

        instance.addFeatures(s);

        assertEquals(4, instance.getFeatures().size());

    }

    @Test
    public void instanceSetSingleOutcomeTest() throws Exception
    {

        Feature f1 = new Feature("feature1", "value1", FeatureType.STRING);
        Instance instance = new Instance();
        instance.addFeature(f1);

        instance.setOutcomes("outcome");
        assertEquals(1, instance.getOutcomes().size());
        assertEquals("outcome", instance.getOutcome());
    }

    @Test
    public void instanceNoSetOutcomeTest() throws Exception
    {

        Instance instance = new Instance();

        assertEquals(null, instance.getOutcome());
    }

    @Test
    public void instanceSetSeveralOutcomesTest() throws Exception
    {

        Feature f1 = new Feature("feature1", "value1", FeatureType.STRING);
        Feature f2 = new Feature("feature2", "value1", FeatureType.STRING);
        List<Feature> features = new ArrayList<>();
        features.add(f1);
        features.add(f2);
        Instance instance = new Instance(features, "outcome");

        List<String> newOutcomes = new ArrayList<String>();
        newOutcomes.add("outcome1");
        newOutcomes.add("outcome2");

        instance.setOutcomes(newOutcomes);

        assertEquals(2, instance.getOutcomes().size());
    }

    @Test
    public void instanceSetNewFeatureCollectionBySetterTest() throws TextClassificationException
    {
        Feature f1 = new Feature("feature1", "value1", FeatureType.STRING);
        Feature f2 = new Feature("feature2", "value1", FeatureType.STRING);
        List<Feature> features = new ArrayList<>();
        features.add(f1);
        features.add(f2);
        Instance instance = new Instance(features, "outcome");

        Feature f3 = new Feature("feature3", "value1", FeatureType.STRING);
        Feature f4 = new Feature("feature4", "value1", FeatureType.STRING);
        Set<Feature> newFeatures = new HashSet<>();
        newFeatures.add(f3);
        newFeatures.add(f4);
        instance.setFeatures(newFeatures);

        assertEquals(2, instance.getFeatures().size());

        Iterator<Feature> iterator = instance.getFeatures().iterator();
        Feature next = iterator.next();
        iterator.hasNext();
        Feature next2 = iterator.next();

        // The order does not matter for this test - the input set orders the values "somehow"

        if (!next.getName().equals("feature3") && !next2.getName().equals("feature3")) {
            fail("Expected to find a feature named [feature3]");
        }
        if (!next.getName().equals("feature4") && !next2.getName().equals("feature4")) {
            fail("Expected to find a feature named [feature4]");
        }
    }

    @Test
    public void testSetterAndGetter() throws TextClassificationException
    {
        Instance i = new Instance(Arrays.asList(new Feature("dummy", 0, FeatureType.STRING)),
                "RESULT");
        i.setJcasId(4711);
        i.setSequenceId(234);
        i.setWeight(2.0);
        i.setSequencePosition(3);

        assertEquals(4711, i.getJcasId());
        assertEquals(234, i.getSequenceId());
        assertEquals(2.0, i.getWeight(), 0.001);
        assertEquals(3, i.getSequencePosition());
        assertTrue(i.toString().startsWith("4711-234"));
    }
}
