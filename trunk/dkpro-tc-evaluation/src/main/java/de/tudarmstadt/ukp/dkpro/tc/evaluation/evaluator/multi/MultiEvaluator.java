/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.multi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.Id2Outcome;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.SingleOutcome;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.AbstractLargeContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.CombinedSmallContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.MultiLargeContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.SmallContingencyTables;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.BipartitionBased;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.EvaluatorBase;

/**
 * @author Andriy Nadolskyy
 * 
 */
public class MultiEvaluator
    extends EvaluatorBase
    implements BipartitionBased
{

    public MultiEvaluator(Id2Outcome id2Outcome, boolean softEvaluation,
			boolean individualLabelMeasures) {
		super(id2Outcome, softEvaluation, individualLabelMeasures);
	}
    
    /***
     * build small contingency tables (a small contingency table for each label) from id2Outcome 
     * 
     * @return built small contingency tables
     */
    private SmallContingencyTables buildSmallContingencyTables() {

    	Set<String> allLabelSet = new TreeSet<String>();
    	for (SingleOutcome outcome : id2Outcome.getOutcomes()) {
    		allLabelSet.addAll(outcome.getLabels());
		}
    	
    	List<String> labelList = new ArrayList<String>(allLabelSet);	
        int numberOfLabels = labelList.size();
        
        double counterIncreaseValue = 1.0;
        SmallContingencyTables smallContingencyTables = new SmallContingencyTables(labelList);
        for (int classId = 0; classId < numberOfLabels; classId++) {
        	for (SingleOutcome outcome : id2Outcome.getOutcomes()) {
        		double threshold = outcome.getBipartitionThreshold();
        		double goldValue = outcome.getGoldstandard()[classId];
        		double predictionValue = outcome.getPrediction()[classId];
        		if (goldValue >= threshold) {
        			// true positive
        			if (predictionValue >= threshold) {
        				smallContingencyTables.addTruePositives(classId, counterIncreaseValue);
        			}
        			// false negative
        			else {
        				smallContingencyTables.addFalseNegatives(classId, counterIncreaseValue);
        			}
        		}
        		else {
        			// false positive
        			if (predictionValue >= threshold) {
        				smallContingencyTables.addFalsePositives(classId, counterIncreaseValue);
        			}
        			// true negative
        			else {
        				smallContingencyTables.addTrueNegatives(classId, counterIncreaseValue);
        			}
        		}
        	}
        }
        return smallContingencyTables;
    }
    

    @Override
    public AbstractLargeContingencyTable<Map<String, Map<String, Double>>> buildLargeContingencyTable()
    {
        List<String> labelCombinations = getLabelCombinations();

        Map<String, Map<String, Double>> largeContingencyTable =
                new HashMap<String, Map<String, Double>>();
        HashMap<String, Double> tempDimension = new HashMap<String, Double>();
        for (String labelCombination : labelCombinations) {
            tempDimension.put(labelCombination, 0.0);
        }
        for (String labelCombination : labelCombinations) {
            largeContingencyTable.put(labelCombination, (HashMap<String, Double>) tempDimension.clone());
        }

        for (SingleOutcome outcome : id2Outcome.getOutcomes()) {

            double[] predictionArray = outcome.getPrediction();
            double[] goldstandardArray = outcome.getGoldstandard();
                        
            List<String> predictionLabels = accumulateLabels(predictionArray, outcome.getBipartitionThreshold(), outcome.getLabels());
            List<String> goldStandardLabels = accumulateLabels(goldstandardArray, outcome.getBipartitionThreshold(), outcome.getLabels());
            
            // update CV per label
            for(String goldStandardLabel : goldStandardLabels){
            	for(String predictionLabel : predictionLabels){
            		Double updatedValue = largeContingencyTable.get(goldStandardLabel).
                    		get(predictionLabel) + 1;
                    largeContingencyTable.get(goldStandardLabel).
                    		put(predictionLabel, updatedValue);
            	}
            }     
            
        }
        return new MultiLargeContingencyTable(largeContingencyTable, labelCombinations);
    }

    /**
     * get set of relevant label combinations: get it from the list of predictions and gold labels
     * regarding to threshold
     * 
     * @return set of label combinations
     * @throws IOException
     */
    public List<String> getLabelCombinations()
    {
        HashSet<String> labelCombinations = new HashSet<String>();
        for (SingleOutcome outcome : id2Outcome.getOutcomes()) {

            double[] predictionArray = outcome.getPrediction();
            double[] goldstandardArray = outcome.getGoldstandard();
                        
            List<String> predictionLabels = accumulateLabels(predictionArray, outcome.getBipartitionThreshold(), outcome.getLabels());
            List<String> goldstandardLabels = accumulateLabels(goldstandardArray, outcome.getBipartitionThreshold(), outcome.getLabels());

            System.out.println("gold"+predictionLabels);
            System.out.println("pred"+goldstandardLabels);


            labelCombinations.addAll(predictionLabels);
            labelCombinations.addAll(goldstandardLabels);
        }
         
        return new ArrayList<String>(labelCombinations);
    }

    @Override
    public Map<String, Double> calculateEvaluationMeasures()
    {    	
        // MultiLargeContingencyTable largeConfMatr = (MultiLargeContingencyTable) buildLargeContingencyTable();
        // SmallContingencyTables smallConfMatrices = largeConfMatr.decomposeLargeContingencyTable();
        
    	SmallContingencyTables smallConfMatrices = buildSmallContingencyTables();
        CombinedSmallContingencyTable combinedSmallConfMatr = smallConfMatrices.buildCombinedSmallContingencyTable();

        // TODO: add example-based measures
        Map<String, Double> results = new HashMap<String, Double>();
        Map<String, Double> macroResults = calculateMacroMeasures(smallConfMatrices);
        Map<String, Double> microResults = calculateMicroMeasures(combinedSmallConfMatr);
		// Map<String, Double> accuracyResult = Accuracy.calculate(combinedSmallConfMatr, 
		//		smallConfMatrices.getClass2Number().size(), softEvaluation);
		
		results.putAll(macroResults);
        results.putAll(microResults);
        // results.putAll(accuracyResult);
        return results;
    }

    @Override
    public Map<String, Double> calculateMicroEvaluationMeasures()
    {
        // MultiLargeContingencyTable largeConfMatr = (MultiLargeContingencyTable) buildLargeContingencyTable();
        // SmallContingencyTables smallConfMatrices = largeConfMatr.decomposeLargeContingencyTable();
    	
    	SmallContingencyTables smallConfMatrices = buildSmallContingencyTables();
        CombinedSmallContingencyTable combinedSmallConfMatr = smallConfMatrices.buildCombinedSmallContingencyTable();

        return calculateMicroMeasures(combinedSmallConfMatr);
    }
    
    private List<String> accumulateLabels(double[] values, double bipartitionThreshold, List<String> labels){
    	List<String> accumulatedLabelCombination = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            if (values[i] >= bipartitionThreshold) {
                try {
                	accumulatedLabelCombination.add(URLEncoder.encode(labels.get(i), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
				}
            }
        }
        if(accumulatedLabelCombination.isEmpty()){
        	accumulatedLabelCombination.add(Constants.EMPTY_PREDICTION);
        }
		return accumulatedLabelCombination;
    }

}
