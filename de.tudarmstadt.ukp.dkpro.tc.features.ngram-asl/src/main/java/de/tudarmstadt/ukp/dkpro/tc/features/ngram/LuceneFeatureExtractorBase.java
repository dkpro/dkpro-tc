package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.io.File;

import org.apache.uima.fit.descriptor.ConfigurationParameter;

public abstract class LuceneFeatureExtractorBase
    extends NGramFeatureExtractorBase
{
    public static final String PARAM_LUCENE_DIR = "luceneDir";
    @ConfigurationParameter(name = PARAM_LUCENE_DIR, mandatory = true)
    protected File luceneDir;
    
}