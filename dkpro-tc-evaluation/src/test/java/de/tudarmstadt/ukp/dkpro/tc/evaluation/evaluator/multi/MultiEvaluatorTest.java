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
package de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.multi;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.EvaluatorFactory;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroFScore;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroPrecision;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MacroRecall;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MicroFScore;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MicroPrecision;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.MicroRecall;

/**
 * @author Andriy Nadolskyy
 * 
 */
public class MultiEvaluatorTest
{

    static File file = new File("src/test/resources/datasets/multi/reuters/id2outcome.txt");
    Map<String, Double> results;

    public void setup(boolean softEvaluation, boolean individualLabelMeasures)
        throws IOException
    {
        EvaluatorBase evaluator = EvaluatorFactory.createEvaluator(file, Constants.LM_MULTI_LABEL, 
        		softEvaluation, individualLabelMeasures);
        results = evaluator.calculateEvaluationMeasures();
    }

    @Test
    public void testCalculateSoftEvaluationMeasures()
        throws IOException
    {
        setup(true, false);

        Double macroPr = results.get(MacroPrecision.class.getSimpleName());
        assertEquals(0.367, macroPr, 0.001);

        Double macroRe = results.get(MacroRecall.class.getSimpleName());
        assertEquals(0.274, macroRe, 0.001);

        Double macroFSc = results.get(MacroFScore.class.getSimpleName());
        assertEquals(0.275325, macroFSc, 0.000001);
        
        Double microPr = results.get(MicroPrecision.class.getSimpleName());
        assertEquals(0.45, microPr, 0.01);
        
        Double microRe = results.get(MicroRecall.class.getSimpleName());
        assertEquals(0.45, microRe, 0.01);
        
        Double microFSc = results.get(MicroFScore.class.getSimpleName());
        assertEquals(0.45, microFSc, 0.01);
    }

    @Test
    public void testCalculateStrictEvaluationMeasures()
        throws IOException
    {
        setup(false, false);

        Double macroPr = results.get(MacroPrecision.class.getSimpleName());
        assertEquals(macroPr, Double.NaN, 0.01);
        
        Double macroRe = results.get(MacroRecall.class.getSimpleName());
        assertEquals(macroRe, Double.NaN, 0.01);
        
        Double macroFSc = results.get(MacroFScore.class.getSimpleName());
        assertEquals(macroFSc, Double.NaN, 0.01);
        
        Double microPr = results.get(MicroPrecision.class.getSimpleName());
        assertEquals(0.45, microPr, 0.01);
        
        Double microRe = results.get(MicroRecall.class.getSimpleName());
        assertEquals(0.45, microRe, 0.01);
        
        Double microFSc = results.get(MicroFScore.class.getSimpleName());
        assertEquals(0.45, microFSc, 0.01);
    }
}
