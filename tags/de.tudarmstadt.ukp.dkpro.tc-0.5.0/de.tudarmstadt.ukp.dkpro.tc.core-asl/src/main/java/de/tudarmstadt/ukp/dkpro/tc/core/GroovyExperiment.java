package de.tudarmstadt.ukp.dkpro.tc.core;

/**
 * This interface enforces Groovy experiment scripts to have the run method. This is important for
 * the ExperimentStarter class.
 * 
 * @author Artem Vovk
 * 
 */
public interface GroovyExperiment
{
    /**
     * Start the Groovy experiment.
     */
    public void run();
}
