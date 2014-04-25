package de.tudarmstadt.ukp.dkpro.tc.api.features;

import java.util.List;
import java.util.TreeSet;

import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

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
    
    public void addInstance(Instance instance) throws TextClassificationException;
    
    public Instance getInstance(int i);
    
    public List<String> getUniqueOutcomes();
    
    public List<String> getOutcomes(int i);
    
    public TreeSet<String> getFeatureNames();

}
