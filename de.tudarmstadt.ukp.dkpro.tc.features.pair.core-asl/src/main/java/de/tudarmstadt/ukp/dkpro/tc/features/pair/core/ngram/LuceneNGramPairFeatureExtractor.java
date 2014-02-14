package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import com.google.common.collect.MinMaxPriorityQueue;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.TermFreqTuple;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.LuceneNGramPairMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

/**
 * Pair ngram feature extractor for document pair classification.  
 * Can be used to extract ngrams from one or both documents in the pair, and parameters
 * for each document (view 1's, view 2's) can be set separately, or both documents can 
 * be treated together as one extended document.
 * <br />
 * Note that ngram features created by this class are each from a single document, i.e., not
 * combinations of ngrams from the pair of documents.  To make combinations of ngrams
 * across both documents, please use {@link CombinedNGramPairFeatureExtractor}.
 * 
 * @author Emily Jamison
 *
 */
public class LuceneNGramPairFeatureExtractor
	extends LuceneFeatureExtractorBase
{
    /**
     * Minimum size n of ngrams from View 1's.
     */
    public static final String PARAM_NGRAM_MIN_N_VIEW1 = "pairNgramMinNView1";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_N_VIEW1, mandatory = true, defaultValue = "1")
    protected int ngramMinN1;
    /**
     * Minimum size n of ngrams from View 2's.
     */
    public static final String PARAM_NGRAM_MIN_N_VIEW2 = "pairNgramMinNView2";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_N_VIEW2, mandatory = true, defaultValue = "1")
    protected int ngramMinN2;
    /**
     * Maximum size n of ngrams from View 1's.
     */
    public static final String PARAM_NGRAM_MAX_N_VIEW1 = "pairNgramMaxNView1";
    @ConfigurationParameter(name = PARAM_NGRAM_MAX_N_VIEW1, mandatory = true, defaultValue = "3")
    protected int ngramMaxN1;
    /**
     * Maximum size n of ngrams from View 2's.
     */
    public static final String PARAM_NGRAM_MAX_N_VIEW2 = "pairNgramMaxNView2";
    @ConfigurationParameter(name = PARAM_NGRAM_MAX_N_VIEW2, mandatory = true, defaultValue = "3")
    protected int ngramMaxN2;
    /**
     * Use this number of most frequent ngrams from View 1's.
     */
    public static final String PARAM_NGRAM_USE_TOP_K_VIEW1 = "pairNgramUseTopK1";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K_VIEW1, mandatory = true, defaultValue = "500")
    protected int ngramUseTopK1;
    /**
     * Use this number of most frequent ngrams from View 2's.
     */
    public static final String PARAM_NGRAM_USE_TOP_K_VIEW2 = "pairNgramUseTopK2";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K_VIEW2, mandatory = true, defaultValue = "500")
    protected int ngramUseTopK2;
    /**
     * Each ngram from View 1 documents added to the document pair instance as a feature.  
     * E.g. Feature: view1NG_Dear
     */
    public static final String PARAM_USE_VIEW1_NGRAMS_AS_FEATURES = "useView1NgramsAsFeatures";
    @ConfigurationParameter(name = PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, mandatory = true)
    protected boolean useView1NgramsAsFeatures;
    /**
     * Each ngram from View 1 documents added to the document pair instance as a feature.  
     * E.g. Feature: view2NG_Dear
     */
    public static final String PARAM_USE_VIEW2_NGRAMS_AS_FEATURES = "useView2NgramsAsFeatures";
    @ConfigurationParameter(name = PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, mandatory = true)
    protected boolean useView2NgramsAsFeatures;
    /**
     * All qualifying ngrams from anywhere in either document are used as features.  Feature 
     * does not specify which view the ngram came from.
     * E.g. Feature: allNG_Dear
     */
    public static final String PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES = "useViewBlindNgramsAsFeatures";
    @ConfigurationParameter(name = PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, mandatory = true)
    protected boolean useViewBlindNgramsAsFeatures;
    /**
     * This option collects a FrequencyDistribution of ngrams across both documents of all pairs, 
     * but when writing features, the view where a particular ngram is found is recorded with the ngram.
     * For example, using a {@link #PARAM_NGRAM_USE_TOP_K_ALL} value of 500, 400 of the ngrams in the 
     * top 500 might happen to be from View 2's; and whenever an ngram from the 500 is seen in any 
     * document, view 1 or 2, the document's view is recorded.<br />
     * E.g., Feature: view2allNG_Dear<br />
     * In order to use this option, {@link #PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES} must also be set to true.
     */
    public static final String PARAM_MARK_VIEWBLIND_NGRAMS_WITH_LOCAL_VIEW = "markViewBlindNgramsWithLocalView";
    @ConfigurationParameter(name = PARAM_MARK_VIEWBLIND_NGRAMS_WITH_LOCAL_VIEW, mandatory = false, defaultValue = "false")
    protected boolean markViewBlindNgramsWithLocalView;
    
    // These are only public so the MetaCollector can see them
    public static final String LUCENE_NGRAM_FIELD1 = "ngram1";
    public static final String LUCENE_NGRAM_FIELD2 = "ngram2";

    protected FrequencyDistribution<String> topKSetView1;
    protected FrequencyDistribution<String> topKSetView2;
    
    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(LuceneNGramPairMetaCollector.class);

        return metaCollectorClasses;
    }

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
     
        topKSetView1 = getTopNgramsView1();
        topKSetView2 = getTopNgramsView2();

        return true;
    }
    
    @Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {   
        FrequencyDistribution<String> view1Ngrams = getViewNgrams(
        		AbstractPairReader.PART_ONE, jcas, classificationUnit);
        FrequencyDistribution<String> view2Ngrams = getViewNgrams(
        		AbstractPairReader.PART_TWO, jcas, classificationUnit);
        FrequencyDistribution<String> allNgrams = getViewNgrams(
                AbstractPairReader.INITIAL_VIEW, jcas, classificationUnit);
        
        List<Feature> features = new ArrayList<Feature>();
        if(useView1NgramsAsFeatures){
    		prefix = "view1NG";
        	features = addToFeatureArray(view1Ngrams, topKSetView1, features);
        }
        if(useView2NgramsAsFeatures){
    		prefix = "view2NG";
        	features = addToFeatureArray(view2Ngrams, topKSetView2, features);
        }
        if(useViewBlindNgramsAsFeatures && !markViewBlindNgramsWithLocalView){
    		prefix = "allNG";
        	features = addToFeatureArray(allNgrams, topKSet, features);
        }
        if(useViewBlindNgramsAsFeatures && markViewBlindNgramsWithLocalView){
            prefix = "view1allNG";
            features = addToFeatureArray(view1Ngrams, topKSet, features);
            prefix = "view2allNG";
            features = addToFeatureArray(view2Ngrams, topKSet, features);
        }
        
        return features;
    }

	protected List<Feature> addToFeatureArray(FrequencyDistribution<String> viewNgrams, FrequencyDistribution<String> topKSet,
			List<Feature> features)
	{
		for(String ngram: topKSet.getKeys()){
			if(viewNgrams.contains(ngram)){
				features.add(new Feature(ComboUtils.combo(prefix, ngram), 1));
			}else{
				features.add(new Feature(ComboUtils.combo(prefix, ngram), 0));
			}
		}
		return features;
	}
    
    @Override
    protected FrequencyDistribution<String> getTopNgrams()
        throws ResourceInitializationException
    {       
        return getTopNgrams(ngramUseTopK, LuceneFeatureExtractorBase.LUCENE_NGRAM_FIELD);
    }
    
    protected FrequencyDistribution<String> getTopNgramsView1()
        throws ResourceInitializationException
    {
        return getTopNgrams(ngramUseTopK1, LUCENE_NGRAM_FIELD1);
    }

    protected FrequencyDistribution<String> getTopNgramsView2()
        throws ResourceInitializationException
    {
        return getTopNgrams(ngramUseTopK2, LUCENE_NGRAM_FIELD2);
    }
    
    private FrequencyDistribution<String> getTopNgrams(int topNgramThreshold, String fieldName)
        throws ResourceInitializationException
    {       

    	FrequencyDistribution<String> topNGrams = new FrequencyDistribution<String>();
        
        MinMaxPriorityQueue<TermFreqTuple> topN = MinMaxPriorityQueue.maximumSize(topNgramThreshold).create();
        IndexReader reader;
        try {
            reader = DirectoryReader.open(FSDirectory.open(luceneDir));
            Fields fields = MultiFields.getFields(reader);
            if (fields != null) {
                Terms terms = fields.terms(fieldName);
                if (terms != null) {
                    TermsEnum termsEnum = terms.iterator(null);
                    BytesRef text = null;
                    while ((text = termsEnum.next()) != null) {
                        String term = text.utf8ToString();
                        long freq = termsEnum.totalTermFreq();
                        topN.add(new TermFreqTuple(term, freq));
                    }
                }
            }
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        
        int size = topN.size();
        for (int i=0; i < size; i++) {
            TermFreqTuple tuple = topN.poll();
//            	System.out.println(tuple.getTerm() + " - " + tuple.getFreq());
            topNGrams.addSample(tuple.getTerm(), tuple.getFreq());
        }
        
        return topNGrams;
    }
    
    protected FrequencyDistribution<String> getViewNgrams(String name, JCas jcas, 
    		TextClassificationUnit classificationUnit)
    		throws TextClassificationException{
    	
    	JCas view1 = null;
    	JCas view2 = null;
    	try{
    		view1 = jcas.getView(AbstractPairReader.PART_ONE);
    		view2 = jcas.getView(AbstractPairReader.PART_TWO);
    	}catch (Exception e){
    		throw new TextClassificationException(e);
    	}
    	
    	if(name.equals(AbstractPairReader.PART_ONE)){
    		return NGramUtils.getDocumentNgrams(
                    view1, ngramLowerCase, filterPartialStopwordMatches, ngramMinN1, ngramMaxN1, stopwords);
    	}else if(name.equals(AbstractPairReader.PART_TWO)){
    		return NGramUtils.getDocumentNgrams(
                    view2, ngramLowerCase, filterPartialStopwordMatches, ngramMinN2, ngramMaxN2, stopwords);
    	}else{
    		List<JCas> jcases = new ArrayList<JCas>();
    		jcases.add(view1);
    		jcases.add(view2);
    		return ComboUtils.getMultipleViewNgrams(
                    jcases, null, ngramLowerCase, filterPartialStopwordMatches, ngramMinN, ngramMaxN, stopwords);
    	}
    	
    }

    @Override
    protected String getFieldName()
    {
        return LUCENE_NGRAM_FIELD;
    }

    @Override
    protected int getTopN()
    {
        return ngramUseTopK;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return "allNG";
    }
    
    //TODO This shouldn't be inherited here.
    @Override
    protected FrequencyDistribution<String> getDocumentNgrams(JCas jcas)
        throws TextClassificationException
    {
        return null;
    }
    //TODO This shouldn't be inherited here.
    @Override
    protected FrequencyDistribution<String> getAnnotationNgrams(JCas jcas, Annotation anno)
        throws TextClassificationException
    {
        return null;
    }
    protected void setStopwords(Set<String> newStopwords){
    	stopwords = newStopwords;
    }
    protected void setFilterPartialStopwordMatches(boolean filtering){
    	filterPartialStopwordMatches = filtering;
    }
    protected void setLowerCase(boolean isLower){
    	ngramLowerCase = isLower;
    }
    protected void makeTopKSet(FrequencyDistribution<String> topK){
    	topKSet = topK;
    }
}