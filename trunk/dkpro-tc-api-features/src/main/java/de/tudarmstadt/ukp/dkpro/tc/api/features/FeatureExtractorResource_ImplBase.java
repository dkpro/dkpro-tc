package de.tudarmstadt.ukp.dkpro.tc.api.features;

import java.util.Map;

import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.internal.ReflectionUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

/**
 * Abstract base class for all feature extractors.
 * 
 * Feature extractors are implemented as UIMA external resources.
 * 
 * @author zesch
 *
 */
public abstract class FeatureExtractorResource_ImplBase
    extends Resource_ImplBase
{
    
    protected String[] requiredTypes;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
                
        TypeCapability annotation = ReflectionUtil.getAnnotation(this.getClass(), TypeCapability.class);

        if (annotation != null) {
            requiredTypes = annotation.inputs();
        }
        else {
            requiredTypes = new String[0];
        }

        return true;
    }
}