/**
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.tc.groovyexamples.single.document;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import org.dkpro.tc.groovyexamples.utils.GroovyDemosTest_Base;

/**
 * This test just ensures that the experiment runs without throwing
 * any exception.
 */
public class TwitterSentimentDemoTest extends GroovyDemosTest_Base
{
    TwitterSentimentDemo groovyExperiment;

    @Before
    public void setup()
        throws Exception
    {
        super.setup();

        groovyExperiment = new TwitterSentimentDemo();
    }

    @Test
    public void testGroovyTrainTest()
        throws Exception
    {
        groovyExperiment.runTrainTest();
    }
    
    @Test
    public void testGroovyCV()
        throws Exception
    {
        groovyExperiment.runCrossValidation();
    }
}
