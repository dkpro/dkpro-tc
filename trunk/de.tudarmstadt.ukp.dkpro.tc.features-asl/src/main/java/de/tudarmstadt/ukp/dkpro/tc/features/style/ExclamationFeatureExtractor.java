package de.tudarmstadt.ukp.dkpro.tc.features.style;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

/**
 * Counts the ratio of number of sentences ending with exclamation(s) compared to all sentences.
 * Multiple exclamations in a row are considered as one exclamation sentence.
 * 
 */

public class ExclamationFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{

    public static final String FEATURE_NAME = "ExclamationRatio";

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {

        double sentences = JCasUtil.select(jcas, Sentence.class).size();
        String text = jcas.getDocumentText();

        Pattern p = Pattern.compile("\\!+");

        int matches = 0;
        Matcher m = p.matcher(text);
        while (m.find()) {
            matches++;
        }

        return Arrays.asList(new Feature(FEATURE_NAME, sentences > 0 ? (matches / sentences) : 0));
    }

}
