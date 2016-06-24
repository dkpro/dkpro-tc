/*
 * Copyright 2016
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

import org.dkpro.tc.evaluation.Id2Outcome;
import org.dkpro.tc.evaluation.SingleOutcome;
import org.dkpro.tc.evaluation.measures.regression.SpearmanCorrelation;
import org.junit.Test;

public class SpearmanCorrelationTest
{

    @Test
    public void measureTest()
    {
        Id2Outcome id2 = new Id2Outcome();

        id2.addOutcome(new SingleOutcome(
        		new double[] { 1.0 },
        		new double[] { 2.0 },
        		Collections.emptyList(), "1"
        ));
        id2.addOutcome(new SingleOutcome(
        		new double[] { 10.0 },
        		new double[] { 1.0 },
        		Collections.emptyList(), "1"
        ));
        id2.addOutcome(new SingleOutcome(
        		new double[] { 14.0 },
        		new double[] { 3.0 },
        		Collections.emptyList(), "1"
        ));
        id2.addOutcome(new SingleOutcome(
        		new double[] { 3.0 },
        		new double[] { 5.0 },
        		Collections.emptyList(), "1"
        ));
        id2.addOutcome(new SingleOutcome(
        		new double[] { 1.0 },
        		new double[] { 20.0 },
        		Collections.emptyList(), "1"
        ));
        id2.addOutcome(new SingleOutcome(
        		new double[] { 10.0 },
        		new double[] { 11.0 },
        		Collections.emptyList(), "1"
        ));
        id2.addOutcome(new SingleOutcome(
        		new double[] { 14.0 },
        		new double[] { 31.0 },
        		Collections.emptyList(), "1"
        ));
        id2.addOutcome(new SingleOutcome(
        		new double[] { 31.0 },
        		new double[] { 5.0 },
        		Collections.emptyList(), "1"
        ));

        double result = SpearmanCorrelation.calculate(id2).get(SpearmanCorrelation.class.getSimpleName());
        assertEquals(0.097568, result, 0.00001);
    }
    
    @Test
    public void measurePerfectMatch()
    {
        Id2Outcome id2 = new Id2Outcome();

        id2.addOutcome(new SingleOutcome(
        		new double[] { 1.0 },
        		new double[] { 1.0 },
        		Collections.emptyList(), "1"
        ));
        id2.addOutcome(new SingleOutcome(
        		new double[] { 2.0 },
        		new double[] { 2.0 },
        		Collections.emptyList(), "1"
        ));
        id2.addOutcome(new SingleOutcome(
        		new double[] { 3.0 },
        		new double[] { 3.0 },
        		Collections.emptyList(), "1"
        ));
        id2.addOutcome(new SingleOutcome(
        		new double[] { 4.0 },
        		new double[] { 4.0 },
        		Collections.emptyList(), "1"
        ));

        double result = SpearmanCorrelation.calculate(id2).get(SpearmanCorrelation.class.getSimpleName());
        assertEquals(1.0, result, 0.000001);
    }
}