package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import java.io.IOException;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureUtil;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneSkipNgramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramUtils;

public class LuceneSkipNgramMetaCollector
    extends LuceneBasedMetaCollector
{    
    @ConfigurationParameter(name = LuceneSkipNgramFeatureExtractor.PARAM_SKIP_NGRAM_MIN_N, mandatory = true, defaultValue = "2")
    private int minN;

    @ConfigurationParameter(name = LuceneSkipNgramFeatureExtractor.PARAM_SKIP_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int maxN;

    @ConfigurationParameter(name = LuceneSkipNgramFeatureExtractor.PARAM_SKIP_N, mandatory = true, defaultValue = "2")
    private int skipN;
    
    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_STOPWORDS_FILE, mandatory = false)
    private String stopwordsFile;
    
    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_FILTER_PARTIAL_STOPWORD_MATCHES, mandatory = true, defaultValue="false")
    protected boolean filterPartialStopwordMatches;

    @ConfigurationParameter(name = LuceneSkipNgramFeatureExtractor.PARAM_SKIP_NGRAM_LOWER_CASE, mandatory = true, defaultValue = "true")
    private boolean ngramLowerCase;

    private Set<String> stopwords;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        
        try {
            stopwords = FeatureUtil.getStopwords(stopwordsFile, ngramLowerCase);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        FrequencyDistribution<String> documentNGrams = NGramUtils.getDocumentSkipNgrams(
                jcas, ngramLowerCase, filterPartialStopwordMatches, minN, maxN, skipN, stopwords);

        for (String ngram : documentNGrams.getKeys()) {
            addField(jcas, LuceneSkipNgramFeatureExtractor.LUCENE_SKIP_NGRAM_FIELD, ngram); 
        }
       
        try {
            writeToIndex();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}