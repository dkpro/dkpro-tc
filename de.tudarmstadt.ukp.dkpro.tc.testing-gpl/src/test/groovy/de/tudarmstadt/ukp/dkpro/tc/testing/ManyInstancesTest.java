package de.tudarmstadt.ukp.dkpro.tc.testing;

import org.junit.Ignore;
import org.junit.Test;

public class ManyInstancesTest
{
    @Ignore
    @Test
    public void testManyInstances()
        throws Exception
    {
        new ManyInstancesExperiment().run();
    }
}