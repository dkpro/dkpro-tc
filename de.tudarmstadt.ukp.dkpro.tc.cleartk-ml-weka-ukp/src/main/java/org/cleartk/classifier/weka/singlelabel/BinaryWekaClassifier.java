package org.cleartk.classifier.weka.singlelabel;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;

import weka.classifiers.Classifier;

public class BinaryWekaClassifier extends AbstractWekaClassifier<Boolean> {



	public BinaryWekaClassifier(Classifier wekaClassifier,
			FeaturesEncoder<Iterable<Feature>> featuresEncoder,
			OutcomeEncoder<Boolean, String> outcomeEncoder) {
		super(wekaClassifier, featuresEncoder, outcomeEncoder);
	}

	@Override
	protected Boolean decode(String classifyInstance) {
		return Boolean.valueOf(classifyInstance);
//		return classifyInstance > 0.1;
	}

}
