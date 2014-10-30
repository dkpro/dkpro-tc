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
package de.tudarmstadt.ukp.dkpro.tc.fstore.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;

/**
 * Resamples the instances in order to achieve a uniform class distribution.
 * If the class distribution is already uniform, nothing is changed.
 * In all other cases, this results in dropping some instances.
 * 
 * FIXME: This is currently optimized for memory consumption and might be slow for large feature stores.
 * If there is enough memory (at least 2x the size of the current feature store) a time optimized version should simply
 * create a new feature store that only holds the selected instances.
 * In the worst case, this would double memory consumption.
 */
public class UniformClassDistributionFilter
	implements FeatureStoreFilter
{

	@Override
	public void applyFilter(FeatureStore store) {
		
		// create mapping from outcomes to instance offsets in the feature store
		Map<String, List<Integer>> outcome2instanceOffset = new HashMap<String, List<Integer>>();
		for (int i=0; i<store.getNumberOfInstances(); i++) {
			Instance instance = store.getInstance(i);
			String outcome = instance.getOutcome();
			List<Integer> offsets;
			if (outcome2instanceOffset.containsKey(outcome)) {
				offsets = outcome2instanceOffset.get(outcome);
			}
			else {
				offsets = new ArrayList<>();
			}
			offsets.add(i);
			outcome2instanceOffset.put(outcome, offsets);
		}
		
		// find the smallest class
		int minClassSize = Integer.MAX_VALUE;
		for (String outcome : outcome2instanceOffset.keySet()) {
			int classSize = outcome2instanceOffset.get(outcome).size();
			if (classSize < minClassSize) {
				minClassSize = classSize;
			}
		}
		
		// resample all but the smallest class to the same size as the smallest class
		// return the offsets of the instances that should be deleted
		SortedSet<Integer> offsetsToDelete = new TreeSet<>();
		for (String outcome : outcome2instanceOffset.keySet()) {
			int classSize = outcome2instanceOffset.get(outcome).size();
			if (classSize != minClassSize) {
				offsetsToDelete.addAll(resample(outcome2instanceOffset.get(outcome), minClassSize));
			}
		}
		
		int nrOfDeleted = 0;
		for (int offsetToDelete : offsetsToDelete) {
			store.deleteInstance(offsetToDelete - nrOfDeleted);
			nrOfDeleted++;
		}
	}

	private List<Integer> resample(List<Integer> offsets, int targetSize) {
		List<Integer> shuffledOffsets = new ArrayList<>(offsets);
		Collections.shuffle(shuffledOffsets);
		return shuffledOffsets.subList(targetSize, shuffledOffsets.size());	
	}

	@Override
	public boolean isApplicableForTraining() {
		return true;
	}

	@Override
	public boolean isApplicableForTesting() {
		return false;
	}
}