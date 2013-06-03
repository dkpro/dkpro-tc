package de.tudarmstadt.ukp.dkpro.tc.features.pair.core;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

// FIXME this seems to be named a bit misleading
public class BaselinePairFeatureExtractor
implements PairFeatureExtractor
{

	@Override
	public List<Feature> extract(JCas view1, JCas view2)
			throws TextClassificationException {
		return Arrays.asList(
				new Feature("Baseline",
						0));
	}

}
