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
import java.util.Map;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.EvaluatorFactory;
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
    Map<String, Double> results;

    public void setup(boolean softEvaluation, boolean individualLabelMeasures)
        throws IOException
    {
        EvaluatorBase evaluator = EvaluatorFactory.createEvaluator(file, Constants.LM_SINGLE_LABEL, 
        		softEvaluation, individualLabelMeasures);
        results = evaluator.calculateEvaluationMeasures();
    }

    @Test
    public void testCalculateSoftEvaluationMeasures()
        throws IOException
    {
        setup(true, false);
        
        Double macroPr = results.get(MacroPrecision.class.getSimpleName());
        assertEquals(0.0476190, macroPr, 0.00001);

        Double macroRe = results.get(MacroRecall.class.getSimpleName());
        assertEquals(0.0476190, macroRe, 0.00001);

        Double macroAcc = results.get(MacroAccuracy.class.getSimpleName());
        assertEquals(0.3809523, macroAcc, 0.0001);

        Double macroFSc = results.get(MacroFScore.class.getSimpleName());
        assertEquals(0.0476190, macroFSc, 0.0001);
    }

    @Test
    public void testCalculateStrictEvaluationMeasures()
        throws IOException
    {
        setup(false, false);

        Double macroPr = results.get(MacroPrecision.class.getSimpleName());
        assertEquals(0.0476190, macroPr, 0.00001);

        Double macroRe = results.get(MacroRecall.class.getSimpleName());
        assertTrue("result should be an invalid value, but isn't", Double.isNaN(macroRe));

        Double macroAcc = results.get(MacroAccuracy.class.getSimpleName());
        assertEquals(0.3809523, macroAcc, 0.0001);

        Double macroFSc = results.get(MacroFScore.class.getSimpleName());
        assertTrue("result should be an invalid value, but isn't", Double.isNaN(macroFSc));
    }

}
