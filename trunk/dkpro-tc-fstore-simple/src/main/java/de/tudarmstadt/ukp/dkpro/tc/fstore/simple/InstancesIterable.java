package de.tudarmstadt.ukp.dkpro.tc.fstore.simple;

import java.util.Iterator;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;

/**
 * An iterable over TC instances
 */
public class InstancesIterable
    implements Iterable<Instance>
{
    private FeatureStore featureStore;
    private int instanceCounter;
    
    /**
     * Creates a new instance iterable from a feature store
     * 
     * @param featureStore
     */
    public InstancesIterable(SimpleFeatureStore featureStore) {
        this.featureStore = featureStore;
        instanceCounter = 0;
    }

    @Override
    public Iterator<Instance> iterator()
    {
        return new InstanceIterator();
    }  
    
    private class InstanceIterator
        implements Iterator<Instance>
    {
       
        @Override
        public boolean hasNext()
        {
            return instanceCounter < featureStore.size();
        }
    
        @Override
        public Instance next()
        {
            Instance instance = featureStore.getInstance(instanceCounter);
            instanceCounter++;
            return instance;
        }
    
        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}