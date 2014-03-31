package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureUtil;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.FrequencyDistributionNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.NGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.NGramUtils;

public class NGramMetaCollector
    extends FreqDistBasedMetaCollector
{
    public static final String NGRAM_FD_KEY = "ngrams.ser";

    @ConfigurationParameter(name = FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_FD_FILE, mandatory = true)
    private File ngramFdFile;

    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int ngramMinN;

    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int ngramMaxN;

    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_STOPWORDS_FILE, mandatory = false)
    private String ngramStopwordsFile;
    
    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_FILTER_PARTIAL_STOPWORD_MATCHES, mandatory = true, defaultValue="false")
    private boolean filterPartialStopwordMatches;

    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_LOWER_CASE, mandatory = true, defaultValue = "true")
    private boolean ngramLowerCase;

    private  Set<String> stopwords;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        
        try {
            stopwords = FeatureUtil.getStopwords(ngramStopwordsFile, ngramLowerCase);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }
    
    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        FrequencyDistribution<String> documentNGrams = NGramUtils.getDocumentNgrams(
                jcas, ngramLowerCase, filterPartialStopwordMatches, ngramMinN, ngramMaxN, stopwords);      

        for (String ngram : documentNGrams.getKeys()) {
            fd.addSample(ngram, documentNGrams.getCount(ngram));
        }
    }

    @Override
    public Map<String, String> getParameterKeyPairs()
    {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put(FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_FD_FILE, NGRAM_FD_KEY);
        return mapping;
    }

    @Override
    protected File getFreqDistFile()
    {
        return ngramFdFile;
    }
}