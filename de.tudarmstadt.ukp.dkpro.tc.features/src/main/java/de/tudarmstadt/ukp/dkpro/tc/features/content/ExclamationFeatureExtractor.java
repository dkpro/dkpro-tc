package de.tudarmstadt.ukp.dkpro.tc.features.content;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;


public class ExclamationFeatureExtractor
    extends FeatureExtractorResource_ImplBase
{

    public static final String FEATURE_NAME = "ExclamationRatio";


	@Override
    public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
			throws TextClassificationException {
		
		double sentences = JCasUtil.select(jcas, Sentence.class).size();
		String text = jcas.getDocumentText();

		Pattern p = Pattern.compile("\\?[^\\!]"); //don't count multiple question marks as multiple questions

		int matches = 0;
		Matcher m = p.matcher(text);
		while(m.find()){
			matches++;
		}

		return Arrays.asList(new Feature(FEATURE_NAME,sentences>0?(matches/sentences):0));
	}	
 
}
