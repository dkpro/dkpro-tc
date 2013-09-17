package de.tudarmstadt.ukp.dkpro.tc.weka.writer;

import java.io.File;

import de.tudarmstadt.ukp.dkpro.tc.api.features.InstanceList;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

public class WekaDataWriter
    implements DataWriter
{

    // FIXME should this name be fixed somewhere else?
    public static final String arffFileName = "training-data.arff.gz";
    
    @Override
    public void write(File outputDirectory, InstanceList instanceList, boolean useDenseInstances, boolean isRegressionExperiment)
            throws Exception
    {
        WekaUtils.instanceListToArffFile(new File(outputDirectory, arffFileName), instanceList, useDenseInstances, isRegressionExperiment);
    }

}
