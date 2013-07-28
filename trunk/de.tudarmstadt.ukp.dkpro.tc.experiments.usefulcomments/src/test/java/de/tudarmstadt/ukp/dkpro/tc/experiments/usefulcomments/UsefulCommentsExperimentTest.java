package de.tudarmstadt.ukp.dkpro.tc.experiments.usefulcomments;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.experiments.usefulcomments.UsefulCommentsExperiment;

/**
 * This is not exactly a unit test (yet). It just ensures that the experiments run without throwing
 * any exception. Additional unit tests should test the inner workings of the experiments
 * 
 * @author Oliver Ferschke
 * 
 */
public class UsefulCommentsExperimentTest
{
    UsefulCommentsExperiment javaExperiment;
    ParameterSpace pSpace;

    @Before
    public void setup()
        throws Exception
    {
        String path = "target/repository/" + getClass().getSimpleName() + "/"
                + name.getMethodName();
        System.setProperty("DKPRO_HOME", new File(path).getAbsolutePath());

        javaExperiment = new UsefulCommentsExperiment();
        pSpace = javaExperiment.setup();
    }

    @Test
    @Ignore
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
