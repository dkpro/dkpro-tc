package org.cleartk.classifier.weka.test;

import java.util.ArrayList;
import java.util.List;

public class Instance
{
    private List<Feature> features;
    private String outcome;
    
    public Instance() {
        this.features = new ArrayList<Feature>();
        this.outcome = "";
    }
    
    public Instance(List<Feature> features, String outcome)
    {
        super();
        this.features = features;
        this.outcome = outcome;
    }

    public void addFeature(Feature feature)
    {
        features.add(feature);
    }
    
    public String getOutcome()
    {
        return outcome;
    }

    public void setOutcome(String outcome)
    {
        this.outcome = outcome;
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
