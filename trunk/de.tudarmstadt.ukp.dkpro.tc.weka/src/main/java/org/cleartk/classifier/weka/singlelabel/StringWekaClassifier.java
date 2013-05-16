package org.cleartk.classifier.weka.singlelabel;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;

import weka.classifiers.Classifier;

public class StringWekaClassifier extends AbstractWekaClassifier<String> {



	public StringWekaClassifier(Classifier wekaClassifier,
			FeaturesEncoder<Iterable<Feature>> featuresEncoder,
			OutcomeEncoder<String, String> outcomeEncoder) {
		super(wekaClassifier, featuresEncoder, outcomeEncoder);
	}

	@Override
	protected String decode(String classifyInstance) {
		return classifyInstance;
	}

}
