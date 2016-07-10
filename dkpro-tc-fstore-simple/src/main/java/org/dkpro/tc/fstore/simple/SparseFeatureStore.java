/*
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
 */

package org.dkpro.tc.fstore.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.api.features.Instance;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Feature store that internally uses hash maps instead of arrays to store sparse features. All
 * instances retrieved from this FeatureStore by {@link #getInstance(int)} have the same number and
 * ordering of features, but some features might have {@code null} value.
 *
 */
public class SparseFeatureStore
    implements FeatureStore
{
    static Logger log = Logger.getLogger(SparseFeatureStore.class);

    private ObjectArrayList<Map<String, Object>> instanceList = new ObjectArrayList<>();
    // private List<List<String>> outcomeList = new ArrayList<>();
    private ObjectArrayList<String[]> outcomeList = new ObjectArrayList<>();
    private DoubleArrayList weightList = new DoubleArrayList();
    private IntArrayList casIds = new IntArrayList();
    private IntArrayList sequenceIds = new IntArrayList();
    private IntArrayList sequencePositions = new IntArrayList();

    /**
     * If this flag is set to false, it is not possible to add another instances; this is set after
     * first calling {@linkplain #getInstance(int)}. Adding another instance might then introduce
     * new feature and thus make feature vectors of retrieved instances inconsistent.
     */
    private boolean addingAnotherInstancesAllowed = true;

    // statistics for feature sparsity
    int totalNonNullFeaturesCount = 0;

    // cached feature names
    private TreeSet<String> allFeatureNames = new TreeSet<>();

    @Override
    public int getNumberOfInstances()
    {
        return instanceList.size();
    }

    @Override
    public Iterable<Instance> getInstances()
    {
        return new InstancesIterable(this);
    }

    @Override
    public void addInstance(Instance instance)
        throws TextClassificationException
    {
        // check consistency of feature vectors
        if (!this.addingAnotherInstancesAllowed) {
            throw new TextClassificationException("Not allowed to add another instance to the "
                    + "feature store; getInstance() has been called already.");
        }

        // convert from List<Feature> to Map<name, value>
        Map<String, Object> currentInstanceFeatures = new HashMap<>();
        for (Feature feature : instance.getFeatures()) {
            String name = feature.getName();
            Object value = feature.getValue();

            // check for duplicate features
            if (currentInstanceFeatures.containsKey(name)) {
                throw new TextClassificationException("Feature with name '" + name
                        + "' is defined in multiple times in one instance.");
            }

            // add all feature to the global feature mapping
            this.allFeatureNames.add(name);

            if (value != null) {
                currentInstanceFeatures.put(name, value);

                // increase statistics
                totalNonNullFeaturesCount++;
            }
        }

        this.instanceList.add(currentInstanceFeatures);
        this.outcomeList.add(instance.getOutcomes().toArray(new String[0]));
        this.weightList.add(instance.getWeight());
        this.casIds.add(instance.getJcasId());
        this.sequenceIds.add(instance.getSequenceId());
        this.sequencePositions.add(instance.getSequencePosition());
    }

    /**
     * Returns a ratio (0-1) how many features were not-null among all instances
     *
     * @return double
     */
    public double getFeatureSparsityRatio()
    {
        // feature vector length
        int featureVectorLength = this.allFeatureNames.size();
        // matrix
        long matrixSize = featureVectorLength * this.instanceList.size();

        return ((double) this.totalNonNullFeaturesCount) / ((double) matrixSize);
    }

    @Override
    public Instance getInstance(int i)
    {
        this.addingAnotherInstancesAllowed = false;

        List<Feature> features = new ArrayList<>();

        // feature values of the required instance (mapping feature mame: featureValue)
        Map<String, Object> instanceFeatureValues = instanceList.get(i);

        for (Map.Entry<String, Object> entry : instanceFeatureValues.entrySet()) {
            Feature feature = new Feature(entry.getKey(), entry.getValue());

            features.add(feature);
        }

        Instance result = new Instance(features, outcomeList.get(i));
        result.setWeight(weightList.getDouble(i));
        result.setJcasId(casIds.getInt(i));
        result.setSequenceId(sequenceIds.getInt(i));
        result.setSequencePosition(sequencePositions.getInt(i));

        return result;
    }

    @Override
    public SortedSet<String> getUniqueOutcomes()
    {
        SortedSet<String> result = new TreeSet<>();

        for (String[] outcomes : outcomeList) {
            result.addAll(Arrays.asList(outcomes));
        }

        return result;
    }

    @Override
    public List<String> getOutcomes(int i)
    {
        return new ArrayList<String>(Arrays.asList(this.outcomeList.get(i)));
    }

    @Override
    public Double getWeight(int i)
    {
        return this.weightList.getDouble(i);
    }

    @Override
    public TreeSet<String> getFeatureNames()
    {
        log.debug("Returning " + this.allFeatureNames.size() + " features");
        return this.allFeatureNames;
    }

    /**
     * Primarily for debug purposes
     *
     * @return all instances, features, mapping, internal state, etc.
     */
    @Override
    public String toString()
    {
        return "SparseFeatureStore{" + "instanceList=" + instanceList + ", outcomeList="
                + outcomeList + ", weightList=" + weightList + ", sequenceIds=" + sequenceIds
                + ", sequencePositions=" + sequencePositions + ", addingAnotherInstancesAllowed="
                + addingAnotherInstancesAllowed + ", totalNonNullFeaturesCount="
                + totalNonNullFeaturesCount + '}';
    }

    @Override
    public void deleteInstance(int i)
    {
        instanceList.remove(i);
    }

    @Override
    public boolean isSettingFeatureNamesAllowed()
    {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setFeatureNames(TreeSet<String> featureNames)
    {
        if (featureNames == null) {
            throw new IllegalArgumentException("param featureNames is null");
        }

        if (featureNames.isEmpty()) {
            throw new IllegalStateException("Cannot set empty feature space");
        }

        if (!isSettingFeatureNamesAllowed()) {
            throw new IllegalStateException("Setting feature names is not allowed.");
        }

        Set<String> deletedFeatures = new HashSet<>(
                (Collection<String>) CollectionUtils.subtract(this.allFeatureNames, featureNames));

        log.debug(deletedFeatures.size() + " features from test data not seen in training data, "
                + "removing features from the store.");

        this.allFeatureNames = featureNames;

        // update all instances to they do not contain old features
        for (Map<String, Object> instance : this.instanceList) {
            for (String deletedFeature : deletedFeatures) {
                if (instance.containsKey(deletedFeature)) {
                    instance.remove(deletedFeature);
                }
            }
        }
    }

    @Override
    public boolean supportsSparseFeatures()
    {
        return true;
    }

}
