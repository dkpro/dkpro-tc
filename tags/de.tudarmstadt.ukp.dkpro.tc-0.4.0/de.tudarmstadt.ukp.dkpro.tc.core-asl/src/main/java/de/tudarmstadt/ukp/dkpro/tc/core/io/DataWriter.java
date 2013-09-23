package de.tudarmstadt.ukp.dkpro.tc.core.io;

import java.io.File;

import de.tudarmstadt.ukp.dkpro.tc.api.features.InstanceList;

/**
 * Interface for data writers that write instances in the representation format used by machine learning tools.
 * 
 * @author zesch
 *
 */
public interface DataWriter
{
    public void write(File outputDirectory, InstanceList instanceList, boolean useDenseInstances, boolean isRegressionExperiment)
        throws Exception;
}
