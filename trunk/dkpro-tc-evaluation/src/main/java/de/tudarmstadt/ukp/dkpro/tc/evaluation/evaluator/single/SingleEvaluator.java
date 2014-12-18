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
import java.util.List;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.AbstractLargeContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.CombinedSmallContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.SingleLargeContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.SmallContingencyTables;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.BipartitionBased;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.Accuracy;

/**
 * @author Andriy Nadolskyy
 * 
 */
public class SingleEvaluator
    extends EvaluatorBase
    implements BipartitionBased
{
	
    public SingleEvaluator(Map<String, Integer> class2number,
			List<String> readData, boolean softEvaluation,
			boolean individualLabelMeasures) {
		super(class2number, readData, softEvaluation, individualLabelMeasures);
	}

    @Override
	public AbstractLargeContingencyTable<List<List<Double>>> buildLargeContingencyTable()
    {
        int number = class2number.keySet().size();
        List<List<Double>> largeContingencyTable = new ArrayList<List<Double>>();
        for (int i = 0; i < number; i++) {
            ArrayList<Double> local = new ArrayList<Double>();
            for (int j = 0; j < number; j++) {
                local.add(0.0);
            }
            largeContingencyTable.add(i, (ArrayList<Double>) local.clone());
        }

        for (String line : readData) {
            // consists of: prediction, gold label, threshold
            // in the case of single label the threshold is ignored
            String[] splittedEvaluationData = line.split(";");
            int predictedClass = Integer.valueOf(splittedEvaluationData[0]);
            int goldClass = Integer.valueOf(splittedEvaluationData[1]);

            double oldValue = largeContingencyTable.get(goldClass).get(predictedClass);
            largeContingencyTable.get(goldClass).set(predictedClass, oldValue + 1);
        }
        return new SingleLargeContingencyTable(largeContingencyTable, class2number);
    }

    @Override
    public Map<String, Double> calculateEvaluationMeasures()
    {
        SingleLargeContingencyTable largeConfMatr = (SingleLargeContingencyTable) buildLargeContingencyTable();
        SmallContingencyTables smallConfMatrices = largeConfMatr.decomposeLargeContingencyTable();
        CombinedSmallContingencyTable combinedSmallConfMatr = smallConfMatrices.buildCombinedSmallContingencyTable();

        Map<String, Double> results = new HashMap<String, Double>();
        Map<String, Double> macroResults = calculateMacroMeasures(smallConfMatrices);
        Map<String, Double> microResults = calculateMicroMeasures(combinedSmallConfMatr);
        int numberOfContingencyTables = class2number.size();
		Map<String, Double> accuracyResult = Accuracy.calculate(combinedSmallConfMatr, 
				numberOfContingencyTables, softEvaluation);
		
		results.putAll(macroResults);
        results.putAll(microResults);
        results.putAll(accuracyResult);
        return results;
    }

    @Override
    public Map<String, Double> calculateMicroEvaluationMeasures()
    {
        SingleLargeContingencyTable largeConfMatr = (SingleLargeContingencyTable) buildLargeContingencyTable();
        SmallContingencyTables smallConfMatrices = largeConfMatr.decomposeLargeContingencyTable();
        CombinedSmallContingencyTable combinedSmallConfMatr = smallConfMatrices.buildCombinedSmallContingencyTable();

        return calculateMicroMeasures(combinedSmallConfMatr);
    }

}
