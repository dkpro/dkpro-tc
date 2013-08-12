package de.tudarmstadt.ukp.dkpro.tc.testing;

import junit.framework.Assert;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.lab.engine.ExecutionException;

public class ExtremeConfiguratonSettingsTest
{
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
}