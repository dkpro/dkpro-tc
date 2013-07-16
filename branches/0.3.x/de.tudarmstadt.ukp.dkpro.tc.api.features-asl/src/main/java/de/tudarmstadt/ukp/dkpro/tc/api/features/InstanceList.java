package de.tudarmstadt.ukp.dkpro.tc.api.features;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InstanceList
{
    private List<Instance> instanceList;
    private List<List<String>> outcomeList;

    public InstanceList() {
        this.instanceList = new ArrayList<Instance>();
        this.outcomeList = new ArrayList<List<String>>();
    }
    
    public InstanceList(List<Instance> instanceList)
    {
        super();
        this.instanceList = instanceList;
        
        for (Instance instance : instanceList) {
            outcomeList.add(instance.getOutcomes());
        }
    }
    
    public void addInstance(Instance instance)
    {
        this.instanceList.add(instance);
        this.outcomeList.add(instance.getOutcomes());
    }
    
    public Instance getInstance(int i) {
        return this.instanceList.get(i);
    }
    
    public String getOutcome(int i) {
        if (this.outcomeList.get(i).size() > 0) {
            return this.outcomeList.get(i).get(0);
        }
        else {
            return null;
        }
    }
    
    public List<String> getOutcomes(int i) {
        return this.outcomeList.get(i);
    }
    
    public List<List<String>> getOutcomeLists()
    {
        return outcomeList;
    }
    
    public List<String> getUniqueOutcomes()
    {
        Set<String> uniqueOutcomes = new HashSet<String>();
        for (List<String> outcomes : outcomeList) {
            uniqueOutcomes.addAll(outcomes);
        }
        return new ArrayList<String>(uniqueOutcomes);
    }

    public void setOutcomeList(List<List<String>> outcomeList)
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