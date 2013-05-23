package de.tudarmstadt.ukp.dkpro.tc.features.length;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

public class NrOfSentencesFeatureExtractor
    implements FeatureExtractor
{

    public static final String FN_NR_OF_SENTENCES = "NrofSentences";

    @Override
    public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
        throws TextClassificationException
    {

        if (focusAnnotation == null) {
            return Arrays.asList(new Feature(FN_NR_OF_SENTENCES, JCasUtil.select(jcas, Sentence.class).size()));
        }
        else {
            return Arrays.asList(new Feature(FN_NR_OF_SENTENCES, JCasUtil.selectCovered(jcas, Sentence.class, focusAnnotation).size()));
        }
    }
}
