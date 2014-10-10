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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.ConfusionMatrix;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.SingleConfusionMatrix;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.BipartitionBased;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.ContingencyTable;

/**
 * @author Andriy Nadolskyy
 * 
 */
public class SingleEvaluator
    extends EvaluatorBase
    implements BipartitionBased
{

    public SingleEvaluator(Map<String, Integer> class2number,
            List<String> readData, boolean softEvaluation)
    {
        super(class2number, readData, softEvaluation);
    }

    public ConfusionMatrix<List<List<Double>>> buildConfusionMatrix()
    {
        int number = class2number.keySet().size();
        List<List<Double>> confusionMatrix = new ArrayList<List<Double>>();
        for (int i = 0; i < number; i++) {
            ArrayList<Double> local = new ArrayList<Double>();
            for (int j = 0; j < number; j++) {
                local.add(0.0);
            }
            confusionMatrix.add(i, (ArrayList<Double>) local.clone());
        }

        for (String line : readData) {
            // consists of: prediction, gold label, threshold
            // in the case of single label the threshold is ignored
            String[] splittedEvaluationData = line.split(";");
            int predictedClass = Integer.valueOf(splittedEvaluationData[0]);
            int goldClass = Integer.valueOf(splittedEvaluationData[1]);

            double oldValue = confusionMatrix.get(goldClass).get(predictedClass);
            confusionMatrix.get(goldClass).set(predictedClass, oldValue + 1);
        }
        return new SingleConfusionMatrix(confusionMatrix, class2number);
    }

    @Override
    public Map<String, String> calculateEvaluationMeasures()
    {
        SingleConfusionMatrix confMatr = (SingleConfusionMatrix) buildConfusionMatrix();
        ContingencyTable table = confMatr.decomposeConfusionMatrix();

        // TODO: add measures for individual labels
        // TODO: add micro-averaged measures
        Map<String, String> results = calculateLabelBasedEvaluationMeasures(table);
        return results;
    }

}
