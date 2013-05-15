package de.tudarmstadt.ukp.dkpro.tc.features.pair.core;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;

// FIXME this seems to be named a bit misleading
public class BaselinePairFeatureExtractor
implements SimpleFeatureExtractor
{

	@Override
	public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
			throws CleartkExtractorException
			{
		return Arrays.asList(
				new Feature("Baseline", 
						0 ));
			}
}
