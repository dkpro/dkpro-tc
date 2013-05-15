package org.cleartk.classifier.weka.singlelabel;

import org.cleartk.classifier.encoder.outcome.BooleanToStringOutcomeEncoder;

public class BinaryWekaClassifierBuilder extends
		AbstractWekaClassifierBuilder<Boolean> {

	public BinaryWekaClassifierBuilder() {
		super();
		this.setOutcomeEncoder(new BooleanToStringOutcomeEncoder());
	}

	@Override
	protected BinaryWekaClassifier newClassifier() {
		return new BinaryWekaClassifier(this.classifier,this.featuresEncoder,this.outcomeEncoder);
	}

}
