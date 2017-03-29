/*******************************************************************************
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
 ******************************************************************************/
package org.dkpro.tc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.evaluation.Id2Outcome;
import org.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import org.dkpro.tc.evaluation.evaluator.EvaluatorFactory;

public class EvaluationReportUtil
{
    public static Map<String, String> getResultsHarmonizedId2Outcome(File id2o,
            boolean softEvaluation, boolean individualLabelMeasures)
                throws TextClassificationException
    {
        Map<String, String> resultMap = new HashMap<>();
        try {
            ObjectInputStream inputStream = new ObjectInputStream(
                    new FileInputStream(id2o));
            Id2Outcome id2Outcome = (Id2Outcome) inputStream.readObject();
            inputStream.close();

            EvaluatorBase evaluator = EvaluatorFactory.createEvaluator(id2Outcome, softEvaluation,
                    individualLabelMeasures);
            Map<String, Double> resultTempMap = evaluator.calculateEvaluationMeasures();
            resultMap = new HashMap<String, String>();
            for (String key : resultTempMap.keySet()) {
                Double value = resultTempMap.get(key);
                resultMap.put(key, String.valueOf(value));
            }
        }
        catch (Exception e) {
            throw new TextClassificationException(e);
        }

        return resultMap;
    }

    public static Map<String, String> getResultsId2Outcome(File id2o, String mode, boolean softEvaluation,
            boolean individualLabelMeasures) throws TextClassificationException, IOException
    {
        Id2Outcome id2outcome = new Id2Outcome(id2o, mode);
        EvaluatorBase evaluator = EvaluatorFactory.createEvaluator(id2outcome, softEvaluation, individualLabelMeasures);
        Map<String, Double> resultTempMap = evaluator.calculateEvaluationMeasures();
        Map<String, String> resultMap = new HashMap<String, String>();
        for (String key : resultTempMap.keySet()) {
            Double value = resultTempMap.get(key);
            resultMap.put(key, String.valueOf(value));
        }
        return resultMap;
    }
}
