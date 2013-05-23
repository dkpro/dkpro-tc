package de.tudarmstadt.ukp.dkpro.tc.api.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Instance
{
    private List<Feature> features;
    private List<String> outcomes;
    
    public Instance() {
        this.features = new ArrayList<Feature>();
        this.outcomes = new ArrayList<String>();
    }
    
    public Instance(List<Feature> features, String outcome)
    {
        super();
        this.features = features;
        this.outcomes.add(outcome);
    }
    
    public Instance(List<Feature> features, String ... outcomes)
    {
        super();
        this.features = features;
        this.outcomes.addAll(Arrays.asList(outcomes));
    }

    public Instance(List<Feature> features, List<String> outcomes)
    {
        super();
        this.features = features;
        this.outcomes.addAll(outcomes);
    }

    public void addFeature(Feature feature)
    {
        features.add(feature);
    }
    
    public void addFeatures(List<Feature> features)
    {
        features.addAll(features);
    }
    
    public String getOutcome()
    {
        if (outcomes.size() > 0) {
            return outcomes.get(0);
        }
        else {
            return null;
        }      
    }
    
    public List<String> getOutcomes() {
        return this.outcomes;
    }

    public void setOutcomes(List<String> outcomes)
    {
        this.outcomes.clear();
        this.outcomes.addAll(outcomes);
    }
    
    public void setOutcomes(String ... outcomes)
    {
        this.outcomes.clear();
        this.outcomes.addAll(Arrays.asList(outcomes));
    }

    public List<Feature> getFeatures()
    {
        return features;
    }

    public void setFeatures(List<Feature> features)
    {
        this.features = features;
    }
}
