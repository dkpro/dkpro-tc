package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.core.meta.MetaCollector;

public abstract class FreqDistBasedMetaCollector
    extends MetaCollector
{

    public static final String PARAM_LOWER_CASE = "lowerCase";
    @ConfigurationParameter(name = PARAM_LOWER_CASE, mandatory = true, defaultValue="true")
    protected boolean lowerCase;
    
    protected FrequencyDistribution<String> fd;
    
    protected File fdFile;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
                
        fd = new FrequencyDistribution<String>();
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();
        
        try {
            fd.save(fdFile);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

}