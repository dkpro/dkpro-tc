package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.io.File;
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
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

public abstract class NGramPairFeatureExtractorBase
		extends FeatureExtractorResource_ImplBase
		implements MetaDependent, ClassificationUnitFeatureExtractor
{
    public static final String PARAM_NGRAM_MIN_N_VIEW1 = "ngramMinNView1";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_N_VIEW1, mandatory = false, defaultValue = "1")
    private int ngramMinN1;
    
    public static final String PARAM_NGRAM_MIN_N_VIEW2 = "ngramMinNView2";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_N_VIEW2, mandatory = false, defaultValue = "1")
    private int ngramMinN2;
    
    @ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_MIN_N, mandatory = false, defaultValue = "1")
    private int ngramMinNAll;
    
    public static final String PARAM_NGRAM_MIN_N_COMBO = "ngramMinNCombo";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_N_COMBO, mandatory = false, defaultValue = "1")
    protected int ngramMinNCombo;

    public static final String PARAM_NGRAM_MAX_N_VIEW1 = "ngramMaxNView1";
    @ConfigurationParameter(name = PARAM_NGRAM_MAX_N_VIEW1, mandatory = false, defaultValue = "3")
    private int ngramMaxN1;
    
    public static final String PARAM_NGRAM_MAX_N_VIEW2 = "ngramMaxNView2";
    @ConfigurationParameter(name = PARAM_NGRAM_MAX_N_VIEW2, mandatory = false, defaultValue = "3")
    private int ngramMaxN2;
    
    @ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_MAX_N, mandatory = false, defaultValue = "3")
    private int ngramMaxNAll;
    
    public static final String PARAM_NGRAM_MAX_N_COMBO = "ngramMaxNCombo";
    @ConfigurationParameter(name = PARAM_NGRAM_MAX_N_COMBO, mandatory = false, defaultValue = "3")
    protected int ngramMaxNCombo;

    public static final String PARAM_NGRAM_USE_TOP_K_VIEW1 = "ngramUseTopK1";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K_VIEW1, mandatory = false, defaultValue = "500")
    private int ngramUseTopK1;
    
    public static final String PARAM_NGRAM_USE_TOP_K_VIEW2 = "ngramUseTopK2";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K_VIEW2, mandatory = false, defaultValue = "500")
    private int ngramUseTopK2;
    
    @ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_USE_TOP_K, mandatory = false, defaultValue = "500")
    private int ngramUseTopKAll;
    
    public static final String PARAM_NGRAM_USE_TOP_K_COMBO = "ngramUseTopKCombo";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K_COMBO, mandatory = false, defaultValue = "500")
	protected int ngramUseTopKCombo;

	@ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_STOPWORDS_FILE, mandatory = false)
	protected String ngramStopwordsFile;

    public static final String PARAM_NGRAM_FREQ_THRESHOLD_VIEW1 = "ngramFreqThreshold1";
    @ConfigurationParameter(name = PARAM_NGRAM_FREQ_THRESHOLD_VIEW1, mandatory = false, defaultValue = "0.01")
    private float ngramFreqThreshold1;
    
    public static final String PARAM_NGRAM_FREQ_THRESHOLD_VIEW2 = "ngramFreqThreshold2";
    @ConfigurationParameter(name = PARAM_NGRAM_FREQ_THRESHOLD_VIEW2, mandatory = false, defaultValue = "0.01")
    private float ngramFreqThreshold2;
    
    @ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_FREQ_THRESHOLD, mandatory = false, defaultValue = "0.01")
    private float ngramFreqThresholdAll;
    
    public static final String PARAM_NGRAM_FREQ_THRESHOLD_COMBO = "ngramFreqThresholdCombo";
    @ConfigurationParameter(name = PARAM_NGRAM_FREQ_THRESHOLD_COMBO, mandatory = false, defaultValue = "0.01")
    private float ngramFreqThresholdCombo;

	@ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_LOWER_CASE, mandatory = false, defaultValue = "true")
	protected boolean ngramLowerCase;

	@ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_MIN_TOKEN_LENGTH_THRESHOLD, mandatory = false, defaultValue = "1")
	protected int ngramMinTokenLengthThreshold;
	
    @ConfigurationParameter(name = LuceneNGramFeatureExtractor.PARAM_LUCENE_DIR, mandatory = true)
    private File luceneDirAll;

    public static final String LUCENE_NGRAM_FIELD = "ngram";
    public static final String LUCENE_NGRAM_FIELD1 = "ngram1";
    public static final String LUCENE_NGRAM_FIELD2 = "ngram2";
    

    protected Set<String> stopwords;
    protected Set<String> topKSetView1;
    protected Set<String> topKSetView2;
    protected Set<String> topKSetAll;
    protected Set<String> topKSetCombo;
    protected String prefix;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        stopwords = getStopwords();

        topKSetAll = getTopNgrams(ngramFreqThresholdAll, ngramUseTopKAll, LUCENE_NGRAM_FIELD);
        topKSetView1 = getTopNgrams(ngramFreqThreshold1, ngramUseTopK1, LUCENE_NGRAM_FIELD1);
        topKSetView2 = getTopNgrams(ngramFreqThreshold2, ngramUseTopK2, LUCENE_NGRAM_FIELD2);
        topKSetCombo = getTopNgramsCombo(topKSetAll, topKSetView1, topKSetView2, ngramFreqThresholdCombo, ngramUseTopKCombo);

        
        prefix = "ngrams_";

        return true;
    }
    
    @Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {
        return this.extractFromAnnotation(jcas, classificationUnit);
    }
    
    protected List<Feature> extractFromAnnotation(JCas jcas, Annotation annotation)
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
    	
        List<Feature> features = new ArrayList<Feature>();
        FrequencyDistribution<String> view1Ngrams = null;
        FrequencyDistribution<String> view2Ngrams = null;
//        FrequencyDistribution<String> pairNgrams = null;
                    
        if (annotation == null) {
        	view1Ngrams = NGramUtils.getDocumentNgrams(view1,
                    ngramLowerCase, ngramMinN1, ngramMaxN1, stopwords);
        	view2Ngrams = NGramUtils.getDocumentNgrams(view2, 
                    ngramLowerCase, ngramMinN2, ngramMaxN2, stopwords);
        }
        else {
        	view1Ngrams = NGramUtils.getAnnotationNgrams(view1, annotation,
                    ngramLowerCase, ngramMinN1, ngramMaxN1, stopwords);
        	view2Ngrams = NGramUtils.getAnnotationNgrams(view2, annotation,
                    ngramLowerCase, ngramMinN2, ngramMaxN2, stopwords);
        }
        FrequencyDistribution<String> pairNgrams = NGramUtils.getCombinedNgrams(view1Ngrams,
        		view2Ngrams, ngramMinNCombo, ngramMinNCombo);
         
        for(String pairNgram: topKSetCombo){
        	String featureName = prefix + "_" + pairNgram;
        	if (pairNgrams.contains(pairNgram)) {
        		features.add(new Feature(featureName, 1));
        	}
        	else {
        		features.add(new Feature(featureName, 0));
        	}
        }
        
        return features;
    }
    
    protected abstract Set<String> getTopNgrams(float ngramFreqThreshold, int ngramUseTopK, String field)
            throws ResourceInitializationException;
    
    protected abstract Set<String> getTopNgramsCombo(Set<String> topKSetAll, Set<String> topKSetView1, 
    					Set<String> topKSetView2, float ngramFreqThresholdCombo, 
    					int ngramUseTopKCombo)
            throws ResourceInitializationException;
    
    protected String combo(String ngram1, String ngram2){
    	return combo(prefix, ngram1, ngram2);
    }
    protected static String combo(String prefix, String ngram1, String ngram2){
    	return prefix + ngram1 + "_" + ngram2;
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
}
