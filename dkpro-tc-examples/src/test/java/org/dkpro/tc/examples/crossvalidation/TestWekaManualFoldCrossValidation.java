package org.dkpro.tc.examples.crossvalidation;

import org.dkpro.lab.engine.ExecutionException;
import org.junit.Test;

public class TestWekaManualFoldCrossValidation
{
    /*
     * We request more folds than we have files (2) because we set 'manualMode' to true we expect an exception 
     */
    @Test(expected=ExecutionException.class)
    public void testManualFoldCrossValdiationException() throws Exception{
        WekaManualFoldCrossValidation javaExperiment = new WekaManualFoldCrossValidation();
        javaExperiment.runCrossValidation(WekaManualFoldCrossValidation.getParameterSpace(true), 3);
    }
    
    /*
     * We request more folds than we have files (2) without 'manualModel' thus the CAS should be split up and create sufficient many CAS to be distributed into the fold 
     */
    @Test 
    public void testManualFoldCrossValdiation() throws Exception{
        WekaManualFoldCrossValidation javaExperiment = new WekaManualFoldCrossValidation();
        javaExperiment.runCrossValidation(WekaManualFoldCrossValidation.getParameterSpace(false), 3);
    }

}
