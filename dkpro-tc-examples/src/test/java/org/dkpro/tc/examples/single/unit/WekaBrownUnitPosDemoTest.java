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
package org.dkpro.tc.examples.single.unit;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.dkpro.lab.task.ParameterSpace;
import org.junit.Before;
import org.junit.Test;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.util.ReportConstants;
import org.dkpro.tc.examples.single.sequence.ContextMemoryReport;
import org.dkpro.tc.examples.single.unit.WekaBrownUnitPosDemo;
import org.dkpro.tc.examples.utils.JavaDemosTest_Base;
import org.dkpro.tc.weka.task.WekaTestTask;

/**
 * This test just ensures that the experiment runs without throwing
 * any exception.
 */
public class WekaBrownUnitPosDemoTest extends JavaDemosTest_Base
{
    WekaBrownUnitPosDemo javaExperiment;
    ParameterSpace pSpace;

    @Before
    public void setup()
        throws Exception
    {
        super.setup();
        
        javaExperiment = new WekaBrownUnitPosDemo();
        pSpace = WekaBrownUnitPosDemo.getParameterSpace();
    }

    @Test
    public void testJavaCrossValidation()
        throws Exception
    {
        javaExperiment.runCrossValidation(pSpace);
    }
    
    @Test
    public void testTrainTest()
        throws Exception
    {
        ContextMemoryReport.adapter = WekaTestTask.class.getName();
        javaExperiment.runTrainTest(pSpace);
        
        Properties p = new Properties();
        FileInputStream fs = new FileInputStream(new File(ContextMemoryReport.out, Constants.RESULTS_FILENAME));
        p.load(fs);
        fs.close();
        Double result = Double.valueOf(p.getProperty(ReportConstants.WGT_FMEASURE));
        assertEquals(0.0340, result, 0.0001);
        result = Double.valueOf(p.getProperty(ReportConstants.PCT_CORRECT));
        assertEquals(13.9175, result, 0.0001);
    }
}
