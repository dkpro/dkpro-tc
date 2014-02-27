package de.tudarmstadt.ukp.dkpro.tc.testing;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.lab.engine.ExecutionException;


public class NumberOfFoldsTest
{
    @Test(expected = ExecutionException.class) 
    public void testNumberOfFolds()
        throws Exception
    {
        new NumberOfFoldsSetting().run();
    }
}