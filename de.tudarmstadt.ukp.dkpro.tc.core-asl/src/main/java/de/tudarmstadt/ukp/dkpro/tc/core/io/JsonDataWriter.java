package de.tudarmstadt.ukp.dkpro.tc.core.io;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

/**
 * Writes the feature store to a JSON file. Mainly used for testing purposes.
 * 
 * @author zesch
 * 
 */
public class JsonDataWriter
    implements DataWriter, Constants
{
    public static final String JSON_FILE_NAME = "fs.json";

    private Gson gson = new Gson();

    @Override
    public void write(File outputDirectory, FeatureStore featureStore, boolean useDenseInstances,
            String learningMode)
        throws Exception
    {
        FileUtils.writeStringToFile(new File(outputDirectory, JSON_FILE_NAME),
                gson.toJson(featureStore));
    }
}
