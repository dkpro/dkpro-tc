package org.dkpro.tc.examples.deeplearning.keras;

import static org.junit.Assert.assertEquals;

import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.evaluation.Id2Outcome;
import org.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import org.dkpro.tc.evaluation.evaluator.EvaluatorFactory;
import org.dkpro.tc.evaluation.measures.regression.MeanAbsoluteError;
import org.dkpro.tc.examples.single.sequence.ContextMemoryReport;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.ml.keras.KerasTestTask;
import org.junit.Test;

public class KerasTest
{
    @Test
    public void runTest()
        throws Exception
    {

        DemoUtils.setDkproHome(KerasDocumentRegression.class.getSimpleName());

        ContextMemoryReport.key = KerasTestTask.class.getName();

        boolean testConditon = true;
        try {
            ParameterSpace ps = KerasDocumentRegression.getParameterSpace();
            KerasDocumentRegression.runTrainTest(ps);
        }
        catch (Exception e) {
            //i.e. System is not setup with Python, Keras or any other dependency
            System.err.println("Exception occurred - will ignore test - : " + e.getMessage());
            testConditon = false;
        }

        if (testConditon) {
            Id2Outcome o = new Id2Outcome(ContextMemoryReport.id2outcome, Constants.LM_REGRESSION);
            EvaluatorBase createEvaluator = EvaluatorFactory.createEvaluator(o, true, false);
            Double result = createEvaluator.calculateEvaluationMeasures()
                    .get(MeanAbsoluteError.class.getSimpleName());
            assertEquals(0.825952, result, 0.0001);
        }
    }
}
