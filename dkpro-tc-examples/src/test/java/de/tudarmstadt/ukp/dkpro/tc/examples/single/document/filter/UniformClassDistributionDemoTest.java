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
package de.tudarmstadt.ukp.dkpro.tc.examples.single.document.filter;

import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.examples.utils.JavaDemosTest_Base;

/**
 * This test just ensures that the experiment runs without throwing
 * any exception.
 * 
 * @author Oliver Ferschke, Emily Jamison
 * 
 */
public class UniformClassDistributionDemoTest extends JavaDemosTest_Base
{
    UniformClassDistributionDemo javaExperiment;
    ParameterSpace pSpace;
    
    @Before
    public void setup()
        throws Exception
    {
        super.setup();
        
        javaExperiment = new UniformClassDistributionDemo();
        pSpace = UniformClassDistributionDemo.getParameterSpace();
    }

    @Test
    public void testJavaTrainTest()
        throws Exception
    {
        javaExperiment.runTrainTest(pSpace);
    }

    @Test
    public void testJavaCrossValidation()
        throws Exception
    {
        javaExperiment.runCrossValidation(pSpace);
    }
}
