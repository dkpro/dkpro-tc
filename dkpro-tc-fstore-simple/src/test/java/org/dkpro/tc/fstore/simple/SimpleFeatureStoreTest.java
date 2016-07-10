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
package org.dkpro.tc.fstore.simple;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.fstore.simple.DenseFeatureStore;

public class SimpleFeatureStoreTest {

	@Test
	public void basicTest() 
		throws Exception
    {
		FeatureStore fs = new DenseFeatureStore();
		
		Feature f1 = new Feature("feature1", "value1");
		Feature f2 = new Feature("feature2", "value2");
		List<Feature> features = new ArrayList<>();
		features.add(f1);
		features.add(f2);
		Instance instance = new Instance(features, "outcome");
		fs.addInstance(instance);
		fs.addInstance(instance);
		
		assertEquals(2, fs.getNumberOfInstances());
		assertEquals("outcome", fs.getUniqueOutcomes().first());
		assertEquals(f1, fs.getInstance(0).getFeatures().iterator().next());
	}
}
