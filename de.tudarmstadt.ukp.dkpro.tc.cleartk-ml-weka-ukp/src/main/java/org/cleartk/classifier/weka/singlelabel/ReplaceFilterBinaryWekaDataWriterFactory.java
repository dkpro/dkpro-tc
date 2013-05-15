package org.cleartk.classifier.weka.singlelabel;

import java.io.IOException;

import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.weka.ReplaceMissingValuesWithZeroFilter;

public class ReplaceFilterBinaryWekaDataWriterFactory extends
		DefaultBinaryWekaDataWriterFactory {

	@Override
	public DataWriter<Boolean> createDataWriter() throws IOException {
		AbstractWekaDataWriter<Boolean> datawriter = (AbstractWekaDataWriter<Boolean>) super.createDataWriter();
		datawriter.setPreprocessingFilter(new ReplaceMissingValuesWithZeroFilter());
		return datawriter;
	}

}
