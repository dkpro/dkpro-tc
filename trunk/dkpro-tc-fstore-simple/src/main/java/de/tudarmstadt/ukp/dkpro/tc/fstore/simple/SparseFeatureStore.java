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

import java.util.*;

/**
 * Feature store that internally uses hash maps instead of arrays to store sparse features.
 *
 * @author Ivan Habernal
 */
public class SparseFeatureStore
        implements FeatureStore
{
    /**
     * Feature value for non-present features
     */
    private static final Integer DEFAULT_NON_PRESENT_FEATURE_VALUE = 0;

    private List<Map<Integer, Object>> instanceList = new ArrayList<>();
    private List<List<String>> outcomeList = new ArrayList<>();
    private List<Integer> sequenceIds = new ArrayList<>();
    private List<Integer> sequencePositions = new ArrayList<>();
    private SortedMap<String, Integer> featureNameToFeatureIdMapping = new TreeMap<>();

    @Override
    public int size()
    {
        return getNumberOfInstances();
    }

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
        // check for duplicate features
        checkDuplicateFeatures(instance);

        // adds all features to the global feature mapping
        for (Feature feature : instance.getFeatures()) {
            int nextId = this.featureNameToFeatureIdMapping.size();
            this.featureNameToFeatureIdMapping.put(feature.getName(), nextId);
        }

        // transform features of the current instance to a map: featureId=featureValue
        Map<Integer, Object> currentInstanceFeatures = new HashMap<>();
        for (Feature feature : instance.getFeatures()) {
            // feature id
            Integer featureId = this.featureNameToFeatureIdMapping.get(feature.getName());
            currentInstanceFeatures.put(featureId, feature.getValue());
        }

        this.instanceList.add(currentInstanceFeatures);
        this.outcomeList.add(instance.getOutcomes());
        this.sequenceIds.add(instance.getSequenceId());
        this.sequencePositions.add(instance.getSequencePosition());
    }

    @Override
    public Instance getInstance(int i)
    {
        List<Feature> features = new ArrayList<>();

        // feature values of the required instance (mapping featureID: featureValue)
        Map<Integer, Object> instanceFeatureValues = instanceList.get(i);

        for (String featureName : getFeatureNames()) {
            Integer featureId = this.featureNameToFeatureIdMapping.get(featureName);

            // create default zero-valued feature
            Feature feature = new Feature(featureName, DEFAULT_NON_PRESENT_FEATURE_VALUE);

            // if the feature is present in the current instance, set the correct value
            if (instanceFeatureValues.containsKey(featureId)) {
                feature.setValue(instanceFeatureValues.get(featureId));
            }

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

    @Override public TreeSet<String> getFeatureNames()
    {
        return new TreeSet<>(this.featureNameToFeatureIdMapping.keySet());
    }
}
