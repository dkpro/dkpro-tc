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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Container for id2outcome files.
 * 
 * @author daxenberger
 * 
 */
public class Id2Outcome
{
    private Set<SingleOutcome> outcomes;

    /**
     * Creates an empty id2outcome container.
     */
    public Id2Outcome()
    {
        this.outcomes = new HashSet<SingleOutcome>();
    }

    public Id2Outcome(File id2outcomeFile) throws IOException
    {
        outcomes = new HashSet<SingleOutcome>();

        BufferedReader br = new BufferedReader(new FileReader(id2outcomeFile));
        String line = "";
        List<String> labelList = null;
        while ((line = br.readLine()) != null) {
        	// this needs to happen at the beginning of the loop
            if (line.startsWith("#labels")) {
                labelList = getLabels(line);
            }
            else if (!line.startsWith("#")) {
                if (labelList == null) {
                    br.close();
                    throw new IOException("Wrong file format.");
                }
                // line might contain several '=', split at the last one
                int idxMostRightHandEqual = line.lastIndexOf("=");
                String evaluationData = line.substring(idxMostRightHandEqual + 1);

                String[] splittedEvaluationData = evaluationData.split(";");
                String[] predictionS = splittedEvaluationData[0].split(",");
                String[] goldS = splittedEvaluationData[1].split(",");
                double bipartitionThreshold;
				try {
					bipartitionThreshold = Double.valueOf(splittedEvaluationData[2]);
				} catch (ArrayIndexOutOfBoundsException e) {
					bipartitionThreshold = -1;
				}

                double[] goldstandard = new double[labelList.size()];
                double[] predictions = new double[labelList.size()];
                
                if(predictionS.length == 1 && predictionS.length == 1){
                	// singleLabel
                	goldstandard[Integer.parseInt(goldS[0])] = 1.;
                	predictions[Integer.parseInt(predictionS[0])] = 1.;
                }
                
                else{
                	// multiLabel
                	for (int i = 0; i < predictions.length; i++) {
                		predictions[i] = Double.parseDouble(predictionS[i]);
                	}
	                for (int i = 0; i < goldstandard.length; i++) {
	                    goldstandard[i] = Double.parseDouble(goldS[i]);
	                }
                }
	            
                SingleOutcome outcome;
        
                if (bipartitionThreshold == -1) {
                    outcome = new SingleOutcome(goldstandard, predictions, labelList);
                }
                else {
                    outcome = new SingleOutcome(goldstandard, predictions, bipartitionThreshold, labelList);
                }
                outcomes.add(outcome);
            }
        }
        br.close();
        if (outcomes.size() == 0) {
            throw new IOException("Wrong file format.");
        }
    }

    /**
     * Retrieves the list of labels from the respective header line of the id2outcome file, sorted
     * according to their index.
     * 
     * @param line
     *            the line of the id2outcome file which contains the labels and their indices
     * @return a list of labels sorted by index, ascending
     * @throws UnsupportedEncodingException
     */
    public static List<String> getLabels(String line) throws UnsupportedEncodingException
    {
        String[] numberedClasses = line.split(" ");
        List<String> labels = new ArrayList<String>();

        // filter #labels out and collect labels
        for (int i = 1; i < numberedClasses.length; i++) {
            // split one more time and take just the part with class name
            // e.g. 1=NPg, so take just right site
            String className = numberedClasses[i].split("=")[1];
            labels.add(URLDecoder.decode(className, "UTF-8"));
        }
        return labels;
    }

    /**
     * From a list of labels, creates a mapping from strings to indices.
     * 
     * @param labels
     *            a list of labels, in ascending order
     * @return a map with labels as keys and indices as values
     */
    public static Map<String, Integer> classNamesToMapping(List<String> labels)
    {

        Map<String, Integer> mapping = new HashMap<String, Integer>();
        for (int i = 0; i < labels.size(); i++) {
            mapping.put(labels.get(i), i);
        }
        return mapping;
    }

    /**
     * @return
     */
    public Set<SingleOutcome> getOutcomes()
    {
        return outcomes;
    }

    public void addAll(Collection<SingleOutcome> outcomes)
    {
        outcomes.addAll(outcomes);
    }

    public Properties getProperties()
    {
        Properties prop = new Properties();
        // TODO
        return prop;
    }
}
