package de.tudarmstadt.ukp.dkpro.tc.demo.twentynewsgroups;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
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
public class TwentyNewsgroupsExperimentTest
{
    TwentyNewsgroupsWithoutJsonExperiment javaExperiment;
    TwentyNewsgroupsGroovyExperiment groovyExperiment;
    TwentyNewsgroupsGroovyExtendedExperiment groovyExtendedExperiment;
    ParameterSpace pSpace;

    @Before
    public void setup()
        throws Exception
    {
        String path = "target/repository/" + getClass().getSimpleName() + "/"
                + name.getMethodName();
        System.setProperty("DKPRO_HOME", new File(path).getAbsolutePath());

        javaExperiment = new TwentyNewsgroupsWithoutJsonExperiment();
        pSpace = TwentyNewsgroupsWithoutJsonExperiment.getParameterSpace();
        groovyExperiment = new TwentyNewsgroupsGroovyExperiment();
        groovyExtendedExperiment = new TwentyNewsgroupsGroovyExtendedExperiment();
    }

    @Test
    public void testGroovyCrossValidation()
        throws Exception
    {
        // Groovy setup with automatic task wiring
        groovyExperiment.runCrossValidation();
    }

    @Test
    public void testGroovyTrainTest()
        throws Exception
    {
        // Groovy setup with automatic task wiring
        groovyExperiment.runTrainTest();
    }

    @Ignore
    @Test
    public void testGroovyExtendedCrossValidation()
        throws Exception
    {
        // Groovy setup with manual task wiring
        groovyExtendedExperiment.runCrossValidation();
    }

    @Test
    public void testGroovyExtendedTrainTest()
        throws Exception
    {
        // Groovy setup with manual task wiring
        groovyExtendedExperiment.runTrainTest();
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
