package de.tudarmstadt.ukp.dkpro.tc.fstore.simple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private List<String> featureNames;

    public SimpleFeatureStore() {
        this.instanceList = new ArrayList<List<Object>>();
        this.outcomeList = new ArrayList<List<String>>();
        this.featureNames = null;
    }
    
    @Override
    public void addInstance(Instance instance)
    {
        if (featureNames == null) {
            featureNames = new ArrayList<String>();
            for (Feature feature : instance.getFeatures()) {
                featureNames.add(feature.getName());
            }
        }
        
        List<Object> values = new ArrayList<Object>();
        for (Feature feature : instance.getFeatures()) {
            values.add(feature.getValue());
        }
        this.instanceList.add(values);
        this.outcomeList.add(instance.getOutcomes());
    }
    
    @Override
    public Instance getInstance(int i) {
        List<Feature> features = new ArrayList<Feature>();
        
        int offset = 0;
        for (Object value : instanceList.get(i)) {
            Feature feature = new Feature(featureNames.get(offset), value);
            features.add(feature);
            offset++;
        }
        return new Instance(features, outcomeList.get(i));
    }
    
    @Override
    public String getOutcome(int i) {
        if (this.outcomeList.get(i).size() > 0) {
            return this.outcomeList.get(i).get(0);
        }
        else {
            return null;
        }
    }
    
    public List<String> getOutcomes(int i) {
        return this.outcomeList.get(i);
    }
    
    public List<List<String>> getOutcomeLists()
    {
        return outcomeList;
    }
    
    @Override
    public List<String> getUniqueOutcomes()
    {
        Set<String> uniqueOutcomes = new HashSet<String>();
        for (List<String> outcomes : outcomeList) {
            uniqueOutcomes.addAll(outcomes);
        }
        return new ArrayList<String>(uniqueOutcomes);
    }

    public void setOutcomeList(List<List<String>> outcomeList)
    {
        this.outcomeList = outcomeList;
    }
    
    @Override
    public int size() {
        return getNumberOfInstances();
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

    public List<String> getFeatureNames()
    {
        return featureNames;
    }   
}