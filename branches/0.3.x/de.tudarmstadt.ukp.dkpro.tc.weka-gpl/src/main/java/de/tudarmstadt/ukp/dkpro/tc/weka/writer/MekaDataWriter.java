package de.tudarmstadt.ukp.dkpro.tc.weka.writer;

import java.io.File;

import de.tudarmstadt.ukp.dkpro.tc.api.features.InstanceList;
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.DataWriter;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

public class MekaDataWriter
    implements DataWriter
{

    // FIXME should this name be fixed somewhere else?
    public static final String arffFileName = "training-data.arff.gz";
    
    @Override
    public void write(File outputDirectory, InstanceList instanceList, boolean useDenseInstances, boolean isRegressionExperiment)
            throws Exception
    {
        // TODO for multi-label isRegressionExperiment is currently ignored. I am not sure this makes sense in this setting at all. 
        
        WekaUtils.instanceListToArffFileMultiLabel(new File(outputDirectory, arffFileName), instanceList, useDenseInstances);
    }

}
