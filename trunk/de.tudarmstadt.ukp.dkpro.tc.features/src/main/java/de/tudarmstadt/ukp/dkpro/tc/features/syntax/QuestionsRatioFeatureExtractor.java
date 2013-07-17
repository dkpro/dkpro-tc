package de.tudarmstadt.ukp.dkpro.tc.features.syntax;

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

public class QuestionsRatioFeatureExtractor
    extends FeatureExtractorResource_ImplBase
{

    public static final String FN_QUESTION_RATIO = "QuestionRatio";

    @Override
    public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
        throws TextClassificationException
    {

        int nrOfSentences = JCasUtil.select(jcas, Sentence.class).size();
        String text = jcas.getDocumentText();

        Pattern p = Pattern.compile("\\?[^\\?]"); // don't count multiple question marks as multiple
                                                  // questions

        int matches = 0;
        Matcher m = p.matcher(text);
        while (m.find()) {
            matches++;
        }

        double questionRatio = 0.0;
        if (nrOfSentences > 0) {
            questionRatio = (double) matches / nrOfSentences;
        }
        
        return Arrays.asList(new Feature(FN_QUESTION_RATIO, questionRatio));
    }
}