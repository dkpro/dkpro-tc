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
package de.tudarmstadt.ukp.dkpro.tc.evaluation;

/**
 * A single entry from an id2outcome file (i.e. at least goldstandard and prediction)
 * 
 * @author daxenberger
 * 
 */
public class SingleOutcome
{

    private double bipartitionThreshold;
    private double[] prediction;
    private int[] goldstandard;
    private String[] labels;


    /**
     * @param goldstandard
     * @param prediction
     * @param labels
     */
    public SingleOutcome(int[] goldstandard, double[] prediction, String[] labels)
    {
        this.goldstandard = goldstandard;
        this.prediction = prediction;
        this.bipartitionThreshold = -1;
        this.labels = labels;
    }

    /**
     * @param goldstandard
     * @param prediction
     * @param bipartitionThreshold
     * @param labels
     */
    public SingleOutcome(int[] goldstandard, double[] prediction, double bipartitionThreshold, String[] labels)
    {
        this.goldstandard = goldstandard;
        this.prediction = prediction;
        this.bipartitionThreshold = bipartitionThreshold;
        this.labels = labels;
    }

    /**
     * @return
     */
    public double getBipartitionThreshold()
    {
        return bipartitionThreshold;
    }

    /**
     * @return
     */
    public double[] getPrediction()
    {
        return prediction;
    }

    /**
     * @return
     */
    public int[] getGoldstandard()
    {
        return goldstandard;
    }

    /**
     * @return
     */
    public String[] getLabels()
    {
        return labels;
    }
}
