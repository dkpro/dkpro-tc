/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.evaluation.evaluator.multi;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import org.dkpro.tc.evaluation.evaluator.EvaluatorFactory;
import org.dkpro.tc.evaluation.measures.example.SubsetAccuracy;
import org.dkpro.tc.evaluation.measures.label.MacroFScore;
import org.dkpro.tc.evaluation.measures.label.MacroPrecision;
import org.dkpro.tc.evaluation.measures.label.MacroRecall;
import org.dkpro.tc.evaluation.measures.label.MicroFScore;
import org.dkpro.tc.evaluation.measures.label.MicroPrecision;
import org.dkpro.tc.evaluation.measures.label.MicroRecall;

public class MultiEvaluatorTest
{

    static File file = new File("src/test/resources/datasets/multi/reuters/id2outcome.txt");
    Map<String, Double> results;

    public void setup(boolean softEvaluation, boolean individualLabelMeasures)
        throws IOException, TextClassificationException
    {
        EvaluatorBase evaluator = EvaluatorFactory.createEvaluator(file, Constants.LM_MULTI_LABEL, 
        		softEvaluation, individualLabelMeasures);
        results = evaluator.calculateEvaluationMeasures();
    }

    @Test
    public void testCalculateSoftEvaluationMeasures()
        throws IOException, TextClassificationException
    {
        setup(true, false);

        Double macroPr = results.get(MacroPrecision.class.getSimpleName());
        assertEquals(0.611, macroPr, 0.001);

        Double macroRe = results.get(MacroRecall.class.getSimpleName());
        assertEquals(0.375, macroRe, 0.001);

        Double macroFSc = results.get(MacroFScore.class.getSimpleName());
        assertEquals(0.422222, macroFSc, 0.000001);
        
        Double microPr = results.get(MicroPrecision.class.getSimpleName());
        assertEquals(0.81, microPr, 0.01);
        
        Double microRe = results.get(MicroRecall.class.getSimpleName());
        assertEquals(0.56, microRe, 0.01);
        
        Double microFSc = results.get(MicroFScore.class.getSimpleName());
        assertEquals(0.67, microFSc, 0.01);
        
        Double subsetAccuracy = results.get(SubsetAccuracy.class.getSimpleName());
        assertEquals(0.5833, subsetAccuracy, 0.0001);
    }

    @Test
    public void testCalculateStrictEvaluationMeasures()
        throws IOException, TextClassificationException
    {
        setup(false, false);

        Double macroPr = results.get(MacroPrecision.class.getSimpleName());
        assertEquals(macroPr, Double.NaN, 0.01);
        
        Double macroRe = results.get(MacroRecall.class.getSimpleName());
        assertEquals(macroRe, Double.NaN, 0.01);
        
        Double macroFSc = results.get(MacroFScore.class.getSimpleName());
        assertEquals(macroFSc, Double.NaN, 0.01);
        
        Double microPr = results.get(MicroPrecision.class.getSimpleName());
        assertEquals(0.81, microPr, 0.01);
        
        Double microRe = results.get(MicroRecall.class.getSimpleName());
        assertEquals(0.56, microRe, 0.01);
        
        Double microFSc = results.get(MicroFScore.class.getSimpleName());
        assertEquals(0.67, microFSc, 0.01);
        
        Double subsetAccuracy = results.get(SubsetAccuracy.class.getSimpleName());
        assertEquals(0.5833, subsetAccuracy, 0.0001);
    }
    
}
