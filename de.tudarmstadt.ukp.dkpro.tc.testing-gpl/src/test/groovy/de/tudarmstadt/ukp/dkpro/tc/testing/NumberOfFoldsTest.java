package de.tudarmstadt.ukp.dkpro.tc.testing;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.lab.engine.ExecutionException;

public class NumberOfFoldsTest
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

    @Test(expected = ExecutionException.class) 
    public void testNumberOfFolds()
        throws Exception
    {
        new NumberOfFoldsSetting().run();
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}