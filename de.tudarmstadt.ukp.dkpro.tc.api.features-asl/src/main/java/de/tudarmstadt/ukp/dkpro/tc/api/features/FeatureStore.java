package de.tudarmstadt.ukp.dkpro.tc.api.features;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;

/**
 * Interface for data structures that stores extracted features
 */
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
    
    
    /**
     * @return An iterable over all stored instances
     */
    public Iterable<Instance> getInstances();
    
    /**
     * Adds an instance
     * 
     * @param instance
     * @throws TextClassificationException
     */
    public void addInstance(Instance instance) throws TextClassificationException;
    
    /**
     * 
     * @param i
     * @return The i-th instance in the store
     */
    public Instance getInstance(int i);
    
    /**
     * @return A set of unique classification outcomes from all stored instances
     */
    public TreeSet<String> getUniqueOutcomes();
    
    /**
     * Always returns a list of outcomes, even for single label classification where only the first element of the list will be filled.
     * @param i
     * @return The outcomes of the i-th instance in the store
     */
    public List<String> getOutcomes(int i);
    
    /**
     * 
     * @return An ordered set of all feature names recorded in the store
     */
    public TreeSet<String> getFeatureNames();

}
