package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

public class ComboUtils
{
	private static final String JOINT = "_";
	
    public static String combo(String prefix, String ngram1, String ngram2){
        return prefix + JOINT + ngram1 + JOINT + ngram2;
    }
    public static String combo(String prefix, String comboNgram){
    	return prefix + JOINT + comboNgram;
    }
    
    public static FrequencyDistribution<String> getViewNgrams(JCas jcas, String name, 
    		TextClassificationUnit classificationUnit, boolean ngramLowerCase, 
    		int ngramMinN, int ngramMaxN, Set<String>stopwords)
    		throws TextClassificationException{
    	
    	List<JCas> views = new ArrayList<JCas>();
    	try{
        	if(name.equals(AbstractPairReader.PART_ONE) || 
        	        name.equals(AbstractPairReader.PART_TWO)){
    	        views.add(jcas.getView(name)); 
        	}else{
                views.add(jcas.getView(AbstractPairReader.PART_ONE)); 
                views.add(jcas.getView(AbstractPairReader.PART_TWO)); 
        	}
    	}catch (Exception e) {
            throw new TextClassificationException(e);
    	}
        FrequencyDistribution<String> viewNgramsTotal = new FrequencyDistribution<String>();
        
        for(JCas view: views){
            FrequencyDistribution<String> oneViewsNgrams = new FrequencyDistribution<String>();
            if (classificationUnit == null) {
                oneViewsNgrams = NGramUtils.getDocumentNgrams(view,
                        ngramLowerCase, ngramMinN, ngramMaxN, stopwords);
            }
            else {
                oneViewsNgrams = NGramUtils.getAnnotationNgrams(view, classificationUnit,
                        ngramLowerCase, ngramMinN, ngramMaxN, stopwords);
            }
            // This is a hack because there's no method to combine 2 FD's
            for(String key: oneViewsNgrams.getKeys()){
                viewNgramsTotal.addSample(key, oneViewsNgrams.getCount(key));
            }
        }
    	
    	return viewNgramsTotal;
    }
}
