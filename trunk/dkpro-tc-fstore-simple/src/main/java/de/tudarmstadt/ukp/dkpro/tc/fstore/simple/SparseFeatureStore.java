/*
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
 */

package de.tudarmstadt.ukp.dkpro.tc.fstore.simple;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Feature store that internally uses hash maps instead of arrays to store sparse features.
 * All instances retrieved from this FeatureStore by {@link #getInstance(int)} have the same
 * number and ordering of features, but some features might have {@code null} value.
 *
 * @author Ivan Habernal
 */
public class SparseFeatureStore
        implements FeatureStore
{
    static Logger log = Logger.getLogger(SparseFeatureStore.class);

    private List<Map<String, Object>> instanceList = new ArrayList<>();
    private List<List<String>> outcomeList = new ArrayList<>();
    private List<Integer> sequenceIds = new ArrayList<>();
    private List<Integer> sequencePositions = new ArrayList<>();

    /**
     * If this flag is set to false, it is not possible to add another instances; this is set
     * after first calling {@linkplain #getInstance(int)}. Adding another instance might then
     * introduce new feature and thus make feature vectors of retrieved instances inconsistent.
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

    /**
     * Checks validity of this instance and if two features have the same name, throws an
     * exception
     *
     * @param instance instance
     * @throws TextClassificationException if instance contains duplicate features
     */
    private static void checkDuplicateFeatures(Instance instance)
            throws TextClassificationException
    {
        Set<String> featureNamesOfCurrentInstance = new HashSet<>();

        for (Feature feature : instance.getFeatures()) {
            String featureName = feature.getName();

            if (featureNamesOfCurrentInstance.contains(featureName)) {
                throw new TextClassificationException("Feature with name '" + featureName
                        + "' is defined in multiple times in one instance.");
            }

            featureNamesOfCurrentInstance.add(featureName);
        }
    }

    @Override
    public void addInstance(Instance instance)
            throws TextClassificationException
    {
        // check consistency of feature vectors
        if (!this.addingAnotherInstancesAllowed) {
            throw new TextClassificationException("Not allowed to add another instance to the " +
                    "feature store; getInstance() has been called already.");
        }

        // check for duplicate features
        checkDuplicateFeatures(instance);

        // adds all features to the global feature mapping
        for (Feature feature : instance.getFeatures()) {
            this.allFeatureNames.add(feature.getName());
        }

        // convert from List<Feature> to Map<name, value>
        Map<String, Object> currentInstanceFeatures = new HashMap<>();
        for (Feature feature : instance.getFeatures()) {
            currentInstanceFeatures.put(feature.getName(), feature.getValue());

            // increase statistics
            totalNonNullFeaturesCount++;
        }

        this.instanceList.add(currentInstanceFeatures);
        this.outcomeList.add(instance.getOutcomes());
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
        // set flag to disable adding new instances
        this.addingAnotherInstancesAllowed = false;

        List<Feature> features = new ArrayList<>();

        // feature values of the required instance (mapping featureName: featureValue)
        Map<String, Object> instanceFeatureValues = instanceList.get(i);

        for (String featureName : getFeatureNames()) {
            // create default null-valued feature
            Feature feature = new Feature(featureName, null);

            // if the feature is present in the current instance, set the correct value
            if (instanceFeatureValues.containsKey(featureName)) {
                feature.setValue(instanceFeatureValues.get(featureName));
            }

            features.add(feature);
        }

        Instance result = new Instance(features, outcomeList.get(i));
        result.setSequenceId(sequenceIds.get(i));
        result.setSequencePosition(sequencePositions.get(i));

        return result;
    }

    /**
     * A much faster access to large sparse feature vectors. This methods returns instance with
     * feature vector that contains only features with non-null values.
     *
     * @param i instance id
     * @return instance
     */
    public Instance getInstanceSparseFeatures(int i)
    {
        // set flag to disable adding new instances
        this.addingAnotherInstancesAllowed = false;

        List<Feature> features = new ArrayList<>();

        // feature values of the required instance (mapping feature mame: featureValue)
        Map<String, Object> instanceFeatureValues = instanceList.get(i);

        for (Map.Entry<String, Object> entry : instanceFeatureValues.entrySet()) {
            Feature feature = new Feature(entry.getKey(), entry.getValue());

            features.add(feature);
        }

        Instance result = new Instance(features, outcomeList.get(i));
        result.setSequenceId(sequenceIds.get(i));
        result.setSequencePosition(sequencePositions.get(i));

        return result;
    }

    @Override
    public SortedSet<String> getUniqueOutcomes()
    {
        SortedSet<String> result = new TreeSet<>();

        for (List<String> outcomes : outcomeList) {
            result.addAll(outcomes);
        }

        return result;
    }

    @Override
    public List<String> getOutcomes(int i)
    {
        return this.outcomeList.get(i);
    }

    @Override
    public TreeSet<String> getFeatureNames()
    {
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
        return "SparseFeatureStore{" +
                "instanceList=" + instanceList +
                ", outcomeList=" + outcomeList +
                ", sequenceIds=" + sequenceIds +
                ", sequencePositions=" + sequencePositions +
                ", addingAnotherInstancesAllowed=" + addingAnotherInstancesAllowed +
                ", totalNonNullFeaturesCount=" + totalNonNullFeaturesCount +
                '}';
    }

	@Override
	public void deleteInstance(int i) {
		instanceList.remove(i);
	}
}
