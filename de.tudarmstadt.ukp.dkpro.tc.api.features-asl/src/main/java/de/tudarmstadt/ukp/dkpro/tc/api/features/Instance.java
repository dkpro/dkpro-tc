package de.tudarmstadt.ukp.dkpro.tc.api.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.dkpro.tc.api.features.IFeature;

/**
 * Internal representation of an instance.
 * 
 * @author zesch
 *
 */
public class Instance
{
    private List<IFeature> features;
    private List<String> outcomes;
    
    public Instance() {
        this.features = new ArrayList<IFeature>();
        this.outcomes = new ArrayList<String>();
    }
    
    public Instance(List<IFeature> features, String outcome)
    {
        super();
        this.features = features;
        this.outcomes = new ArrayList<String>();
        this.outcomes.add(outcome);
    }
    
    public Instance(List<IFeature> features, String ... outcomes)
    {
        super();
        this.features = features;
        this.outcomes = Arrays.asList(outcomes);
    }

    public Instance(List<IFeature> features, List<String> outcomes)
    {
        super();
        this.features = features;
        this.outcomes = outcomes;
    }

    public void addFeature(IFeature feature)
    {
        this.features.add(feature);
    }
    
    public void addFeatures(List<IFeature> features)
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

    public List<IFeature> getFeatures()
    {
        return this.features;
    }

    public void setFeatures(List<IFeature> features)
    {
        this.features = features;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (IFeature feature : getFeatures()) {
            sb.append(feature);
            sb.append("\n");
        }
        sb.append(StringUtils.join(outcomes, "-"));
        return sb.toString();
    }   
}