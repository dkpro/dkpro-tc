/*
 * Copyright 2018
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

import org.dkpro.tc.evaluation.Id2Outcome;
import org.dkpro.tc.evaluation.measures.regression.PearsonCorrelation;
import org.junit.Test;
import org.mockito.Mockito;

public class PearsonCorrelationTest
{

    @Test
    public void measureTest()
    {
        Id2Outcome id2 = Mockito.mock(Id2Outcome.class);

        double[] predictions = new double[] { 1.0, 10.0, 14.0, 3.0 };
        double[] golds = new double[] { 2.0, 1.0, 3.0, 5.0 };
        
        Mockito.when(id2.getPredictions()).thenReturn(predictions);
        Mockito.when(id2.getGoldValues()).thenReturn(golds);

        double result = PearsonCorrelation.calculate(id2).get(PearsonCorrelation.class.getSimpleName());
        assertEquals(-0.25786, result, 0.00001);
    }
    
    @Test
    public void measurePerfectMatch()
    {
        Id2Outcome id2 = Mockito.mock(Id2Outcome.class);

        double[] predictions = new double[] { 1.0, 2.0, 3.0, 4.0 };
        double[] golds = new double[] { 1.0, 2.0, 3.0, 4.0 };
        
        Mockito.when(id2.getPredictions()).thenReturn(predictions);
        Mockito.when(id2.getGoldValues()).thenReturn(golds);

        double result = PearsonCorrelation.calculate(id2).get(PearsonCorrelation.class.getSimpleName());
        assertEquals(1.0, result, 0.000001);
    }
}