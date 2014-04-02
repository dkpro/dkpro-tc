package de.tudarmstadt.ukp.dkpro.tc.api.features;

import java.util.List;

public interface FeatureStore
{

    /**
     * @return The number of instances in the store.
     */
    public int size();
    
    /**
     * @return The number of instances in the store.
     */
    public int getNumberOfInstances();
    
    public Iterable<Instance> getInstances();
    
    public void addInstance(Instance instance);
    
    public Instance getInstance(int i);
    
    public List<String> getUniqueOutcomes();
    
    public List<String> getOutcomes(int i);
    
    public List<String> getFeatureNames();

}
