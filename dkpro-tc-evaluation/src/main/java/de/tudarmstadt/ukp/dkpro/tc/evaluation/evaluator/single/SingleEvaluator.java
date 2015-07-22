/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.single;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.Id2Outcome;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.SingleOutcome;
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
	
    public SingleEvaluator(Id2Outcome id2Outcome, boolean softEvaluation,
			boolean individualLabelMeasures) {
		super(id2Outcome, softEvaluation, individualLabelMeasures);
	}

    @Override
	public AbstractLargeContingencyTable<List<List<Double>>> buildLargeContingencyTable() throws TextClassificationException
    {
    	Set<String> allLabelSet = new TreeSet<String>();
    	for (SingleOutcome outcome : id2Outcome.getOutcomes()) {
    		allLabelSet.addAll(outcome.getLabels());
		}
    	
    	List<String> labelList = new ArrayList<String>(allLabelSet);
    	
        int numberOfLabels = labelList.size();
        List<List<Double>> largeContingencyTable = new ArrayList<List<Double>>();
        
        // fill table with zeros
        for (int i = 0; i < numberOfLabels; i++) {
            ArrayList<Double> local = new ArrayList<Double>();
            for (int j = 0; j < numberOfLabels; j++) {
                local.add(0.0);
            }
            largeContingencyTable.add(i, (ArrayList<Double>) local.clone());
        }

        for (SingleOutcome outcome : id2Outcome.getOutcomes()) {
        	int predictedLabedIndex = -1;
        	int goldStandardLabelIndex = -1;
        	for (int i = 0; i < outcome.getLabels().size(); i++) {
        		if(outcome.getGoldstandard()[i]==1.){
        			if(goldStandardLabelIndex != -1){
        				throw new TextClassificationException("Wrong file format and/or learning mode");
        			}
        			else{
        				goldStandardLabelIndex = i;
        			}
        		}
        		if(outcome.getPrediction()[i]==1.){
        			if(predictedLabedIndex != -1){
        				throw new TextClassificationException("Wrong file format and/or learning mode");
        			}
        			else{
        				predictedLabedIndex = i;
        			}
        		}		
			}
        	if(predictedLabedIndex == -1 || goldStandardLabelIndex == -1){
				throw new TextClassificationException("Wrong file format and/or learning mode");
        	}
        	
            double oldValue = largeContingencyTable.get(outcome.getLabelMapping(labelList).get(goldStandardLabelIndex))
            		.get(outcome.getLabelMapping(labelList).get(predictedLabedIndex));
            largeContingencyTable.get(outcome.getLabelMapping(labelList).get(goldStandardLabelIndex))
            		.set(outcome.getLabelMapping(labelList).get(predictedLabedIndex), oldValue + 1);
        }
        return new SingleLargeContingencyTable(largeContingencyTable, labelList);
    }

    @Override
    public Map<String, Double> calculateEvaluationMeasures() throws TextClassificationException
    {
        SingleLargeContingencyTable largeConfMatr = (SingleLargeContingencyTable) buildLargeContingencyTable();
        SmallContingencyTables smallConfMatrices = largeConfMatr.decomposeLargeContingencyTable();
        CombinedSmallContingencyTable combinedSmallConfMatr = smallConfMatrices.buildCombinedSmallContingencyTable();

        Map<String, Double> results = new HashMap<String, Double>();
        Map<String, Double> macroResults = calculateMacroMeasures(smallConfMatrices);
        Map<String, Double> microResults = calculateMicroMeasures(combinedSmallConfMatr);

		Map<String, Double> accuracyResult = Accuracy.calculate(combinedSmallConfMatr, 
				smallConfMatrices.getClass2Number().size(), softEvaluation);
		
		results.putAll(macroResults);
        results.putAll(microResults);
        results.putAll(accuracyResult);
        return results;
    }

    @Override
    public Map<String, Double> calculateMicroEvaluationMeasures() throws TextClassificationException
    {
        SingleLargeContingencyTable largeConfMatr = (SingleLargeContingencyTable) buildLargeContingencyTable();
        SmallContingencyTables smallConfMatrices = largeConfMatr.decomposeLargeContingencyTable();
        CombinedSmallContingencyTable combinedSmallConfMatr = smallConfMatrices.buildCombinedSmallContingencyTable();

        return calculateMicroMeasures(combinedSmallConfMatr);
    }

}
