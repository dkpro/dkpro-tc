/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import java.io.IOException;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureUtil;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.KeywordNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.KeywordNGramUtils;

public class KeywordNGramMetaCollector
	extends LuceneBasedMetaCollector
{    
	@ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
	private int minN;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
	private int maxN;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_NGRAM_KEYWORDS_FILE, mandatory = true)
	private String keywordsFile;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MARK_SENTENCE_BOUNDARY, mandatory = false, defaultValue = "true")
	private boolean markSentenceBoundary;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_MARK_SENTENCE_LOCATION, mandatory = false, defaultValue = "false")
	private boolean markSentenceLocation;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractorBase.PARAM_KEYWORD_NGRAM_INCLUDE_COMMAS, mandatory = false, defaultValue = "false")
	private boolean includeCommas;


	//private Set<String> stopwords;
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
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas){
        return KeywordNGramUtils.getDocumentKeywordNgrams(
                jcas, minN, maxN, markSentenceBoundary, markSentenceLocation, includeCommas, keywords);
    }
    
    @Override
    protected String getFieldName(){
        return KeywordNGramFeatureExtractorBase.KEYWORD_NGRAM_FIELD;
    }
	
}
