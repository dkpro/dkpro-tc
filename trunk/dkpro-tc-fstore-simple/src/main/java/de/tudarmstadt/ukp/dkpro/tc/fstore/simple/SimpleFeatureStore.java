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
package de.tudarmstadt.ukp.dkpro.tc.fstore.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;

/**
 * Data structure that holds instances.
 * 
 * @author zesch
 * 
 */
public class SimpleFeatureStore
    implements FeatureStore
{
    private List<List<Object>> instanceList;
    private List<List<String>> outcomeList;
    private List<Integer> sequenceIds;
    private List<Integer> sequencePositions;
    private TreeSet<String> featureNames;

    /**
     * Creates an empty feature store
     */
    public SimpleFeatureStore()
    {
        this.instanceList = new ArrayList<List<Object>>();
        this.outcomeList = new ArrayList<List<String>>();
        this.sequenceIds = new ArrayList<Integer>();
        this.sequencePositions = new ArrayList<Integer>();
        this.featureNames = null;
    }

    @Override
    public void addInstance(Instance instance)
        throws TextClassificationException
    {
        if (featureNames == null) {
            featureNames = new TreeSet<String>();
            for (Feature feature : instance.getFeatures()) {
                String name = feature.getName();
                if (featureNames.contains(name)) {
                    throw new TextClassificationException("Feature with name '" + name
                            + "' is defined in multiple times.");
                }
                featureNames.add(name);
            }
        }

        HashSet<String> instanceFeatureNames = new HashSet<String>();
        for (Feature f : instance.getFeatures()) {
            instanceFeatureNames.add(f.getName());
        }
        @SuppressWarnings("unchecked")
        String[] symDiff = new ArrayList<String>(CollectionUtils.disjunction(
                instanceFeatureNames,
                featureNames)).toArray(new String[] {});
        if (symDiff.length > 0) {
            throw new TextClassificationException(
                    "One or more, but not all of your instances return the following feature(s): "
                            + StringUtils.join(symDiff, " and "));
        }

        // create map of feature names and offset in set
        Map<String, Integer> sortedFeatureNameMap = new HashMap<String, Integer>();
        int offset = 0;
        Iterator<String> iterator = featureNames.iterator();
        while (iterator.hasNext()) {
            sortedFeatureNameMap.put(iterator.next(), offset);
            offset++;
        }

        Object[] values = new Object[featureNames.size()];
        for (Feature feature : instance.getFeatures()) {
            values[sortedFeatureNameMap.get(feature.getName())] = feature.getValue();
        }
        this.instanceList.add(Arrays.asList(values));
        this.outcomeList.add(instance.getOutcomes());
        this.sequenceIds.add(instance.getSequenceId());
        this.sequencePositions.add(instance.getSequencePosition());
    }

    @Override
    public Instance getInstance(int i)
    {
        List<Feature> features = new ArrayList<Feature>();

        List<Object> values = instanceList.get(i);

        int offset = 0;
        Iterator<String> sortedNames = getFeatureNames().iterator();
        while (sortedNames.hasNext()) {
            String name = sortedNames.next();
            Feature feature = new Feature(name, values.get(offset));
            features.add(feature);
            offset++;
        }

        Instance instance = new Instance(features, outcomeList.get(i));
        instance.setSequenceId(sequenceIds.get(i));
        instance.setSequencePosition(sequencePositions.get(i));
        return instance;
    }

    @Override
    public List<String> getOutcomes(int i)
    {
        return this.outcomeList.get(i);
    }

    // public List<List<String>> getOutcomeLists()
    // {
    // return outcomeList;
    // }

    @Override
    public SortedSet<String> getUniqueOutcomes()
    {
        SortedSet<String> uniqueOutcomes = new TreeSet<String>();
        for (List<String> outcomes : outcomeList) {
            uniqueOutcomes.addAll(outcomes);
        }
        return uniqueOutcomes;
    }

    // public void setOutcomeList(List<List<String>> outcomeList)
    // {
    // this.outcomeList = outcomeList;
    // }

    @Override
    public int getNumberOfInstances()
    {
        return this.instanceList.size();
    }

    @Override
    public Iterable<Instance> getInstances()
    {
        return new InstancesIterable(this);
    }

    @Override
    public TreeSet<String> getFeatureNames()
    {
        return featureNames;
    }

	@Override
	public void deleteInstance(int i) {
		instanceList.remove(i);
	}
}