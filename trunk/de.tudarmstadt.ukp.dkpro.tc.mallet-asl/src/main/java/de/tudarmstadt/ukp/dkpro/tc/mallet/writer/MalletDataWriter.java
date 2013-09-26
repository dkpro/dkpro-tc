package de.tudarmstadt.ukp.dkpro.tc.mallet.writer;

import java.io.File;

import de.tudarmstadt.ukp.dkpro.tc.api.features.InstanceList;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;

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
    public void write(File outputDirectory, InstanceList instanceList, boolean useDenseInstances, boolean isRegressionExperiment)
            throws Exception
    {
        //TODO implement
    }

}
