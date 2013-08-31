package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

/**
 * Pair-wise feature extractor
 * Returns the ngrams in a view with the view name as prefix
 * @author nico.erbs@gmail.com
 *
 */
public class NGramPairFeatureExtractor
    extends NGramFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {
        prefix = prefix + jcas.getViewName() + "_";

        return super.extract(jcas, classificationUnit);
    }
}
