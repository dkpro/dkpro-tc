package de.tudarmstadt.ukp.dkpro.tc.demo.pairtwentynewsgroups;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/** 
 * This test only checks to see if the experiment runs without exceptions.
 * 
 * @author jamison
 *
 */
@Ignore
public class PairTwentyNewsgroupsExperimentTest
{
    PairTwentyNewsgroupsExperiment groovyExperiment;

    @Before
    public void setup()
        throws Exception
    {
        String path = "target/repository/" + getClass().getSimpleName() + "/"
                + name.getMethodName();
        System.setProperty("DKPRO_HOME", new File(path).getAbsolutePath());

        groovyExperiment = new PairTwentyNewsgroupsExperiment();
    }

    @Test
    public void testGroovyTrainTest()
        throws Exception
    {
        // Groovy setup with automatic task wiring
        groovyExperiment.runTrainTest();
    }



    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
