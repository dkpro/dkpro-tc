package de.tudarmstadt.ukp.dkpro.tc.groovyexamples.single.document;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.tc.groovyexamples.single.document.TwentyNewsgroupsDemo;
import de.tudarmstadt.ukp.dkpro.tc.groovyexamples.single.document.TwentyNewsgroupsDemoExtended;

/**
 * This is not exactly a unit test (yet). It just ensures that the experiments run without throwing
 * any exception. Additional unit tests should test the inner workings of the experiments
 * 
 * @author Oliver Ferschke
 * 
 */
public class TwentyNewsgroupsDemoTest
{
    TwentyNewsgroupsDemo groovyExperiment;
    TwentyNewsgroupsDemoExtended groovyExtendedExperiment;

    @Before
    public void setup()
        throws Exception
    {
        String path = "target/repository/" + getClass().getSimpleName() + "/"
                + name.getMethodName();
        System.setProperty("DKPRO_HOME", new File(path).getAbsolutePath());

        groovyExperiment = new TwentyNewsgroupsDemo();
        groovyExtendedExperiment = new TwentyNewsgroupsDemoExtended();
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

    @Test
    public void testGroovyExtendedTrainTest()
        throws Exception
    {
        // Groovy setup with manual task wiring
        groovyExtendedExperiment.runTrainTest();
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
