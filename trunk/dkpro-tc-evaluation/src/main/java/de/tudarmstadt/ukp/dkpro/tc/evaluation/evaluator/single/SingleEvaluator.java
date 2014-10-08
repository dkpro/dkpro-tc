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
import java.util.HashMap;
import java.util.LinkedList;

import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.ConfusionMatrix;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.SingleConfusionMatrix;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.BipartitionBased;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.EvaluatorBase;

/**
 * @author Andriy Nadolskyy
 * 
 */
public class SingleEvaluator
    extends EvaluatorBase
    implements BipartitionBased
{

    public SingleEvaluator(HashMap<String, Integer> class2number,
            LinkedList<String> readData, boolean softEvaluation)
    {
        super(class2number, readData, softEvaluation);
    }

    public ConfusionMatrix<ArrayList<ArrayList<Double>>> buildConfusionMatrix()
    {
        int number = class2number.keySet().size();
        ArrayList<ArrayList<Double>> confusionMatrix = new ArrayList<ArrayList<Double>>();
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
    public HashMap<String, String> calculateEvaluationMeasures()
    {
        SingleConfusionMatrix confMatr = (SingleConfusionMatrix) buildConfusionMatrix();
        double[][][] decomposedConfusionMatrix = confMatr.decomposeConfusionMatrix();

        // TODO: add measures for individual labels
        // TODO: add micro-averaged measures
        HashMap<String, String> results =
                calculateLabelBasedEvaluationMeasures(decomposedConfusionMatrix);
        return results;
    }

}
