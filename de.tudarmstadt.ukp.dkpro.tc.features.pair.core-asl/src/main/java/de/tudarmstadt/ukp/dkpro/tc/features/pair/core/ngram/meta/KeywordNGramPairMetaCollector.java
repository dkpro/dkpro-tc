package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureUtil;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.KeywordNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneBasedMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.KeywordNGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.KeywordNGramPairFeatureExtractor;

public class KeywordNGramPairMetaCollector
extends LuceneBasedMetaCollector
{

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

@Override
public void process(JCas jcas)
    throws AnalysisEngineProcessException
{
	JCas view1;
	JCas view2;
	try{
		view1 = jcas.getView(AbstractPairReader.PART_ONE);
		view2 = jcas.getView(AbstractPairReader.PART_TWO);
	}
    catch (Exception e) {
        throw new AnalysisEngineProcessException(e);
    }
	List<JCas> jcases = new ArrayList<JCas>();
	jcases.add(view1);
	jcases.add(view2);
    FrequencyDistribution<String> view1NGrams = KeywordNGramUtils.getDocumentKeywordNgrams(
            view1, ngramMinN1, ngramMaxN1, markSentenceBoundary, markSentenceLocation, includeCommas, keywords);
    FrequencyDistribution<String> view2NGrams = KeywordNGramUtils.getDocumentKeywordNgrams(
            view2, ngramMinN2, ngramMaxN2, markSentenceBoundary, markSentenceLocation, includeCommas, keywords);
    FrequencyDistribution<String> documentNGrams = KeywordNGramUtils.getMultipleViewKeywordNgrams(
            jcases, ngramMinN, ngramMaxN, markSentenceBoundary, markSentenceLocation, includeCommas, keywords);


    for (String ngram : documentNGrams.getKeys()) {
        for (int i=0;i<documentNGrams.getCount(ngram);i++){
            addField(jcas, KeywordNGramFeatureExtractorBase.KEYWORD_NGRAM_FIELD, ngram); 
        }
    }
    for (String ngram : view1NGrams.getKeys()) {
        for (int i=0;i<view1NGrams.getCount(ngram);i++){
            addField(jcas, KeywordNGramPairFeatureExtractor.KEYWORD_NGRAM_FIELD1, ngram); 
        }
    }
    for (String ngram : view2NGrams.getKeys()) {
        for (int i=0;i<view2NGrams.getCount(ngram);i++){
            addField(jcas, KeywordNGramPairFeatureExtractor.KEYWORD_NGRAM_FIELD2, ngram); 
        }
    }
    
    try {
    	writeToIndex();
    }
    catch (IOException e) {
        throw new AnalysisEngineProcessException(e);
    }
}   
}
