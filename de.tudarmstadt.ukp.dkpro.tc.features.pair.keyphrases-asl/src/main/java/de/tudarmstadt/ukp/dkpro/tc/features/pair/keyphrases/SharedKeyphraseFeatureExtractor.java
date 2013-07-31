package de.tudarmstadt.ukp.dkpro.tc.features.pair.keyphrases;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

public class SharedKeyphraseFeatureExtractor extends
		AbstractKeyphraseFeatureExtractor {

	private int number;

	public SharedKeyphraseFeatureExtractor(int number){
		this.number = number;
	}
	
    @Override
    public List<de.tudarmstadt.ukp.dkpro.tc.api.features.Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {

        return Arrays.asList(
                new Feature("SharedKeyphrases_" + number, 
                        (
                                Collections.disjoint(getKeyphrases(view1, number),
                                        getKeyphrases(view2, number))
                )));
    }
}
