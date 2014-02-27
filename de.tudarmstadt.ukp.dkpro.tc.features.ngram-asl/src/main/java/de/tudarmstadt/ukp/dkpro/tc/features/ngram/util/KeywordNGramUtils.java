package de.tudarmstadt.ukp.dkpro.tc.features.ngram.util;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.fit.util.JCasUtil.toText;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringListIterable;

public class KeywordNGramUtils
{
    public static String SENTENCE_BOUNDARY = "SB";
    public static final String COMMA = "CA";
    public static final String GLUE = "_";
    
    //all tokens should be already lowercased
    public static FrequencyDistribution<String> getDocumentKeywordNgrams(
            JCas jcas,
            int minN,
            int maxN,
            boolean markSentenceBoundary,
            boolean markSentenceLocation,
            boolean includeCommas,
            Set<String> keywords)
    {
        FrequencyDistribution<String> documentNgrams = new FrequencyDistribution<String>();
        List<String> keywordList = new ArrayList<String>();
        int sentenceNumber = 0;
        int totalSentences = select(jcas, Sentence.class).size();
        for (Sentence s : select(jcas, Sentence.class)) {
            for(String token: toText(selectCovered(Token.class, s))){
                token = token.toLowerCase();
                if(keywords.contains(token)){
                    keywordList.add(token);
                }else if(includeCommas && token.equals(",")){
                    keywordList.add(COMMA);
                }
            }
            String sentenceBoundary = SENTENCE_BOUNDARY;
            if(markSentenceLocation){
                if((new Float(sentenceNumber) / totalSentences) < 0.25){
                    sentenceBoundary = sentenceBoundary + "BEG";
                }else if((new Float(sentenceNumber) / totalSentences) > 0.75){
                    sentenceBoundary = sentenceBoundary + "END";
                }else{
                    sentenceBoundary = sentenceBoundary + "MID";
                }
            }
            keywordList.add(sentenceBoundary);
            sentenceNumber++;
        }
        for (List<String> ngram : new NGramStringListIterable(keywordList.toArray(new String[keywordList.size()]), minN, maxN)) {
            String ngramString = StringUtils.join(ngram, GLUE);
            documentNgrams.inc(ngramString);
        }
        return documentNgrams;
    }
    public static FrequencyDistribution<String> getMultipleViewKeywordNgrams(
            List<JCas> jcases,
            int minN,
            int maxN,
            boolean markSentenceBoundary,
            boolean markSentenceLocation,
            boolean includeCommas,
            Set<String> keywords){
    	
        FrequencyDistribution<String> viewNgramsTotal = new FrequencyDistribution<String>();
        
        for(JCas view: jcases){
            FrequencyDistribution<String> oneViewsNgrams = new FrequencyDistribution<String>();
            oneViewsNgrams = getDocumentKeywordNgrams(
                    view, minN, maxN, markSentenceBoundary, markSentenceLocation, includeCommas, keywords);
            // This is a hack because there's no method to combine 2 FD's
            for(String key: oneViewsNgrams.getKeys()){
                viewNgramsTotal.addSample(key, oneViewsNgrams.getCount(key));
            }
        }
    	
    	return viewNgramsTotal;
    	
    }
}
