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
package org.dkpro.tc.api.features;

import org.dkpro.tc.api.exception.TextClassificationException;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Interface for data structures that stores extracted features
 */
public interface FeatureStore
{

    public int getNumberOfInstances();

    public Iterable<Instance> getInstances();

    public void addInstance(Instance instance)
            throws TextClassificationException;

    public Instance getInstance(int i);

    public void deleteInstance(int i);

    public SortedSet<String> getUniqueOutcomes();

    public List<String> getOutcomes(int i);

    public Double getWeight(int i);

    /**
     * @return An ordered set of all feature names recorded in the store
     */
    public TreeSet<String> getFeatureNames();

    /**
     * Returns true, if feature names can be injected to the feature store by
     * calling {@link #setFeatureNames(java.util.TreeSet)}, false otherwise.
     * This might be used for sparse feature stores that creates their feature
     * space from features observed during training.
     *
     * @return boolean value
     */
    public boolean isSettingFeatureNamesAllowed();

    /**
     * Sets the feature names. If {@link #isSettingFeatureNamesAllowed()} returns false,
     * throws an exception, otherwise injects the feature names to the store
     *
     * @param featureNames feature names
     * @throws java.lang.IllegalStateException if setting feature names is not allowed
     */
    public void setFeatureNames(TreeSet<String> featureNames)
            throws IllegalStateException;

}
