package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureUtil;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.KeywordNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.KeywordNGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.KeywordComboNGramPairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.KeywordNGramPairFeatureExtractor;

public class CombinedKeywordNGramPairMetaCollector
extends LuceneBasedComboMetaCollector
{

    @ConfigurationParameter(name = KeywordComboNGramPairFeatureExtractor.PARAM_KEYWORD_NGRAM_MIN_N_COMBO, mandatory = true, defaultValue = "2")
	protected int ngramMinNCombo;
    
    @ConfigurationParameter(name = KeywordComboNGramPairFeatureExtractor.PARAM_KEYWORD_NGRAM_MAX_N_COMBO, mandatory = true, defaultValue = "4")
	protected int ngramMaxNCombo;
	    
	@ConfigurationParameter(name = KeywordNGramPairFeatureExtractor.PARAM_KEYWORD_NGRAM_MIN_N_VIEW1, mandatory = true, defaultValue = "1")
	private int ngramMinN1;
	
	@ConfigurationParameter(name = KeywordNGramPairFeatureExtractor.PARAM_KEYWORD_NGRAM_MIN_N_VIEW2, mandatory = true, defaultValue = "1")
	private int ngramMinN2;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
	private int ngramMinN;
	
	@ConfigurationParameter(name = KeywordNGramPairFeatureExtractor.PARAM_KEYWORD_NGRAM_MAX_N_VIEW1, mandatory = true, defaultValue = "3")
	private int ngramMaxN1;
	
	@ConfigurationParameter(name = KeywordNGramPairFeatureExtractor.PARAM_KEYWORD_NGRAM_MAX_N_VIEW2, mandatory = true, defaultValue = "3")
	private int ngramMaxN2;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
	private int ngramMaxN;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_NGRAM_KEYWORDS_FILE, mandatory = true)
	protected String keywordsFile;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MARK_SENTENCE_BOUNDARY, mandatory = false, defaultValue = "true")
	private boolean markSentenceBoundary;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MARK_SENTENCE_LOCATION, mandatory = false, defaultValue = "false")
	private boolean markSentenceLocation;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_INCLUDE_COMMAS, mandatory = false, defaultValue = "false")
	private boolean includeCommas;
	
	private Set<String> keywords;

@Override
public void initialize(UimaContext context)
    throws ResourceInitializationException
{
    super.initialize(context);
    
    try {
        keywords = FeatureUtil.getStopwords(keywordsFile, true);
    }
    catch (IOException e) {
        throw new ResourceInitializationException(e);
    }
}

    protected int getNgramMinNCombo(){
        return ngramMinNCombo;
    }
    protected int getNgramMaxNCombo(){
        return ngramMaxNCombo;
    }
    
    @Override
    protected FrequencyDistribution<String> getNgramsFD(List<JCas> jcases)
        throws TextClassificationException
    {
        return KeywordNGramUtils.getMultipleViewKeywordNgrams(
                jcases, ngramMinN, ngramMaxN, markSentenceBoundary, markSentenceLocation, includeCommas, keywords);
    }
    /**
     * This is an artifact to be merged with getNgramsFD(List<JCas> jcases) when pair FEs are ready.
     */
    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas)
        throws TextClassificationException
    {
        return null;
    }
    
    @Override
    protected FrequencyDistribution<String> getNgramsFDView1(JCas view1)
        throws TextClassificationException
    {
        return KeywordNGramUtils.getDocumentKeywordNgrams(
                view1, ngramMinN1, ngramMaxN1, markSentenceBoundary, markSentenceLocation, includeCommas, keywords);
    }
    
    @Override
    protected FrequencyDistribution<String> getNgramsFDView2(JCas view2)
        throws TextClassificationException
    {
        return KeywordNGramUtils.getDocumentKeywordNgrams(
                view2, ngramMinN2, ngramMaxN2, markSentenceBoundary, markSentenceLocation, includeCommas, keywords);
    }
    
    @Override
    protected String getFieldName()
    {
        return KeywordNGramFeatureExtractorBase.KEYWORD_NGRAM_FIELD;
    }
    @Override
    protected String getFieldNameView1()
    {
        return KeywordNGramPairFeatureExtractor.KEYWORD_NGRAM_FIELD1;
    }
    @Override
    protected String getFieldNameView2()
    {
        return KeywordNGramPairFeatureExtractor.KEYWORD_NGRAM_FIELD2;
    }
    @Override
    protected String getFieldNameCombo(){
    	return KeywordComboNGramPairFeatureExtractor.KEYWORD_NGRAM_FIELD_COMBO;
    }

}
