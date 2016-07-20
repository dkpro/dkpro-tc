package org.dkpro.tc.core.task;

import java.util.Map;

import org.dkpro.lab.task.Discriminable;

public abstract class TcFeature<T> implements Discriminable
{
    private String name;
    protected Map<String, Object> config;
    
    public TcFeature(String aName)
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
}