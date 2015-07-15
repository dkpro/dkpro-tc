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
package de.tudarmstadt.ukp.dkpro.tc.evaluation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container for single entries from a {link {@link Id2Outcome}s, i.e. at least gold standard value
 * and prediction value
 * 
 * @author daxenberger
 * @author Andriy Nadolskyy
 * 
 */
public class SingleOutcome implements Serializable
{

	private static final long serialVersionUID = 2067854918511850890L;
	private double bipartitionThreshold;
    private double[] prediction;
    private double[] goldstandard;
    private List<String> labels;


    /**
     * Creates a single outcome (single-label classification).
     * 
     * @param goldstandard
     *            the gold standard values, in the order specified by {@code labels}
     * @param prediction
     *            the prediction value, in the order specified by {@code labels}
     * @param labels
     *            a list with the class labels
     */
    public SingleOutcome(double[] goldstandard, double[] prediction, List<String> labels)
    {
        this.goldstandard = goldstandard;
        this.prediction = prediction;
        this.bipartitionThreshold = -1;
        this.labels = labels;
    }

    /**
     * Creates a single outcome (multi-label classification).
     * 
     * @param goldstandard
     *            the gold standard values, in the order specified by {@code labels}
     * @param prediction
     *            the prediction value, in the order specified by {@code labels}
     * @param bipartitionThreshold
     *            the bipartition threshold
     * @param labels
     *            a list with the class labels
     */
    public SingleOutcome(double[] goldstandard, double[] prediction, double bipartitionThreshold, List<String> labels)
    {
        this.goldstandard = goldstandard;
        this.prediction = prediction;
        this.bipartitionThreshold = bipartitionThreshold;
        this.labels = labels;
    }

    /**
     * Retrieves the bipartition threshold.
     * 
     * @return the bipartition threshold
     */
    public double getBipartitionThreshold()
    {
        return bipartitionThreshold;
    }

    /**
     * Retrieves the prediction.
     * 
     * @return the prediction, as array ordered according to @link {@link SingleOutcome#getLabels()}
     *         .
     */
    public double[] getPrediction()
    {
        return prediction;
    }

    /**
     * Retrieves the gold standard.
     * 
     * @return the gold standard, as array ordered according to @link
     *         {@link SingleOutcome#getLabels()}.
     */
    public double[] getGoldstandard()
    {
        return goldstandard;
    }

    /**
     * Retrieves the class labels.
     * 
     * @return the class labels
     */
    public List<String> getLabels()
    {
        return labels;
    }

    /**
     * Retrieves a mapping of class label indices, given an external list of class labels.
     * 
     * @param allLabels
     *            a list of class labels from another @link {@link SingleOutcome}
     * @return a map with corresponding class label indices, the keys are indices of the class
     *         labels in this @link {@link SingleOutcome}, the values are indices of the class
     *         labels from {@code allLabels}
     */
    public Map<Integer, Integer> getLabelMapping(List<String> allLabels){
    	
    	Map<Integer, Integer> map = new HashMap<Integer, Integer>();
    	for (String label : allLabels) {
			map.put(labels.indexOf(label), allLabels.indexOf(label));
		}
    	return map;
    }
    
    /**
     * Retrieves a mapping of class label indices, given an external list of class labels.
     * 
     * @param allLabels
     *            a list of class labels from another @link {@link SingleOutcome}
     * @return a map with corresponding class label indices, the keys are indices of the class
     *         labels from {@code allLabels}, the values are indices of the class labels in this @link
     *         {@link SingleOutcome}
     */
    public Map<Integer, Integer> getReverseLabelMapping(List<String> allLabels)
    {

        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (String label : allLabels) {
            map.put(allLabels.indexOf(label), labels.indexOf(label));
        }
        return map;
    }

    /**
     * checks if predicted and gold labels provide an exact match (regarding to threshold)
     * 
     * @return true in the case of an exact match false otherwise
     */
    public boolean isExactMatch(){
    	int length = goldstandard.length;
    	if (length != prediction.length)
    		return false;
    	
    	for (int i = 0; i < length; i++) {
			if (((goldstandard[i] >= bipartitionThreshold) && (prediction[i] < bipartitionThreshold)) ||
					((goldstandard[i] < bipartitionThreshold) && (prediction[i] >= bipartitionThreshold))){
				return false;				
			}
		}
    	return true;
    }
}
