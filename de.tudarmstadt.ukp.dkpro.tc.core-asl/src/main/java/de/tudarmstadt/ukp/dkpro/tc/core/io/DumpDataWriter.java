package de.tudarmstadt.ukp.dkpro.tc.core.io;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

/**
 * Dumps the feature store to a human readable file and also to the console. Mainly used for testing
 * purposes.
 * 
 * @author zesch
 * 
 */
public class DumpDataWriter
    implements DataWriter, Constants
{

    /**
     * Public name of the dump file
     */
    public static final String DUMP_FILE_NAME = "fs.ser";

    @Override
    public void write(File outputDirectory, FeatureStore featureStore, boolean useDenseInstances,
            String learningMode)
        throws Exception
    {
        StringBuilder sb = new StringBuilder();
        sb.append(featureStore.getNumberOfInstances());
        sb.append("\n");
        sb.append(StringUtils.join(featureStore.getUniqueOutcomes(), ", "));
        sb.append("\n");
        for (Instance instance : featureStore.getInstances()) {
            sb.append(instance.toString());
        }
        System.out.println(sb.toString());
        FileUtils.writeStringToFile(new File(outputDirectory, DUMP_FILE_NAME), sb.toString());
    }
}
