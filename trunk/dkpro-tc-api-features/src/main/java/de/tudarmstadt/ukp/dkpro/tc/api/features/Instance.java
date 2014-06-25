/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
    private int sequenceId;
    private int sequencePosition;
    
    /**
     * Create an empty instance
     */
    public Instance() {
        this.features = new ArrayList<Feature>();
        this.outcomes = new ArrayList<String>();
        this.sequenceId = 0;
        this.sequencePosition = 0;
    }
    
    /**
     * Create an instance
     * 
     * @param features
     * @param outcome
     */
    public Instance(List<Feature> features, String outcome)
    {
        super();
        this.features = features;
        this.outcomes = new ArrayList<String>();
        this.outcomes.add(outcome);
        this.sequenceId = 0;
        this.sequencePosition = 0;
    }
    
    /**
     * Create an instance
     * 
     * @param features
     * @param outcomes
     */
    public Instance(List<Feature> features, String ... outcomes)
    {
        super();
        this.features = features;
        this.outcomes = Arrays.asList(outcomes);
        this.sequenceId = 0;
        this.sequencePosition = 0;
    }

    /**
     * Create an instance
     * 
     * @param features
     * @param outcomes
     */
    public Instance(List<Feature> features, List<String> outcomes)
    {
        super();
        this.features = features;
        this.outcomes = outcomes;
        this.sequenceId = 0;
        this.sequencePosition = 0;
    }

    /**
     * Add a feature
     * 
     * @param feature
     */
    public void addFeature(Feature feature)
    {
        this.features.add(feature);
    }
    
    /**
     * Add a list of features
     * 
     * @param features
     */
    public void addFeatures(List<Feature> features)
    {
        this.features.addAll(features);
    }
    
    /**
     * Returns the first outcome if more than one outcome is stored, or null if no outcomes have been stored yet.
     * 
     * @return The outcome of this instance
     */
    public String getOutcome()
    {
        if (outcomes.size() > 0) {
            return outcomes.get(0);
        }
        else {
            return null;
        }      
    }
    
    /**
     * @return The list of outcomes for this instance
     */
    public List<String> getOutcomes() {
        return this.outcomes;
    }

    /**
     * Set the outcomes for this instance
     * 
     * @param outcomes
     */
    public void setOutcomes(List<String> outcomes)
    {
        this.outcomes.clear();
        this.outcomes.addAll(outcomes);
    }

    /**
     * Set the outcomes for this instance
     * 
     * @param outcomes
     */
    public void setOutcomes(String ... outcomes)
    {
        this.outcomes.clear();
        this.outcomes.addAll(Arrays.asList(outcomes));
    }

    /**
     * @return The list of features stored for this instance
     */
    public List<Feature> getFeatures()
    {
        return this.features;
    }

    /**
     * Set the list of features for this instance
     * 
     * @param features
     */
    public void setFeatures(List<Feature> features)
    {
        this.features = features;
    }

    /**
     * @return The id of the sequence this instance is part of. 0 if not part of any sequence.
     */
    public int getSequenceId()
    {
        return sequenceId;
    }

    /**
     * Sets the sequence id
     * 
     * @param sequenceId
     */
    public void setSequenceId(int sequenceId)
    {
        this.sequenceId = sequenceId;
    }   
    
    /**
     * @return The position within a certain sequence
     */
    public int getSequencePosition()
    {
        return sequencePosition;
    }

    /**
     * Sets the position with the current sequence (as defined by the sequence id)
     * 
     * @param sequencePosition
     */
    public void setSequencePosition(int sequencePosition)
    {
        this.sequencePosition = sequencePosition;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(sequenceId); sb.append(" - "); sb.append(sequencePosition);
        sb.append("\n");
        for (Feature feature : getFeatures()) {
            sb.append(feature);
            sb.append("\n");
        }
        sb.append(StringUtils.join(outcomes, "-"));
        return sb.toString();
    }
}