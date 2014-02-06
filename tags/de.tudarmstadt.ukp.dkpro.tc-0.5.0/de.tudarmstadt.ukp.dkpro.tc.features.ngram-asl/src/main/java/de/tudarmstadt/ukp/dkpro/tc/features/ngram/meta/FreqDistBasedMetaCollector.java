package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;

public abstract class FreqDistBasedMetaCollector
    extends MetaCollector
{

    protected FrequencyDistribution<String> fd;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        fd = new FrequencyDistribution<String>();
    }

    /**
     * @return The path where the lucene index should be stored for this meta collector.
     */
    protected abstract File getFreqDistFile();
    
    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        try {
            fd.save(getFreqDistFile());
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

}