package de.tudarmstadt.ukp.dkpro.tc.core.io;

import java.io.File;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;

/**
 * Interface for data writers that write instances in the representation format used by machine
 * learning tools.
 * 
 * @author zesch
 * 
 */
public interface DataWriter
{
    /**
     * Write the contents of the feature store to the output directory.
     * 
     * @param outputDirectory
     * @param featureStore
     * @param useDenseInstances
     * @param learningMode
     * @throws Exception
     */
    public void write(File outputDirectory, FeatureStore featureStore, boolean useDenseInstances,
            String learningMode)
        throws Exception;
}
