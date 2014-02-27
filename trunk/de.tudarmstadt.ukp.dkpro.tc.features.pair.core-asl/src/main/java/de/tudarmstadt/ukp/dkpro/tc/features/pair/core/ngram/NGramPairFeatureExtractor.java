package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.FrequencyDistributionNGramDFE;

/**
 * Pair-wise feature extractor Returns the ngrams in a view with the view name as prefix
 * 
 * @author nico.erbs@gmail.com
 * @author daxenberger
 * 
 */
@Deprecated
public class NGramPairFeatureExtractor
    extends FrequencyDistributionNGramDFE
    implements PairFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        List<Feature> features = new ArrayList<Feature>();

        prefix = "ngrams_" + view1.getViewName() + "_";
        features.addAll(super.extract(view1));
        prefix = "ngrams_" + view2.getViewName() + "_";
        features.addAll(super.extract(view2));
        return features;
    }

    protected void setStopwords(Set<String> newStopwords)
    {
        stopwords = newStopwords;
    }

    protected void makeTopKSet(FrequencyDistribution<String> topK)
    {
        topKSet = topK;
    }
}
