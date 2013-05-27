package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor;

public class NGramPairFeatureExtractor
    extends NGramFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
        throws TextClassificationException
    {
        prefix = prefix + jcas.getViewName() + "_";

        return super.extract(jcas, focusAnnotation);
    }
}
