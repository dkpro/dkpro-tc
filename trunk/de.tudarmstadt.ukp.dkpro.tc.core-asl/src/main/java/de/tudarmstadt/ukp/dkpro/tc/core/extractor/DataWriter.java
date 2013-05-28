package de.tudarmstadt.ukp.dkpro.tc.core.extractor;

import java.io.File;

import de.tudarmstadt.ukp.dkpro.tc.api.features.InstanceList;

public interface DataWriter
{
    public void write(File outputDirectory, InstanceList instanceList, boolean useDenseInstances, boolean isRegressionExperiment)
        throws Exception;
}
