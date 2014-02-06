package de.tudarmstadt.ukp.dkpro.tc.demo.sentimentpolarity;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.tc.core.ExperimentStarter;

/**
 * This is not exactly a unit test (yet). It just ensures that the experiments run without throwing
 * any exception.
 * 
 * @author Oliver Ferschke
 * @author Artem Vovk
 * 
 */
public class SentimentPolarityExperimentTest
{

    @Before
    public void setup()
        throws Exception
    {
        String path = "target/repository/" + getClass().getSimpleName() + "/"
                + name.getMethodName();
        System.setProperty("DKPRO_HOME", new File(path).getAbsolutePath());
    }

    @Test
    public void testGroovyExperiment()
        throws Exception
    {
        ExperimentStarter.start("scripts/SentimentPolarityGroovyExperiment.groovy");
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
