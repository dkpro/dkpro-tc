package de.tudarmstadt.ukp.dkpro.tc.testing;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.TestName;

public class ManyInstancesTest
{
    @Before
    public void setupLogging()
    {
        System.setProperty("org.apache.uima.logger.class", "org.apache.uima.util.impl.Log4jLogger_impl");
    }

    @Before
    public void setupWorkingDirectory()
    {
        System.setProperty("DKPRO_HOME", "target/dkpro_home");
    }

    @Ignore
    public void testManyInstances()
        throws Exception
    {
        new ManyInstancesExperiment().run();
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}