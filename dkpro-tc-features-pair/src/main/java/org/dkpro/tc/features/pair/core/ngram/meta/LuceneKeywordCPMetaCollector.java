/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.features.pair.core.ngram.meta;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.base.KeywordNGramFeatureExtractorBase;
import org.dkpro.tc.features.ngram.util.KeywordNGramUtils;
import org.dkpro.tc.features.pair.core.ngram.LuceneKeywordCPFE;
import org.dkpro.tc.features.pair.core.ngram.LuceneKeywordPFE;

public class LuceneKeywordCPMetaCollector
extends LuceneCPMetaCollectorBase
{

    @ConfigurationParameter(name = LuceneKeywordCPFE.PARAM_KEYWORD_NGRAM_MIN_N_COMBO, mandatory = true, defaultValue = "2")
	protected int ngramMinNCombo;
    
    @ConfigurationParameter(name = LuceneKeywordCPFE.PARAM_KEYWORD_NGRAM_MAX_N_COMBO, mandatory = true, defaultValue = "4")
	protected int ngramMaxNCombo;
	    
	@ConfigurationParameter(name = LuceneKeywordPFE.PARAM_KEYWORD_NGRAM_MIN_N_VIEW1, mandatory = true, defaultValue = "1")
	private int ngramMinN1;
	
	@ConfigurationParameter(name = LuceneKeywordPFE.PARAM_KEYWORD_NGRAM_MIN_N_VIEW2, mandatory = true, defaultValue = "1")
	private int ngramMinN2;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
	private int ngramMinN;
	
	@ConfigurationParameter(name = LuceneKeywordPFE.PARAM_KEYWORD_NGRAM_MAX_N_VIEW1, mandatory = true, defaultValue = "3")
	private int ngramMaxN1;
	
	@ConfigurationParameter(name = LuceneKeywordPFE.PARAM_KEYWORD_NGRAM_MAX_N_VIEW2, mandatory = true, defaultValue = "3")
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

    @Override
    protected int getNgramMinNCombo(){
        return ngramMinNCombo;
    }
    @Override
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
     * This is an artifact to be merged with {@code getNgramsFD(List<JCas> jcases)} when pair FEs
     * are ready.
     */
    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas)
        throws TextClassificationException
    {
        return null;
    }
    
    @Override
    protected FrequencyDistribution<String> getNgramsFDView1(JCas view1, TextClassificationTarget target)
        throws TextClassificationException
    {
        return KeywordNGramUtils.getDocumentKeywordNgrams(
                view1, target, ngramMinN1, ngramMaxN1, markSentenceBoundary, markSentenceLocation, includeCommas, keywords);
    }
    
    @Override
    protected FrequencyDistribution<String> getNgramsFDView2(JCas view2, TextClassificationTarget target)
        throws TextClassificationException
    {
        return KeywordNGramUtils.getDocumentKeywordNgrams(
                view2, target, ngramMinN2, ngramMaxN2, markSentenceBoundary, markSentenceLocation, includeCommas, keywords);
    }
    
    @Override
    protected String getFieldName()
    {
        return KeywordNGramFeatureExtractorBase.KEYWORD_NGRAM_FIELD;
    }
    @Override
    protected String getFieldNameView1()
    {
        return LuceneKeywordPFE.KEYWORD_NGRAM_FIELD1;
    }
    @Override
    protected String getFieldNameView2()
    {
        return LuceneKeywordPFE.KEYWORD_NGRAM_FIELD2;
    }
    @Override
    protected String getFieldNameCombo(){
    	return LuceneKeywordCPFE.KEYWORD_NGRAM_FIELD_COMBO;
    }

}
