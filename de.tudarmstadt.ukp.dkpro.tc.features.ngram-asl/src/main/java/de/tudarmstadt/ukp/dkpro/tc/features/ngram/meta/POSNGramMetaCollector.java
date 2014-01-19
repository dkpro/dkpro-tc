package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.POSNGramFeatureExtractor;

public class POSNGramMetaCollector
    extends FreqDistBasedMetaCollector
{
    public static final String POS_NGRAM_FD_KEY = "posngrams.ser";

    @ConfigurationParameter(name = POSNGramFeatureExtractor.PARAM_POS_NGRAM_FD_FILE, mandatory = true)
    private File posNgramFdFile;

    @ConfigurationParameter(name = POSNGramFeatureExtractor.PARAM_POS_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int posNgramMinN;

    @ConfigurationParameter(name = POSNGramFeatureExtractor.PARAM_POS_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int posNgramMaxN;

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        FrequencyDistribution<String> documentPOSNGrams = NGramUtils.getDocumentPOSNgrams(jcas,
                posNgramMinN, posNgramMaxN);
        for (String ngram : documentPOSNGrams.getKeys()) {
            fd.addSample(ngram, documentPOSNGrams.getCount(ngram));
        }
    }

    @Override
    public Map<String, String> getParameterKeyPairs()
    {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put(POSNGramFeatureExtractor.PARAM_POS_NGRAM_FD_FILE, POS_NGRAM_FD_KEY);
        return mapping;
    }

    @Override
    protected File getFreqDistFile()
    {
        return posNgramFdFile;
    }
}