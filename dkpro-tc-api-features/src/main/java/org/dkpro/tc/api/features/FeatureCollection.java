/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.api.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A feature set implementation that distinguishes internally features that have
 * a default value set and those that have an actual non-default value. This
 * enables a direct access to features with a non-default value, which speeds up
 * processing in case the classifier works with sparse features.
 */
public class FeatureCollection implements Iterable<Feature> {
	private static final String KEY_DEFAULT = "default";
	private static final String KEY_SET_VALUE = "setValue";

	private Map<String, List<Feature>> features;

	public FeatureCollection() {
		features = new HashMap<String, List<Feature>>();
		features.put(KEY_DEFAULT, new ArrayList<>(4096));
		features.put(KEY_SET_VALUE, new ArrayList<>(4096));
	}

	/**
	 * Adds a single feature instance to the feature set
	 * 
	 * @param f
	 *            the feature
	 */
	public void add(Feature f) {
		if (f.isDefaultValue()) {
			List<Feature> list = features.get(KEY_DEFAULT);
			list.add(f);
			features.put(KEY_DEFAULT, list);
			return;
		}
		List<Feature> list = features.get(KEY_SET_VALUE);
		list.add(f);
		features.put(KEY_SET_VALUE, list);
	}

	/**
	 * Adds a collection of features. Convenience methods, calls for each
	 * instance in the collection {@link #add(Feature)}
	 * 
	 * @param features
	 *            the features
	 */
	public void add(Collection<Feature> features) {
		features.forEach(x -> add(x));
	}

	/**
	 * Adds an array of features. Convenience methods, calls for each instance
	 * in the array {@link #add(Feature)}
	 * 
	 * @param features
	 *            the features
	 */
	public void add(Feature... features) {
		Arrays.asList(features).forEach(x -> add(x));
	}

	/**
	 * Selects only the features that have a value that is different to the
	 * default value of a feature. The default value is feature dependent. If
	 * one is unsure about the notion of default values, use the method
	 * {@link #getAllFeatures()}, which returns all
	 * instances of a feature set.
	 * 
	 * @return sub set of features with non-default value
	 */
	public Set<Feature> getNonDefaultFeatures() {
		return new HashSet<>(features.get(KEY_SET_VALUE));
	}

	/**
	 * Selects only the features that have the default value as feature. If one
	 * is unsure about the notion of default values, use the method
	 * {@link #getAllFeatures()}, which returns all
	 * instances of a feature set.
	 * 
	 * @return sub set of features with default values
	 */
	public Set<Feature> getDefaultFeatures() {
		return new HashSet<>(features.get(KEY_DEFAULT));
	}

	/**
	 * Returns all features in the feature set, the one which have only default
	 * values, i.e. are not set for an instance and those which have actual
	 * values for an instance.
	 * 
	 * @return all features
	 */
	public Set<Feature> getAllFeatures() {
		Set<Feature> f = new HashSet<>();
		f.addAll(features.get(KEY_DEFAULT));
		f.addAll(features.get(KEY_SET_VALUE));
		return f;
	}

	/**
	 * Returns the number of {@link Instance} in the feature set.
	 * 
	 * @return number of instances in this feature set
	 */
	public int size() {
		return features.get(KEY_DEFAULT).size() + features.get(KEY_SET_VALUE).size();
	}

	@Override
	public Iterator<Feature> iterator() {
		Set<Feature> feature = getAllFeatures();
		return feature.iterator();
	}
}