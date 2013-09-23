package de.tudarmstadt.ukp.dkpro.tc.weka.writer;

import java.io.File;

import de.tudarmstadt.ukp.dkpro.tc.api.features.InstanceList;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

/**
 * {@link DataWriter} for the Meka machine learning tool.
 * 
 * @author Johannes Daxenberger
 *
 */
public class MekaDataWriter
    implements DataWriter, Constants
{
    
    @Override
    public void write(File outputDirectory, InstanceList instanceList, boolean useDenseInstances, boolean isRegressionExperiment)
            throws Exception
    {
        // TODO for multi-label isRegressionExperiment is currently ignored. I am not sure this makes sense in this setting at all. 
        
        WekaUtils.instanceListToArffFileMultiLabel(new File(outputDirectory, ARFF_FILENAME), instanceList, useDenseInstances);
    }

}
