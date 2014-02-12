package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramUtils;

public class ComboUtils
{
	private static final String JOINT = "_";
	
    public static String combo(String prefix, String ngram1, String ngram2){
        return prefix + JOINT + ngram1 + JOINT + ngram2;
    }
    public static String combo(String prefix, String comboNgram){
    	return prefix + JOINT + comboNgram;
    }
    
}
