package de.tudarmstadt.ukp.dkpro.tc.api.features;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class InstanceList
{
    private List<Instance> instanceList;
    private List<String> outcomeList;

    public InstanceList() {
        this.instanceList = new ArrayList<Instance>();
        this.outcomeList = new ArrayList<String>();
    }
    
    public InstanceList(List<Instance> instanceList)
    {
        super();
        this.instanceList = instanceList;
        
        for (Instance instance : instanceList) {
            outcomeList.add(instance.getOutcome());
        }
    }
    
    public void addInstance(Instance instance, String outcome)
    {
        this.instanceList.add(instance);
        this.outcomeList.add(outcome);
    }
    
    public Instance getInstance(int i) {
        return this.instanceList.get(i);
    }
    
    public String getOutcome(int i) {
        return this.outcomeList.get(i);
    }
    
    public List<String> getOutcomeList()
    {
        return outcomeList;
    }
    
    public List<String> getUniqueOutcomes()
    {
        return new ArrayList<String>(new HashSet<String>(outcomeList));
    }

    public void setOutcomeList(List<String> outcomeList)
    {
        this.outcomeList = outcomeList;
    }

    public List<Instance> getInstanceList()
    {
        return instanceList;
    }

    public void setInstanceList(List<Instance> instanceList)
    {
        this.instanceList = instanceList;
    }
    
    public int size() {
        return this.getInstanceList().size();
    }
}