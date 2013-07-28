package de.tudarmstadt.ukp.dkpro.tc.api.features;

import java.util.List;

public interface MetaDependent
{

    public List<Class<? extends MetaCollector>> getMetaCollectorClasses();
    
}
