/*
 * Copyright 2017
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
 */

package org.dkpro.tc.evaluation.evaluator.measures.regression;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.dkpro.tc.evaluation.Id2Outcome;
import org.dkpro.tc.evaluation.SingleOutcome;
import org.dkpro.tc.evaluation.measures.regression.MeanAbsoluteError;
import org.dkpro.tc.evaluation.measures.regression.RootMeanSquaredError;
import org.junit.Test;
import org.mockito.Mockito;

public class RootMeanSquaredErrorTest
{

    @Test
    public void measureTest()
    {
        Id2Outcome id2 = Mockito.mock(Id2Outcome.class);

        double[] pred1 = new double[] { 3.1 };
        double[] gold1 = new double[] { 1.5 };
        SingleOutcome o1 = new SingleOutcome(gold1, pred1, Collections.emptyList(), "1");

        double[] pred2 = new double[] { .3 };
        double[] gold2 = new double[] { .6 };
        SingleOutcome o2 = new SingleOutcome(gold2, pred2, Collections.emptyList(), "2");

        Set<SingleOutcome> outcomes = new HashSet<>();
        outcomes.add(o1);
        outcomes.add(o2);

        Mockito.when(id2.getOutcomes()).thenReturn(outcomes);

        double result = RootMeanSquaredError.calculate(id2)
                .get(RootMeanSquaredError.class.getSimpleName());
        assertEquals(result, 1.1510, 0.0001);
    }
    
    @Test
    public void measurePerfectMatch()
    {
        Id2Outcome id2 = Mockito.mock(Id2Outcome.class);

        double[] pred1 = new double[] { 3.1 };
        double[] gold1 = new double[] { 3.1 };
        SingleOutcome o1 = new SingleOutcome(gold1, pred1, Collections.emptyList(), "1");

        double[] pred2 = new double[] { .6 };
        double[] gold2 = new double[] { .6 };
        SingleOutcome o2 = new SingleOutcome(gold2, pred2, Collections.emptyList(), "2");

        Set<SingleOutcome> outcomes = new HashSet<>();
        outcomes.add(o1);
        outcomes.add(o2);

        Mockito.when(id2.getOutcomes()).thenReturn(outcomes);

        double result = MeanAbsoluteError.calculate(id2)
                .get(MeanAbsoluteError.class.getSimpleName());
        assertEquals(result, 0.0, 0.00000001);
    }
}