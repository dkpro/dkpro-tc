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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.AbstractLargeContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.CombinedSmallContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.MultiLargeContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.SmallContingencyTables;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.BipartitionBased;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label.Accuracy;

/**
 * @author Andriy Nadolskyy
 * 
 */
public class MultiEvaluator
    extends EvaluatorBase
    implements BipartitionBased
{

    public MultiEvaluator(Map<String, Integer> class2number,
			List<String> readData, boolean softEvaluation,
			boolean individualLabelMeasures) {
		super(class2number, readData, softEvaluation, individualLabelMeasures);
	}

    @Override
    public AbstractLargeContingencyTable<Map<String, Map<String, Double>>> buildLargeContingencyTable()
    {
        Set<String> labelCombinations = getSetOfLabelCombinations();

        // gold - prediction - value
        Map<String, Map<String, Double>> largeContingencyTable =
                new HashMap<String, Map<String, Double>>();
        HashMap<String, Double> tempDimension = new HashMap<String, Double>();
        for (String labelCombination : labelCombinations) {
            tempDimension.put(labelCombination, 0.0);
        }
        for (String labelCombination : labelCombinations) {
            largeContingencyTable.put(labelCombination, (HashMap<String, Double>) tempDimension.clone());
        }

        for (String line : readData) {
            // consists of: prediction, gold label, threshold
            String[] splittedEvaluationData = line.split(";");
            String predictionValues = splittedEvaluationData[0];
            String goldValues = splittedEvaluationData[1];
            Double threshold = Double.valueOf(splittedEvaluationData[2]);

            String[] predictionValue = predictionValues.split(",");
            String accumulatedPredictionLabelCombination = "";
            for (int i = 0; i < predictionValue.length; i++) {
                if (Double.valueOf(predictionValue[i]) >= threshold) {
                    if (! accumulatedPredictionLabelCombination.equals(""))
                        accumulatedPredictionLabelCombination += ",";
                    accumulatedPredictionLabelCombination += i;
                }
            }
            if (accumulatedPredictionLabelCombination.equals("")) {
                // replace with appropriate label
            	accumulatedPredictionLabelCombination = String.valueOf(class2number.get(""));
            }
            
            String[] goldValue = goldValues.split(",");
            String accumulatedGoldLabelCombination = "";
            for (int i = 0; i < goldValue.length; i++) {
                if (Double.valueOf(goldValue[i]) >= threshold) {
                    if (! accumulatedGoldLabelCombination.equals(""))
                        accumulatedGoldLabelCombination += ",";
                    accumulatedGoldLabelCombination += i;
                }
            }
            if (accumulatedGoldLabelCombination.equals("")) {
                // replace with appropriate label
            	accumulatedGoldLabelCombination = String.valueOf(class2number.get(""));
            }
            
            Double updatedValue = largeContingencyTable.get(accumulatedGoldLabelCombination).
            		get(accumulatedPredictionLabelCombination) + 1;
            largeContingencyTable.get(accumulatedGoldLabelCombination).
            		put(accumulatedPredictionLabelCombination, updatedValue);
        }
        return new MultiLargeContingencyTable(largeContingencyTable, class2number);
    }

    /**
     * get set of relevant label combinations: get it from the list of predictions and gold labels
     * regarding to threshold
     * 
     * @return set of label combinations
     * @throws IOException
     */
    public HashSet<String> getSetOfLabelCombinations()
    {
        HashSet<String> labelCombinations = new HashSet<String>();
        for (String line : readData) {
            // consists of: prediction, gold label, threshold
            String[] splittedEvaluationData = line.split(";");
            String predictionValues = splittedEvaluationData[0];
            String goldValues = splittedEvaluationData[1];
            Double threshold = Double.valueOf(splittedEvaluationData[2]);

            String[] predictionValue = predictionValues.split(",");
            String accumulatedPredictionLabelCombination = "";
            for (int i = 0; i < predictionValue.length; i++) {
                if (Double.valueOf(predictionValue[i]) >= threshold) {
                    if (! accumulatedPredictionLabelCombination.equals(""))
                        accumulatedPredictionLabelCombination += ",";
                    accumulatedPredictionLabelCombination += i;
                }
            }
            
            String[] goldValue = goldValues.split(",");
            String accumulatedGoldLabelCombination = "";
            for (int i = 0; i < goldValue.length; i++) {
                if (Double.valueOf(goldValue[i]) >= threshold) {
                    if (! accumulatedGoldLabelCombination.equals(""))
                        accumulatedGoldLabelCombination += ",";
                    accumulatedGoldLabelCombination += i;
                }
            }

            labelCombinations.add(accumulatedPredictionLabelCombination);
            labelCombinations.add(accumulatedGoldLabelCombination);
        }
         
        if (labelCombinations.contains("")) {
        	// add "" label to class2number and replace it in list of labelCombinations
        	// with the next free number from class2number
        	int additionalLabelNumber = class2number.size();
        	class2number.put("", additionalLabelNumber);
        	labelCombinations.remove("");
        	labelCombinations.add(String.valueOf(additionalLabelNumber));
        }
        return labelCombinations;
    }

    @Override
    public Map<String, Double> calculateEvaluationMeasures()
    {
        MultiLargeContingencyTable largeConfMatr = (MultiLargeContingencyTable) buildLargeContingencyTable();
        SmallContingencyTables smallConfMatrices = largeConfMatr.decomposeLargeContingencyTable();
        CombinedSmallContingencyTable combinedSmallConfMatr = smallConfMatrices.buildCombinedSmallContingencyTable();

        // TODO: add example-based measures
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
        MultiLargeContingencyTable largeConfMatr = (MultiLargeContingencyTable) buildLargeContingencyTable();
        SmallContingencyTables smallConfMatrices = largeConfMatr.decomposeLargeContingencyTable();
        CombinedSmallContingencyTable combinedSmallConfMatr = smallConfMatrices.buildCombinedSmallContingencyTable();

        return calculateMicroMeasures(combinedSmallConfMatr);
    }

}
