package de.tudarmstadt.ukp.dkpro.tc.features.pair.keyphrases;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;

public class SharedKeyphraseFeatureExtractor extends
		AbstractKeyphraseFeatureExtractor {

	private int number;

	public SharedKeyphraseFeatureExtractor(int number){
		this.number = number;
	}
	
	@Override
	public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
			throws CleartkExtractorException
			{
		try {
			return Arrays.asList(
					new Feature("SharedKeyphrases_" + number, 
							(
									Collections.disjoint(getKeyphrases(jcas.getView(PART_ONE), number),
											getKeyphrases(jcas.getView(PART_ONE), number))
					)));
		} catch (CASException e) {
			throw new CleartkExtractorException(e);
		}
			}


}
