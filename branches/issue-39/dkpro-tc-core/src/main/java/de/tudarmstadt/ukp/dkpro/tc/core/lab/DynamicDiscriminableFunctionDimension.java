package de.tudarmstadt.ukp.dkpro.tc.core.lab;

import java.util.Map;

import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.DynamicDimension;

public class DynamicDiscriminableFunctionDimension<T>
    extends Dimension<DynamicDiscriminableFunctionBase>
    implements DynamicDimension
{
    private DynamicDiscriminableFunctionBase[] closures;
    private int current = -1;
    private Map<String, Object> config;

    public DynamicDiscriminableFunctionDimension(String aName, DynamicDiscriminableFunctionBase... aClosures)
    {
        super(aName);
        closures = aClosures;
    }
    
    @Override
    public void setConfiguration(Map<String, Object> aConfig)
    {
        config = aConfig;
    }

    @Override
    public boolean hasNext()
    {
        return current+1 < closures.length;
    }

    @Override
    public DynamicDiscriminableFunctionBase next()
    {
        current++;
        return current();
    }

    @Override
    public DynamicDiscriminableFunctionBase current()
    {
        // When calling next() after rewind() to position current() at the first
        // dimension value, no config has been set yet. At this point just
        // do nothing.
        if (config == null) {
            return null;
        }
        
        closures[current].setConfig(config);
        return closures[current];
    }

    @Override
    public void rewind()
    {
        current = -1;
    }

    @Override
    public String toString()
    {
        return "[" + getName() + ": " + (current >= 0 && current < closures.length ? current() : "?") + "]";
    }
}