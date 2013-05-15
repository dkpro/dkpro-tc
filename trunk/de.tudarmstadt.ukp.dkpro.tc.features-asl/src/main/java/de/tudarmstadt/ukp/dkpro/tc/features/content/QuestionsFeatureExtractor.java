package de.tudarmstadt.ukp.dkpro.tc.features.content;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;


public class QuestionsFeatureExtractor
    implements SimpleFeatureExtractor
{

    public static final String FEATURE_NAME = "QuestionRatio";


	public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
			throws CleartkExtractorException {
		
		double sentences = JCasUtil.select(jcas, Sentence.class).size();
		String text = jcas.getDocumentText();

		Pattern p = Pattern.compile("\\?[^\\?]"); //don't count multiple question marks as multiple questions

		int matches = 0;
		Matcher m = p.matcher(text);
		while(m.find()){
			matches++;
		}

		return Arrays.asList(new Feature(FEATURE_NAME,sentences>0?(matches/sentences):0));
	}	
 
}
