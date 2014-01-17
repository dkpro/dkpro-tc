package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor;

/**
 * Pair-wise feature extractor
 * Returns all combinations of qualified ngrams from each of the two views.
 * An ngram-pair's value is 1 if each of the ngrams appeared in their 
 * respective text, and 0 otherwise.
 * @author Emily Jamison
 *
 */
public class CombinedNGramPairFeatureExtractor
    extends NGramFeatureExtractor implements
    PairFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        List<Feature> features = new ArrayList<Feature>();
        
        prefix = new String("");
        
        List<Feature> view1Ngrams = super.extract(view1, null);
        List<Feature> view2Ngrams = super.extract(view2, null);
        
        for(Feature ngram1: view1Ngrams){
        	for(Feature ngram2: view2Ngrams){
        		String featureName = "comboNg"+ngram1.getName()+ngram2.getName();
        		Object featureValue = 0;
        		if(ngram1.getValue().toString().equals("1") && ngram2.getValue().toString().equals("1")){
        			featureValue = 1;
        		}
        		
        		System.out.println("New pair ngram: " + featureName + "  featureValue: " + featureValue);
        		features.add(new Feature(featureName, featureValue));
        	}
        }
        return features;
    }
}