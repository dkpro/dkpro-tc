package de.tudarmstadt.ukp.dkpro.tc.api.features.meta;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

public class MetaCollectorConfiguration
{
    public final AnalysisEngineDescription descriptor;
    public final Map<String, String> storageOverrides = new HashMap<>();
    
    public MetaCollectorConfiguration(AnalysisEngineDescription aDescriptor)
    {
        descriptor = aDescriptor;
    }

    public MetaCollectorConfiguration(Class<? extends AnalysisComponent> aClass)
        throws ResourceInitializationException
    {
        descriptor = createEngineDescription(aClass);
    }

    public MetaCollectorConfiguration addStorageMapping(String aParameter, String aPreferredLocation)
    {
        storageOverrides.put(aParameter, aPreferredLocation);
        return this;
    }
}
