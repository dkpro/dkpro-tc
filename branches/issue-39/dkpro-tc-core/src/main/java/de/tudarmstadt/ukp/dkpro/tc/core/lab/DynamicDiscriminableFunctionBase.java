package de.tudarmstadt.ukp.dkpro.tc.core.lab;

import java.util.Map;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminable;

public abstract class DynamicDiscriminableFunctionBase implements Discriminable
{
    private String name;
    protected Map<String, Object> config;
    
    public DynamicDiscriminableFunctionBase(String aName)
    {
        name = aName;
    }

    public void setConfig(Map<String, Object> aConfig) {
        config = aConfig;
    }
    
    @Override
    public Object getDiscriminatorValue()
    {
        return name;
    }
    
    @Override
    public Object getActualValue()
    {
        throw new UnsupportedOperationException("buuuh!");
    }
    
    public abstract Object getActualValue(TaskContext aContext);
}