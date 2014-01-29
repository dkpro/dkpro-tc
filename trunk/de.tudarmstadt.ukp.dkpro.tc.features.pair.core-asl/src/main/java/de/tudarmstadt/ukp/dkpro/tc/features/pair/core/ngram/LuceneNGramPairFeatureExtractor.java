package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

public class LuceneNGramPairFeatureExtractor
	extends LucenePairFeatureExtractorBase
{
    
    public static final String PARAM_NGRAM_MIN_N_VIEW1 = "pairNgramMinNView1";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_N_VIEW1, mandatory = true, defaultValue = "1")
    protected int ngramMinN1;
    
    public static final String PARAM_NGRAM_MIN_N_VIEW2 = "pairNgramMinNView2";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_N_VIEW2, mandatory = true, defaultValue = "1")
    protected int ngramMinN2;
    
    public static final String PARAM_NGRAM_MIN_N_ALL = "pairNgramMinNAll";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_N_ALL, mandatory = true, defaultValue = "1")
    protected int ngramMinNAll;
    
    public static final String PARAM_NGRAM_MAX_N_VIEW1 = "pairNgramMaxNView1";
    @ConfigurationParameter(name = PARAM_NGRAM_MAX_N_VIEW1, mandatory = true, defaultValue = "3")
    protected int ngramMaxN1;
    
    public static final String PARAM_NGRAM_MAX_N_VIEW2 = "pairNgramMaxNView2";
    @ConfigurationParameter(name = PARAM_NGRAM_MAX_N_VIEW2, mandatory = true, defaultValue = "3")
    protected int ngramMaxN2;
    
    public static final String PARAM_NGRAM_MAX_N_ALL = "pairNgramMaxNAll";
    @ConfigurationParameter(name = PARAM_NGRAM_MAX_N_ALL, mandatory = true, defaultValue = "3")
    protected int ngramMaxNAll;
    
    public static final String PARAM_NGRAM_USE_TOP_K_VIEW1 = "pairNgramUseTopK1";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K_VIEW1, mandatory = true, defaultValue = "500")
    protected int ngramUseTopK1;
    
    public static final String PARAM_NGRAM_USE_TOP_K_VIEW2 = "pairNgramUseTopK2";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K_VIEW2, mandatory = true, defaultValue = "500")
    protected int ngramUseTopK2;
    
    public static final String PARAM_NGRAM_USE_TOP_K_ALL = "pairNgramUseTopKAll";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K_ALL, mandatory = true, defaultValue = "500")
    protected int ngramUseTopKAll;
    
    public static final String PARAM_NGRAM_STOPWORDS_FILE = "pairNgramStopwordsFile";
    @ConfigurationParameter(name = PARAM_NGRAM_STOPWORDS_FILE, mandatory = false)
    protected String ngramStopwordsFile;

    public static final String PARAM_NGRAM_FREQ_THRESHOLD_VIEW1 = "pairNgramFreqThreshold1";
    @ConfigurationParameter(name = PARAM_NGRAM_FREQ_THRESHOLD_VIEW1, mandatory = true, defaultValue = "0.01")
    protected float ngramFreqThreshold1;
    
    public static final String PARAM_NGRAM_FREQ_THRESHOLD_VIEW2 = "pairNgramFreqThreshold2";
    @ConfigurationParameter(name = PARAM_NGRAM_FREQ_THRESHOLD_VIEW2, mandatory = true, defaultValue = "0.01")
    protected float ngramFreqThreshold2;
    
    public static final String PARAM_NGRAM_FREQ_THRESHOLD_ALL = "pairNgramFreqThresholdAll";
    @ConfigurationParameter(name = PARAM_NGRAM_FREQ_THRESHOLD_ALL, mandatory = true, defaultValue = "0.01")
    protected float ngramFreqThresholdAll;
    
    public static final String PARAM_NGRAM_LOWER_CASE = "pairNgramLowerCase";
    @ConfigurationParameter(name = PARAM_NGRAM_LOWER_CASE, mandatory = true, defaultValue = "true")
    protected boolean ngramLowerCase;

    public static final String PARAM_NGRAM_MIN_TOKEN_LENGTH_THRESHOLD = "pairNgramMinTokenLengthThreshold";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_TOKEN_LENGTH_THRESHOLD, mandatory = true, defaultValue = "1")
    protected int ngramMinTokenLengthThreshold;
    
    
    public static final String LUCENE_NGRAM_FIELD = "ngram";
    public static final String LUCENE_NGRAM_FIELD1 = "ngram1";
    public static final String LUCENE_NGRAM_FIELD2 = "ngram2";

    protected Set<String> stopwords;
    protected Set<String> topKSetView1;
    protected Set<String> topKSetView2;
    protected Set<String> topKSetAll;
    protected String prefix;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        stopwords = getStopwords();

        topKSetAll = getTopNgrams();
        topKSetView1 = getTopNgramsView1();
        topKSetView2 = getTopNgramsView2();
        
        prefix = "pair_ngrams_";

        return true;
    }
    
    @Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {     
        JCas view1;
        JCas view2;
        try{
            view1 = jcas.getView("PART_ONE");
            view2 = jcas.getView("PART_TWO");   
        }
        catch (Exception e) {
            throw new TextClassificationException(e);
        }
        
        FrequencyDistribution<String> view1Ngrams = null;
        FrequencyDistribution<String> view2Ngrams = null;
//        FrequencyDistribution<String> pairNgrams = null;
                    
        if (classificationUnit == null) {
            view1Ngrams = NGramUtils.getDocumentNgrams(view1,
                    ngramLowerCase, ngramMinN1, ngramMaxN1, stopwords);
            view2Ngrams = NGramUtils.getDocumentNgrams(view2, 
                    ngramLowerCase, ngramMinN2, ngramMaxN2, stopwords);
        }
        else {
            view1Ngrams = NGramUtils.getAnnotationNgrams(view1, classificationUnit,
                    ngramLowerCase, ngramMinN1, ngramMaxN1, stopwords);
            view2Ngrams = NGramUtils.getAnnotationNgrams(view2, classificationUnit,
                    ngramLowerCase, ngramMinN2, ngramMaxN2, stopwords);
        }
// FIXME something sensible that this could return here without using combos?
//        FrequencyDistribution<String> pairNgrams = NGramUtils.getCombinedNgrams(view1Ngrams,
//                view2Ngrams, ngramMinNCombo, ngramMinNCombo);
//         
        List<Feature> features = new ArrayList<Feature>();
//        for(String pairNgram: topKSetCombo){
//            String featureName = prefix + "_" + pairNgram;
//            if (pairNgrams.contains(pairNgram)) {
//                features.add(new Feature(featureName, 1));
//            }
//            else {
//                features.add(new Feature(featureName, 0));
//            }
//        }
        
        return features;
    }
    
    //TODO This is reused from NGramFeatureExtractorBase
    private Set<String> getStopwords()
            throws ResourceInitializationException
    {
        Set<String> stopwords = new HashSet<String>();
        try {
            if (ngramStopwordsFile != null && !ngramStopwordsFile.isEmpty()) {
                    // each line of the file contains one stopword
                    URL stopUrl = ResourceUtils.resolveLocation(ngramStopwordsFile, null);
                    InputStream is = stopUrl.openStream();
                    stopwords.addAll(IOUtils.readLines(is, "UTF-8"));
            }
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        return stopwords;
    }
	
    protected Set<String> getTopNgrams()
        throws ResourceInitializationException
    {       
        return LuceneNgramUtils.getTopNgrams(ngramUseTopKAll, luceneDir, LUCENE_NGRAM_FIELD);
    }
    
    protected Set<String> getTopNgramsView1()
        throws ResourceInitializationException
    {
        return LuceneNgramUtils.getTopNgrams(ngramUseTopK1, luceneDir, LUCENE_NGRAM_FIELD1);
    }

    protected Set<String> getTopNgramsView2()
        throws ResourceInitializationException
    {
        return LuceneNgramUtils.getTopNgrams(ngramUseTopK2, luceneDir, LUCENE_NGRAM_FIELD2);
    }
}