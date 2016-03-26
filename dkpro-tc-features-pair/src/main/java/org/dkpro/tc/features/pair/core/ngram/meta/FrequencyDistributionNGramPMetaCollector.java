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
package org.dkpro.tc.features.pair.core.ngram.meta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.model.DfModel;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.util.TfidfUtils;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.features.ngram.LuceneNGramDFE;
import org.dkpro.tc.features.ngram.base.FrequencyDistributionNGramFeatureExtractorBase;
import org.dkpro.tc.features.ngram.meta.FreqDistBasedMetaCollector;
import org.dkpro.tc.features.ngram.util.NGramUtils;
import org.dkpro.tc.features.pair.core.ngram.LuceneNGramPFE;

public class FrequencyDistributionNGramPMetaCollector
	extends FreqDistBasedMetaCollector implements Constants

{
    public static final String NGRAM_FD_KEY = "ngramsPair.ser";
    
    @ConfigurationParameter(name = FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_FD_FILE, mandatory = true)
    private File ngramFdFile;
    
    @ConfigurationParameter(name = FrequencyDistributionNGramFeatureExtractorBase.PARAM_DFSTORE_FILE, mandatory = true)
    private File dfstoreFile;
    
    @ConfigurationParameter(name = LuceneNGramPFE.PARAM_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
	protected int ngramMinN;

    @ConfigurationParameter(name = LuceneNGramPFE.PARAM_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
	protected int ngramMaxN;

    @ConfigurationParameter(name = LuceneNGramPFE.PARAM_NGRAM_STOPWORDS_FILE, mandatory = false)
    protected String ngramStopwordsFile;

    @ConfigurationParameter(name = LuceneNGramPFE.PARAM_FILTER_PARTIAL_STOPWORD_MATCHES, mandatory = true, defaultValue="false")
	protected boolean filterPartialStopwordMatches;

    @ConfigurationParameter(name = LuceneNGramPFE.PARAM_NGRAM_LOWER_CASE, mandatory = false, defaultValue = "true")
	protected boolean ngramLowerCase;	

    protected Set<String> stopwords;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        
        try {
            stopwords = FeatureUtil.getStopwords(ngramStopwordsFile, ngramLowerCase);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }
    
    @Override
    protected File getFreqDistFile()
    {
        return ngramFdFile;
    }

	@Override
	public Map<String, String> getParameterKeyPairs() {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put(FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_FD_FILE, NGRAM_FD_KEY);
        return mapping;
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		 JCas view1;
	        JCas view2;
	        try {
	            view1 = jcas.getView(PART_ONE);
	            view2 = jcas.getView(PART_TWO);
	        }
	        catch (Exception e) {
	            throw new AnalysisEngineProcessException(e);
	        }

	        List<JCas> jcases = new ArrayList<JCas>();
	        jcases.add(view1);
	        jcases.add(view2);

	        FrequencyDistribution<String> view1NGrams;
	        FrequencyDistribution<String> view2NGrams;
	        try {
	            view1NGrams = getNgramsFDView1(view1);
	            view2NGrams = getNgramsFDView2(view2);
	        }
	        catch (TextClassificationException e) {
	            throw new AnalysisEngineProcessException(e);
	        }	
	        
    		dfStore.registerNewDocument();
	        for (String ngram : view1NGrams.getKeys()) {
	            fd.addSample(ngram, view1NGrams.getCount(ngram));
	            dfStore.countTerm(ngram);
	        }    
	        for (String ngram : view2NGrams.getKeys()) {
	            fd.addSample(ngram, view2NGrams.getCount(ngram));
	            dfStore.countTerm(ngram);

	        }   
	        dfStore.closeCurrentDocument();

	}
	
	    protected FrequencyDistribution<String> getNgramsFDView1(JCas view1)
	        throws TextClassificationException
	    {
	    	FrequencyDistribution<String> fd = NGramUtils.getDocumentNgrams(
	    	              view1, ngramLowerCase, filterPartialStopwordMatches, ngramMinN, 
	    	              ngramMaxN, stopwords);
	        return fd;
	    }

	    protected FrequencyDistribution<String> getNgramsFDView2(JCas view2)
	        throws TextClassificationException
	    {
	    	FrequencyDistribution<String> fd = NGramUtils.getDocumentNgrams(
	    	              view2, ngramLowerCase, filterPartialStopwordMatches, ngramMinN, 
	    	              ngramMaxN, stopwords);
	        return fd;
	    }

		@Override
		protected File getDfStoreFile() {
			return dfstoreFile;
		}
		
	    @Override
	    public void collectionProcessComplete()
	        throws AnalysisEngineProcessException
	    {
	        super.collectionProcessComplete();

	        try {
	            TfidfUtils.writeDfModel(dfStore, getDfStoreFile().getAbsolutePath());
	        }
	        catch (Exception e) {
	            throw new AnalysisEngineProcessException(e);
	        }
	    }
	    }