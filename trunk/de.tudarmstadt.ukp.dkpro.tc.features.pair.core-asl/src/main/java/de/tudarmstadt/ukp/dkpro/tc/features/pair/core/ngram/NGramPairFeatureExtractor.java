package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.features.IFeature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor;

/**
 * Pair-wise feature extractor
 * Returns the ngrams in a view with the view name as prefix
 * @author nico.erbs@gmail.com
 * @author daxenberger
 *
 */
public class NGramPairFeatureExtractor
    extends NGramFeatureExtractor implements
    PairFeatureExtractor
{

    @Override
    public List<IFeature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        List<IFeature> features = new ArrayList<IFeature>();

        prefix = new String("ngrams_" + view1.getViewName() + "_");
        features.addAll(super.extract(view1, null));
        prefix = new String("ngrams_" + view2.getViewName() + "_");
        features.addAll(super.extract(view2, null));
        return features;
    }
}
