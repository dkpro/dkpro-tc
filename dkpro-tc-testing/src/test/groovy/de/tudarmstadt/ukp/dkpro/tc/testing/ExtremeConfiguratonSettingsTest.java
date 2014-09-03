/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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