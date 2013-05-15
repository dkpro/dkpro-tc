package de.tudarmstadt.ukp.dkpro.tc.core.weka;

import java.io.IOException;

import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.jar.GenericDataWriterFactory;
import org.cleartk.classifier.weka.singlelabel.StringWekaDataWriter;
import org.cleartk.classifier.weka.singlelabel.StringWekaSerializedDataWriter;
import org.uimafit.descriptor.ConfigurationParameter;

public class DKProTCSingleLabelDataWriterFactory
    extends GenericDataWriterFactory<String>
    implements DataWriterFactory<String>
{

    /**
     * Defines whether to add instance ids to the arff files.
     */
    public static final String PARAM_ADD_INSTANCE_ID = "AddInstanceId";
    @ConfigurationParameter(name = PARAM_ADD_INSTANCE_ID, mandatory = true)
    private boolean addInstanceId;

    /**
	 * Defines whether to use the old fashioned WekaDataWriter that gathers all
	 * feature names and values in memory (default) or to use a the
	 * SerializedWekaDataWriter that stores intermediate values on disk.
	 */
    public static final String PARAM_FEATURES_IN_MEMORY = "featuresInMemory";
    @ConfigurationParameter(name = PARAM_FEATURES_IN_MEMORY, mandatory = true, defaultValue="true")
    private boolean featuresInMemory;

    @Override
    public DataWriter<String> createDataWriter()
        throws IOException
    {
        if (!featuresInMemory) {
            StringWekaSerializedDataWriter datawriter = new StringWekaSerializedDataWriter(
                    outputDirectory);
            // We need to use dense instances if we want to add a string id.
            // This is due to the long known bug in weka concerning string attributes with sparse
            // instances
            // (The string with offset 0 is not shown in the arff in sparse instances.)
            datawriter.setUseDenseInstances(addInstanceId);
            return datawriter;
        }
        else {
            StringWekaDataWriter datawriter = new StringWekaDataWriter(outputDirectory);
            datawriter.setUseDenseInstances(addInstanceId);
            return datawriter;
        }
    }
}