package org.cleartk.classifier.weka.singlelabel;

import java.io.File;
import java.io.IOException;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.encoder.outcome.BooleanToStringOutcomeEncoder;

public class BinaryWekaDataWriter extends AbstractWekaDataWriter<Boolean> {

	public BinaryWekaDataWriter(File outputDirectory) throws IOException {
		super(outputDirectory);
		this.setOutcomeEncoder(new BooleanToStringOutcomeEncoder());
	}

	public BinaryWekaDataWriter(File outputDirectory, String relationTag)
			throws IOException {
		super(outputDirectory, relationTag);
		this.setOutcomeEncoder(new BooleanToStringOutcomeEncoder());
	}

	@Override
	protected BinaryWekaClassifierBuilder newClassifierBuilder() {
		return new BinaryWekaClassifierBuilder();
	}

	@Override
	public void write(Instance<Boolean> instance)
			throws CleartkProcessingException {
		super.write(instance);
	}

}
