package de.tudarmstadt.ukp.dkpro.tc.testing;

import org.junit.Test;

public class ManyInstancesTest
{
    @Test
    public void testManyInstances()
        throws Exception
    {
        new ManyInstancesExperiment().run();
    }
}