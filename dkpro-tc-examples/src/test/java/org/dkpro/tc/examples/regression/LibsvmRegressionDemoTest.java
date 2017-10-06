/**
 * Copyright 2017
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
import static org.junit.Assert.assertTrue;

import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.evaluation.Id2Outcome;
import org.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import org.dkpro.tc.evaluation.evaluator.EvaluatorFactory;
import org.dkpro.tc.evaluation.measures.regression.MeanAbsoluteError;
import org.dkpro.tc.evaluation.measures.regression.RootMeanSquaredError;
import org.dkpro.tc.examples.single.sequence.ContextMemoryReport;
import org.dkpro.tc.examples.utils.JavaDemosTest_Base;
import org.dkpro.tc.ml.libsvm.LibsvmTestTask;
import org.junit.Before;
import org.junit.Test;

/**
 * This test just ensures that the experiment runs without throwing
 * any exception.
 */
public class LibsvmRegressionDemoTest extends JavaDemosTest_Base
{
    ParameterSpace pSpace;
    LibsvmRegressionDemo experiment;
    
    @Before
    public void setup()
        throws Exception
    {
        super.setup();
        
        experiment = new LibsvmRegressionDemo();
        pSpace = LibsvmRegressionDemo.getParameterSpace();
    }

    
    @Test
    public void testTrainTest() throws Exception{
        ContextMemoryReport.key = LibsvmTestTask.class.getName();
        experiment.runTrainTest(pSpace);
        
        
        Id2Outcome o = new Id2Outcome(ContextMemoryReport.id2outcome, Constants.LM_REGRESSION);
        EvaluatorBase createEvaluator = EvaluatorFactory.createEvaluator(o, true, false);
        Double meanAbsoluteError = createEvaluator.calculateEvaluationMeasures().get(MeanAbsoluteError.class.getSimpleName());
        assertEquals(1.0897, meanAbsoluteError, 0.001);
        
        //we use a greater-as comparison as test as results are not stable between runs due to extremely few training
        Double rootMeanSquaredError = createEvaluator.calculateEvaluationMeasures().get(RootMeanSquaredError.class.getSimpleName());
        assertTrue(rootMeanSquaredError > 1.1);
    }
}
