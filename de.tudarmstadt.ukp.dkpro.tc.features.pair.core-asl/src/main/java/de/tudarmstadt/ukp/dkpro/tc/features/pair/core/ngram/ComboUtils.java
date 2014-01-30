package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

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
    	
    	JCas view;
    	if(name.equals(AbstractPairReader.PART_ONE) || name.equals(AbstractPairReader.PART_TWO)){
	        try{
	            view = jcas.getView(name); 
	        }
	        catch (Exception e) {
	            throw new TextClassificationException(e);
	        }
    	}else{
    		view = jcas;
    	}
        
        FrequencyDistribution<String> viewNgrams = null;
                    
        if (classificationUnit == null) {
            viewNgrams = NGramUtils.getDocumentNgrams(view,
                    ngramLowerCase, ngramMinN, ngramMaxN, stopwords);
        }
        else {
            viewNgrams = NGramUtils.getAnnotationNgrams(view, classificationUnit,
                    ngramLowerCase, ngramMinN, ngramMaxN, stopwords);
        }
    	
    	return viewNgrams;
    }
}
