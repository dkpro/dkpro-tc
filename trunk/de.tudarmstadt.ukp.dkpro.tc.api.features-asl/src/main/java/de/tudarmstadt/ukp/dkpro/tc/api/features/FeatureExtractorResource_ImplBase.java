package de.tudarmstadt.ukp.dkpro.tc.api.features;

import java.util.Map;

import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

public abstract class FeatureExtractorResource_ImplBase
    extends Resource_ImplBase
{

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        return super.initialize(aSpecifier, aAdditionalParams);
    }
}