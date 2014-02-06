package de.tudarmstadt.ukp.dkpro.tc.api.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Internal representation of an instance.
 * 
 * @author zesch
 *
 */
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
        this.outcomes = new ArrayList<String>();
        this.outcomes.add(outcome);
    }
    
    public Instance(List<Feature> features, String ... outcomes)
    {
        super();
        this.features = features;
        this.outcomes = Arrays.asList(outcomes);
    }

    public Instance(List<Feature> features, List<String> outcomes)
    {
        super();
        this.features = features;
        this.outcomes = outcomes;
    }

    public void addFeature(Feature feature)
    {
        this.features.add(feature);
    }
    
    public void addFeatures(List<Feature> features)
    {
        this.features.addAll(features);
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
        return this.features;
    }

    public void setFeatures(List<Feature> features)
    {
        this.features = features;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Feature feature : getFeatures()) {
            sb.append(feature);
            sb.append("\n");
        }
        sb.append(StringUtils.join(outcomes, "-"));
        return sb.toString();
    }   
}