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
package org.dkpro.tc.examples.single.sequence;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.ml.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.ml.report.util.Tc2LtlabEvalConverter;
import org.junit.Before;
import org.junit.Test;

import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.Accuracy;

/**
 * This test just ensures that the experiment runs without throwing any exception.
 */
public class CRFSuiteBrownPosDemoTest
extends TestCaseSuperClass
{
    CRFSuiteBrownPosDemoSimpleDkproReader javaExperiment;

    @Override
    @Before
    public void setup()
        throws Exception
    {
        super.setup();
        javaExperiment = new CRFSuiteBrownPosDemoSimpleDkproReader();
    }

    @Test
    public void testJavaCrossValidation()
        throws Exception
    {
        //Number of iterations is set to an extreme low value (remove --> default: 100 iterations, or set accordingly)
        @SuppressWarnings("unchecked")
		Dimension<List<Object>> dimClassificationArgs = Dimension.create(Constants.DIM_CLASSIFICATION_ARGS,
				asList(new Object[] { new CRFSuiteAdapter(), CRFSuiteAdapter.ALGORITHM_LBFGS, "-p", "max_iterations=5"}));
        ParameterSpace pSpace = CRFSuiteBrownPosDemoSimpleDkproReader.getParameterSpace(
                Constants.FM_SEQUENCE, Constants.LM_SINGLE_LABEL, dimClassificationArgs, null);

        javaExperiment.runCrossValidation(pSpace);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTrainTest()
        throws Exception
    {
        // Random parameters for demonstration!
        Dimension<List<Object>> dimClassificationArgs = Dimension.create(
                Constants.DIM_CLASSIFICATION_ARGS,
                asList(CRFSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR));
        ParameterSpace pSpace = CRFSuiteBrownPosDemoSimpleDkproReader.getParameterSpace(
                Constants.FM_SEQUENCE, Constants.LM_SINGLE_LABEL, dimClassificationArgs, null);

        javaExperiment.runTrainTest(pSpace);

        
        EvaluationData<String> data = Tc2LtlabEvalConverter.convertSingleLabelModeId2Outcome(ContextMemoryReport.id2outcome);
        Accuracy<String> acc = new Accuracy<String>(data);
        
        Double resultNoParams = acc.getResult();

        // Random parameters for demonstration!
        dimClassificationArgs = Dimension.create(Constants.DIM_CLASSIFICATION_ARGS,
                asList(new CRFSuiteAdapter(), CRFSuiteAdapter.ALGORITHM_AVERAGED_PERCEPTRON, "-p",
                        "feature.minfreq=3", "-p", "max_iterations=3"));
        pSpace = CRFSuiteBrownPosDemoSimpleDkproReader.getParameterSpace(Constants.FM_SEQUENCE,
                Constants.LM_SINGLE_LABEL, dimClassificationArgs, null);

        javaExperiment.runTrainTest(pSpace);
        
        data = Tc2LtlabEvalConverter.convertSingleLabelModeId2Outcome(ContextMemoryReport.id2outcome);
        acc = new Accuracy<String>(data);
        Double resultParams = acc.getResult();

        // if the parameters work something should change in the end result
        assertTrue(Math.abs(resultNoParams - resultParams) > 0.00001);
    }
}
