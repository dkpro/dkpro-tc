package de.tudarmstadt.ukp.dkpro.tc.features.pair.core;

import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

/**
 * The interface for all pair features
 * @author nico.erbs@gmail.com
 *
 */
public interface PairFeatureExtractor
{
	/**
	 * 
	 * @param view1 First view to be processed
	 * @param view2 Second view to be processed
	 * @return The list of extracted features
	 * @throws TextClassificationException
	 */
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException;
}
