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
package org.dkpro.tc.examples.single.sequence;


import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.utils.JavaDemosTest_Base;
import org.dkpro.tc.svmhmm.SVMHMMAdapter;
import org.dkpro.tc.svmhmm.random.RandomSVMHMMAdapter;
import org.dkpro.tc.svmhmm.task.SVMHMMTestTask;
import org.junit.Before;
import org.junit.Test;

/**
 * This test just ensures that the experiment runs without throwing
 * any exception.
 */
public class SVMHMMBrownPosDemoTest extends JavaDemosTest_Base
{
    SVMHMMBrownPosDemo javaExperiment;
    ParameterSpace pSpace;

    @Before
    public void setup()
        throws Exception
    {
        super.setup();
        
        javaExperiment = new SVMHMMBrownPosDemo();
    }

    @Test
    public void testRandomSVMHMM()
        throws Exception
    {
        ContextMemoryReport.adapter = SVMHMMTestTask.class.getName();
        pSpace = SVMHMMBrownPosDemo.getParameterSpace(true);
        javaExperiment.runTrainTest(pSpace, RandomSVMHMMAdapter.class);
    }

    @Test
    public void testActualSVMHMM()
        throws Exception
    {
        ContextMemoryReport.adapter = SVMHMMTestTask.class.getName();
        
        pSpace = SVMHMMBrownPosDemo.getParameterSpace(true);
        javaExperiment.runTrainTest(pSpace, SVMHMMAdapter.class);
        
        String fileContent = FileUtils.readFileToString(new File(ContextMemoryReport.out, Constants.RESULTS_FILENAME));
        String beg = "Macro F-measure: ";
        String end = ",";
        
        int s = fileContent.indexOf(beg) + beg.length();
        int e = fileContent.indexOf(end);
        
        Double result = Double.valueOf(fileContent.substring(s, e));
        assertEquals(0.346, result, 0.0000001);
    }
}
