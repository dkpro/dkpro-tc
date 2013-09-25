package de.tudarmstadt.ukp.dkpro.tc.api.features.meta;

import java.util.List;


/**
 * Feature extractors that depend on {@link MetaCollector}s should implemnt this interface.
 * By doing so they declare what kind of {@link MetaCollector}s are used in the MetaInfoTask.
 * 
 * @author zesch
 *
 */
public interface MetaDependent
{
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses();
}
