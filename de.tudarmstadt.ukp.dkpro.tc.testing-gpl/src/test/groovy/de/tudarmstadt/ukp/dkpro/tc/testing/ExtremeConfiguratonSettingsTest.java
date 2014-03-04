package de.tudarmstadt.ukp.dkpro.tc.testing;

import junit.framework.Assert;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.lab.engine.ExecutionException;

public class ExtremeConfiguratonSettingsTest
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

    @Test
    public void testExtremeValues_emptyPipelineparameters()
        throws Exception
    {
        new ExtremeConfigurationSettingsExperiment().runEmptyPipelineParameters();
    }

    @Test  
    public void testExtremeValues_emptyFeatureExtractorSet()
        throws Exception
    {
        try {
            new ExtremeConfigurationSettingsExperiment().runEmptyFeatureExtractorSet();
        }
        catch (ExecutionException e) {
            if (!ExceptionUtils.getRootCauseMessage(e).contains("No feature extractors have been added to the experiment.")) {
                Assert.fail("Unexpected exception");
            }
        }
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}