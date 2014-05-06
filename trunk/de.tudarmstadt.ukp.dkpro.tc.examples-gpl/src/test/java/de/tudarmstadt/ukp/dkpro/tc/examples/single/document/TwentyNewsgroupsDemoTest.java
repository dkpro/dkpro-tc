package de.tudarmstadt.ukp.dkpro.tc.examples.single.document;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;

/**
 * This is not exactly a unit test (yet). It just ensures that the experiments run without throwing
 * any exception. Additional unit tests should test the inner workings of the experiments
 * 
 * @author Oliver Ferschke
 * 
 */
public class TwentyNewsgroupsDemoTest
{
    TwentyNewsgroupsDemo javaExperiment;
    ParameterSpace pSpace;

    @Before
    public void setupLogging()
    {
        System.setProperty("org.apache.uima.logger.class", "org.apache.uima.util.impl.Log4jLogger_impl");
    }
    
    @Before
    public void setup()
        throws Exception
    {
        String path = "target/repository/" + getClass().getSimpleName() + "/"
                + name.getMethodName();
        System.setProperty("DKPRO_HOME", new File(path).getAbsolutePath());

        javaExperiment = new TwentyNewsgroupsDemo();
        pSpace = TwentyNewsgroupsDemo.getParameterSpace();
    }

    @Test
    public void testJavaCrossValidation()
        throws Exception
    {
        // Java setup with automatic task wiring
        javaExperiment.runCrossValidation(pSpace);
    }

    @Test
    public void testJavaTrainTest()
        throws Exception
    {
        // Java setup with automatic task wiring
        javaExperiment.runTrainTest(pSpace);
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
