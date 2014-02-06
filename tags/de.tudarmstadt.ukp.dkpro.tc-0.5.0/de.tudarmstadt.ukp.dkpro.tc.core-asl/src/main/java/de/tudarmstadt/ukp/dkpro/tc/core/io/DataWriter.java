package de.tudarmstadt.ukp.dkpro.tc.core.io;

import java.io.File;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;

/**
 * Interface for data writers that write instances in the representation format used by machine learning tools.
 * 
 * @author zesch
 *
 */
public interface DataWriter
{
    public void write(File outputDirectory, FeatureStore featureStore, boolean useDenseInstances, boolean isRegressionExperiment)
        throws Exception;
}
