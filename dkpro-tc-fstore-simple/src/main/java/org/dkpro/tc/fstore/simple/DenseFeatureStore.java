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
package org.dkpro.tc.fstore.simple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.api.features.Instance;

/**
 * Data structure that holds instances.
 * 
 */
public class DenseFeatureStore
    implements FeatureStore
{
	private List<Instance> instanceList;
    private TreeSet<String> featureNames;

    /**
     * Creates an empty feature store
     */
    public DenseFeatureStore()
    {
    	this.instanceList = new ArrayList<Instance>();
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
                            + "' is defined multiple times.");
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
        
        instanceList.add(instance);

    }

    @Override
    public Instance getInstance(int i) 
    {
    	return instanceList.get(i);
    }

    @Override
    public List<String> getOutcomes(int i)
    {
        return instanceList.get(i).getOutcomes();
    }
    
    @Override
    public Double getWeight(int i)
    {
        return instanceList.get(i).getWeight();
    }

    @Override
    public SortedSet<String> getUniqueOutcomes()
    {
        SortedSet<String> uniqueOutcomes = new TreeSet<String>();
        for(Instance ins : instanceList){
            uniqueOutcomes.addAll(ins.getOutcomes());
        }
        return uniqueOutcomes;
    }

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
                '}';
    }

	@Override
	public void deleteInstance(int i) {
		instanceList.remove(i);
	}

    @Override
    public boolean isSettingFeatureNamesAllowed()
    {
        return false;
    }

    @Override
    public void setFeatureNames(TreeSet<String> featureNames)
    {
        throw new IllegalStateException("Method not allowed in this feature store");
    }
}