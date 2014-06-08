package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.io.IOException;
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
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import com.google.common.collect.MinMaxPriorityQueue;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureUtil;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.KeywordNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LuceneFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.KeywordNGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.NGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.TermFreqTuple;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.KeywordNGramPairMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.LucenePairFeatureExtractorBase;

/**
 * Pair keyword ngram feature extractor for
 * {@link de.tudarmstadt.ukp.dkpro.tc.features.ngram.KeywordNGramDFE
 * KeywordNGramDFE} Can be used to extract ngrams from one or both documents in the
 * pair, and parameters for each document (view 1's, view 2's) can be set separately, or both
 * documents can be treated together as one extended document. <br />
 * Note that ngram features created by this class are each from a single document, i.e., not
 * combinations of ngrams from the pair of documents.
 * 
 * @author Emily Jamison
 * 
 */
public class KeywordNGramPairFeatureExtractor
    extends LucenePairFeatureExtractorBase
    implements PairFeatureExtractor
{

    @ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int ngramMinN;

    @ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int ngramMaxN;

    @ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_NGRAM_KEYWORDS_FILE, mandatory = true)
    protected String keywordsFile;

    @ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MARK_SENTENCE_BOUNDARY, mandatory = false, defaultValue = "true")
	protected boolean markSentenceBoundary;

    @ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MARK_SENTENCE_LOCATION, mandatory = false, defaultValue = "false")
	protected boolean markSentenceLocation;

    @ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_INCLUDE_COMMAS, mandatory = false, defaultValue = "false")
	protected boolean includeCommas;

    protected Set<String> keywords;
    /**
     * Minimum size n of ngrams from View 1's.
     */
    public static final String PARAM_KEYWORD_NGRAM_MIN_N_VIEW1 = "keywordNgramMinNView1";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_MIN_N_VIEW1, mandatory = true, defaultValue = "1")
    protected int ngramMinN1;
    /**
     * Minimum size n of ngrams from View 2's.
     */
    public static final String PARAM_KEYWORD_NGRAM_MIN_N_VIEW2 = "keywordNgramMinNView2";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_MIN_N_VIEW2, mandatory = true, defaultValue = "1")
    protected int ngramMinN2;
    /**
     * Maximum size n of ngrams from View 1's.
     */
    public static final String PARAM_KEYWORD_NGRAM_MAX_N_VIEW1 = "keywordNgramMaxNView1";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_MAX_N_VIEW1, mandatory = true, defaultValue = "3")
    protected int ngramMaxN1;
    /**
     * Maximum size n of ngrams from View 2's.
     */
    public static final String PARAM_KEYWORD_NGRAM_MAX_N_VIEW2 = "keywordNgramMaxNView2";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_MAX_N_VIEW2, mandatory = true, defaultValue = "3")
    protected int ngramMaxN2;
    /**
     * Each ngram from View 1 documents added to the document pair instance as a feature. E.g.
     * Feature: view1NG_Dear
     */
    public static final String PARAM_USE_VIEW1_KEYWORD_NGRAMS_AS_FEATURES = "useView1KeywordNgramsAsFeatures";
    @ConfigurationParameter(name = PARAM_USE_VIEW1_KEYWORD_NGRAMS_AS_FEATURES, mandatory = true)
    protected boolean useView1NgramsAsFeatures;
    /**
     * Each ngram from View 1 documents added to the document pair instance as a feature. E.g.
     * Feature: view2NG_Dear
     */
    public static final String PARAM_USE_VIEW2_KEYWORD_NGRAMS_AS_FEATURES = "useView2KeywordNgramsAsFeatures";
    @ConfigurationParameter(name = PARAM_USE_VIEW2_KEYWORD_NGRAMS_AS_FEATURES, mandatory = true)
    protected boolean useView2NgramsAsFeatures;
    /**
     * All qualifying ngrams from anywhere in either document are used as features. Feature does not
     * specify which view the ngram came from. E.g. Feature: allNG_Dear
     */
    public static final String PARAM_USE_VIEWBLIND_KEYWORD_NGRAMS_AS_FEATURES = "useViewBlindKeywordNgramsAsFeatures";
    @ConfigurationParameter(name = PARAM_USE_VIEWBLIND_KEYWORD_NGRAMS_AS_FEATURES, mandatory = true)
    protected boolean useViewBlindNgramsAsFeatures;
    /**
     * This option collects a FrequencyDistribution of ngrams across both documents of all pairs,
     * but when writing features, the view where a particular ngram is found is recorded with the
     * ngram. For example, using a {@link #PARAM_NGRAM_USE_TOP_K_ALL} value of 500, 400 of the
     * ngrams in the top 500 might happen to be from View 2's; and whenever an ngram from the 500 is
     * seen in any document, view 1 or 2, the document's view is recorded.<br />
     * E.g., Feature: view2allNG_Dear<br />
     * In order to use this option, {@link #PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES} must also be set
     * to true.
     */
    public static final String PARAM_MARK_VIEWBLIND_KEYWORD_NGRAMS_WITH_LOCAL_VIEW = "markViewBlindKeywordNgramsWithLocalView";
    @ConfigurationParameter(name = PARAM_MARK_VIEWBLIND_KEYWORD_NGRAMS_WITH_LOCAL_VIEW, mandatory = false, defaultValue = "false")
    protected boolean markViewBlindNgramsWithLocalView;

    // These are only public so the MetaCollector can see them
  public static final String KEYWORD_NGRAM_FIELD1 = "keywordngram1";
  public static final String KEYWORD_NGRAM_FIELD2 = "keywordngram2";
  

    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(KeywordNGramPairMetaCollector.class);

        return metaCollectorClasses;
    }

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
    	//FIXME This is a hack workaround because ngramUseTopK won't set until after super.initialize,
    	// but super.initialize tries to call getTopNgrams() anyways, which needs ngramUseTopK.
        fieldOfTheMoment = "";
        topNOfTheMoment = 1;
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
        fieldOfTheMoment = KeywordNGramFeatureExtractorBase.KEYWORD_NGRAM_FIELD;
        topNOfTheMoment = ngramUseTopK;
        topKSet = getTopNgrams();
        
        fieldOfTheMoment = KEYWORD_NGRAM_FIELD1;
        topNOfTheMoment = ngramUseTopK1;
        topKSetView1 = getTopNgrams();
        
        fieldOfTheMoment = KEYWORD_NGRAM_FIELD2;
        topNOfTheMoment = ngramUseTopK2;
        topKSetView2 = getTopNgrams();

        try {
            keywords = FeatureUtil.getStopwords(keywordsFile, true);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        return true;
    }

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        FrequencyDistribution<String> view1Ngrams = KeywordNGramUtils.getDocumentKeywordNgrams(view1, ngramMinN1, ngramMaxN1,
                markSentenceBoundary, markSentenceLocation, includeCommas, keywords);
        FrequencyDistribution<String> view2Ngrams = KeywordNGramUtils.getDocumentKeywordNgrams(view2, ngramMinN2, ngramMaxN2,
                markSentenceBoundary, markSentenceLocation, includeCommas, keywords);
        FrequencyDistribution<String> allNgrams = getViewNgrams(view1, view2);
        
        List<Feature> features = new ArrayList<Feature>();
        if (useView1NgramsAsFeatures) {
            prefix = "keyNG1";
            features = addToFeatureArray(view1Ngrams, topKSetView1, features);
        }
        if (useView2NgramsAsFeatures) {
            prefix = "keyNG2";
            features = addToFeatureArray(view2Ngrams, topKSetView2, features);
        }
        if (useViewBlindNgramsAsFeatures && !markViewBlindNgramsWithLocalView) {
            prefix = "keyNG";
            features = addToFeatureArray(allNgrams, topKSet, features);
        }
        if (useViewBlindNgramsAsFeatures && markViewBlindNgramsWithLocalView) {
            prefix = "keyNGall1";
            features = addToFeatureArray(view1Ngrams, topKSet, features);
            prefix = "keyNGall2";
            features = addToFeatureArray(view2Ngrams, topKSet, features);
        }

        return features;
    }

    protected FrequencyDistribution<String> getViewNgrams(JCas view1, JCas view2)
    {
        List<JCas> jcases = new ArrayList<JCas>();
        jcases.add(view1);
        jcases.add(view2);
        return KeywordNGramUtils.getMultipleViewKeywordNgrams(jcases, ngramMinN, ngramMaxN,
                markSentenceBoundary, markSentenceLocation, includeCommas, keywords);
    }

    @Override
    protected String getFieldName()
    {
        return fieldOfTheMoment;
    }

    @Override
    protected int getTopN()
    {
        return topNOfTheMoment;
    }
    
    //FIXME This class must be instantiated in NGramFeatureExtractorBase, but can't be anything useful then.
    @Override
    protected String getFeaturePrefix()
    {
        return "ThisIsAnError";
    }
}
