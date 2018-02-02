/**
 * Copyright 2018
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
package org.dkpro.tc.examples.regression;

import static org.junit.Assert.assertEquals;

import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.ml.liblinear.LiblinearTestTask;
import org.dkpro.tc.ml.report.util.Tc2LtlabEvalConverter;
import org.junit.Before;
import org.junit.Test;

import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.regression.MeanAbsoluteError;

/**
 * This test just ensures that the experiment runs without throwing
 * any exception.
 */
public class LiblinearRegressionDemoTest extends TestCaseSuperClass
{
    ParameterSpace pSpace;
    LiblinearRegressionDemo experiment;
    
    @Before
    public void setup()
        throws Exception
    {
        super.setup();
        
        experiment = new LiblinearRegressionDemo();
        pSpace = LiblinearRegressionDemo.getParameterSpace();
    }


    @Test
    public void testTrainTest() throws Exception{
        ContextMemoryReport.key = LiblinearTestTask.class.getName();
        experiment.runTrainTest(pSpace);
        
        
        EvaluationData<Double> data = Tc2LtlabEvalConverter.convertRegressionModeId2Outcome(ContextMemoryReport.id2outcome);
		MeanAbsoluteError mae = new MeanAbsoluteError(data);
        assertEquals(0.54, mae.getResult(), 0.00001);
        
    }
}
