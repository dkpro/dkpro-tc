package de.tudarmstadt.ukp.dkpro.tc.api.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.LogFactory;

public class UniqueInstanceFeatures
{

    private List<Feature> uniqueFeatures;
    private Set<String> seenFeatureNames;
    
    public void addFeature(Feature feature)
    {

        String name = feature.getName();
        if (seenFeatureNames.contains(name)) {
            LogFactory.getLog(getClass()).warn(
                    "The feature with the name [" + name
                            + "] has been added more than once - dropped duplicate!");
            return;
        }

        seenFeatureNames.add(name);
        uniqueFeatures.add(feature);

    }
    

    public UniqueInstanceFeatures(Collection<Feature> features)
    {
        uniqueFeatures = new ArrayList<Feature>();
        seenFeatureNames = new TreeSet<String>();

        features.forEach(x -> addFeature(x));
    }

    public UniqueInstanceFeatures()
    {
        uniqueFeatures = new ArrayList<Feature>();
        seenFeatureNames = new TreeSet<String>();
    }


    public void addFeatures(Collection<Feature> features)
    {
        features.forEach(x -> addFeature(x));
    }

    public Collection<Feature> getFeatures()
    {
        return uniqueFeatures;
    }

    public void setFeatures(Collection<Feature> features)
    {
        uniqueFeatures = new ArrayList<Feature>();
        seenFeatureNames = new TreeSet<String>();
        
        features.forEach(x -> addFeature(x));
    }

}
