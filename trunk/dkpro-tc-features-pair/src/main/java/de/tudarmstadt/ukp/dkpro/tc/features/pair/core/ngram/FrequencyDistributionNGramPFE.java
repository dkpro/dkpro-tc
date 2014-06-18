package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.FrequencyDistributionNGramDFE;

/**
 * Pair-wise feature extractor Returns the ngrams in a view with the view name as prefix
 * 
 * @author nico.erbs@gmail.com
 * @author daxenberger
 * 
 */
@Deprecated
public class FrequencyDistributionNGramPFE
    extends FrequencyDistributionNGramDFE
    implements PairFeatureExtractor
{
	
	String viewPrefix;

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        List<Feature> features = new ArrayList<Feature>();

        viewPrefix = "ngrams_" + view1.getViewName();
        features.addAll(super.extract(view1));
        viewPrefix = "ngrams_" + view2.getViewName();
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
    
    @Override
    protected String getFeaturePrefix()
    {
    	return viewPrefix;
    }
}
