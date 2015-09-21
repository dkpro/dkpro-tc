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
package de.tudarmstadt.ukp.dkpro.tc.api.features.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;

public class InstanceTest {

	@Test
	public void doubleInstanceTest() throws Exception {
		Feature f1 = new Feature("feature", "value");
		Feature f2 = new Feature("feature", "value");
		List<Feature> features = new ArrayList<>();
		features.add(f1);
		features.add(f2);
		Instance instance = new Instance(features, "outcome");
		assertEquals(1, instance.getFeatures().size());
	}

	@Test
	public void instanceCountTest() throws Exception {
		Feature f1 = new Feature("feature1", "value1");
		Feature f2 = new Feature("feature2", "value1");
		List<Feature> features = new ArrayList<>();
		features.add(f1);
		features.add(f2);
		Instance instance = new Instance(features, "outcome");

		assertEquals(2, instance.getFeatures().size());

		Feature f3 = new Feature("feature3", "value1");
		instance.addFeature(f3);
		assertEquals(3, instance.getFeatures().size());
	}

	@Test
	public void instanceTestOutcome() throws Exception {

		Feature f1 = new Feature("feature1", "value1");
		Instance instance = new Instance();
		instance.addFeature(f1);

		instance.setOutcomes("outcome");
		assertEquals(1, instance.getOutcomes().size());
		assertEquals("outcome", instance.getOutcome());
	}
}
