package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramUtils;

public class NGramMetaCollector
    extends FreqDistBasedMetaCollector
{
    public static final String NGRAM_FD_KEY = "ngrams.ser";

    @ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_FD_FILE, mandatory = true)
    private File ngramFdFile;

    @ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int ngramMinN;

    @ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int ngramMaxN;

    @ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_STOPWORDS_FILE, mandatory = false)
    private String ngramStopwordsFile;

    @ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_LOWER_CASE, mandatory = false)
    private boolean ngramLowerCase = true;

    private  Set<String> stopwords;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        
        stopwords = new HashSet<String>();

        if (ngramStopwordsFile != null && !ngramStopwordsFile.isEmpty()) {
            try {
                URL stopUrl = ResourceUtils.resolveLocation(ngramStopwordsFile, null);
                InputStream is = stopUrl.openStream();
                stopwords.addAll(IOUtils.readLines(is, "UTF-8"));
            }
            catch (Exception e) {
                throw new ResourceInitializationException(e);
            }
        }
    }
    
    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        FrequencyDistribution<String> documentNGrams = NGramUtils.getDocumentNgrams(
                jcas, ngramLowerCase, ngramMinN, ngramMaxN, stopwords);      

        for (String ngram : documentNGrams.getKeys()) {
            fd.addSample(ngram, documentNGrams.getCount(ngram));
        }
    }

    @Override
    public Map<String, String> getParameterKeyPairs()
    {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put(NGramFeatureExtractor.PARAM_NGRAM_FD_FILE, NGRAM_FD_KEY);
        return mapping;
    }

    @Override
    protected File getFreqDistFile()
    {
        return ngramFdFile;
    }
}