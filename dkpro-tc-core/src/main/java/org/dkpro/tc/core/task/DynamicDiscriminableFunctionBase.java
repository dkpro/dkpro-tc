package org.dkpro.tc.core.task;

import java.util.Map;

import org.dkpro.lab.task.Discriminable;

public abstract class DynamicDiscriminableFunctionBase<T> implements Discriminable
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
    public abstract T getActualValue();

    
//    public abstract T getActualValue(TaskContext aContext);
}