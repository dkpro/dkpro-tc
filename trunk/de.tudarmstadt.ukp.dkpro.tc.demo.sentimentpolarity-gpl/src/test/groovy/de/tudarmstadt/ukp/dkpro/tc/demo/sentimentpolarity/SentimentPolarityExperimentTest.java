package de.tudarmstadt.ukp.dkpro.tc.demo.sentimentpolarity;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * This is not exactly a unit test (yet). It just ensures that the experiments run without throwing
 * any exception. Additional unit tests should test the inner workings of the experiments
 * 
 * @author Oliver Ferschke
 * 
 */
public class SentimentPolarityExperimentTest
{
    SentimentPolarityGroovyExperiment experiment;

    @Before
    public void setup()
        throws Exception
    {
        String path = "target/repository/" + getClass().getSimpleName() + "/"
                + name.getMethodName();
        System.setProperty("DKPRO_HOME", new File(path).getAbsolutePath());

        experiment = new SentimentPolarityGroovyExperiment();
    }

    @Test
    public void testGroovyTrainTest()
        throws Exception
    {
        // Groovy setup with automatic task wiring
        experiment.runTrainTest();
    }

    @Test
    public void testGroovyCrossValidation()
        throws Exception
    {
        // Groovy setup with automatic task wiring
        experiment.runCrossValidation();
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
