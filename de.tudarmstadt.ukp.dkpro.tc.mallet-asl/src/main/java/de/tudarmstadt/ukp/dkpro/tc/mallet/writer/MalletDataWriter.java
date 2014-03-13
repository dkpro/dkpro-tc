package de.tudarmstadt.ukp.dkpro.tc.mallet.writer;

import java.io.File;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;
import de.tudarmstadt.ukp.dkpro.tc.mallet.util.MalletUtils;

/**
 * {@link DataWriter} for the Mallet machine learning tool.
 * 
 * @author Oliver Ferschke
 * 
 */
public class MalletDataWriter
    implements DataWriter, Constants
{

    @Override
    public void write(File outputDirectory, FeatureStore featureStore, boolean useDenseInstances,
            String learningMode)
        throws Exception
    {

        // TODO add generic filename to Constants
        MalletUtils.instanceListToMalletFormatFile(new File(outputDirectory + "/"
                + "training-data.txt.gz"), featureStore, useDenseInstances);
    }
}
