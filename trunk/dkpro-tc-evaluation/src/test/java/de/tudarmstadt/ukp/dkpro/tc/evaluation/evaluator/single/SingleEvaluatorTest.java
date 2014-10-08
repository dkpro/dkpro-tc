/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universit�t Darmstadt
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.single;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.EvaluatorFactory;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.EvaluatorFactory.EvaluationMode;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroAccuracy;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroFScore;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroPrecision;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroRecall;

/**
 * @author Andriy Nadolskyy
 * 
 */
public class SingleEvaluatorTest
{

    static File file = new File("src/test/resources/datasets/single/id2outcome.txt");
    EvaluationMode mode = EvaluationMode.SINGLE;
    HashMap<String, String> results;

    public void setup(boolean softEvaluation)
        throws IOException
    {
        EvaluatorFactory evalFactory = new EvaluatorFactory(file, mode, softEvaluation);
        evalFactory.readDataFile();
        EvaluatorBase evaluator = evalFactory.makeEvaluator();
        results = evaluator.calculateEvaluationMeasures();
    }

    @Test
    public void testCalculateSoftEvaluationMeasures()
        throws IOException
    {
        setup(true);
        // macro precision
        Double prValue = Double.valueOf(results.get(MacroPrecision.class.getSimpleName()));
        assertEquals(0.047619047619047616, prValue, 0.00001);

        // macro recall
        Double reValue = Double.valueOf(results.get(MacroRecall.class.getSimpleName()));
        assertEquals(0.047619047619047616, reValue, 0.00000001);

        // macro accuracy
        Double accValue = Double.valueOf(results.get(MacroAccuracy.class.getSimpleName()));
        assertEquals(0.38095238095238093, accValue, 0.0001);

        // macro f-score
        Double fScValue = Double.valueOf(results.get(MacroFScore.class.getSimpleName()));
        assertEquals(0.047619047619047616, fScValue, 0.000001);
    }

    @Test
    public void testCalculateStrictEvaluationMeasures()
        throws IOException
    {
        setup(false);

        // macro precision
        Double prValue = Double.valueOf(results.get(MacroPrecision.class.getSimpleName()));
        assertEquals(0.047619047619047616, prValue, 0.00001);

        // macro recall
        Double reValue = Double.valueOf(results.get(MacroRecall.class.getSimpleName()));
        assertTrue("result should be an invalid value, but isn't", Double.isNaN(reValue));

        // macro accuracy
        Double accValue = Double.valueOf(results.get(MacroAccuracy.class.getSimpleName()));
        assertEquals(0.38095238095238093, accValue, 0.0001);

        // macro f-score
        Double fScValue = Double.valueOf(results.get(MacroFScore.class.getSimpleName()));
        assertTrue("result should be an invalid value, but isn't", Double.isNaN(fScValue));
    }

}
