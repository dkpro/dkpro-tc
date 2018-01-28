/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;

/**
 * Container for id2outcome files.
 */
public class Id2Outcome
    implements Serializable
{

    private static final long serialVersionUID = -1273352385064025736L;
    private Set<SingleOutcome> outcomes;
    private String learningMode;

    /**
     * Character that is used for separating fields in the output file
     */
    public static final String SEPARATOR_CHAR = ";";
    /**
     * Character that is used for separating of single label weights of prediction and goldstandard
     */
    public static final String WEIGHTING_SEPARATOR_CHAR = ",";
    /**
     * Character that is used for separating of decimal values
     */
    public static final String DECIMAL_SEPARATOR_CHAR = ".";

    /**
     * Retrieve learning mode
     * 
     * @return the learning mode
     */
    public String getLearningMode()
    {
        return learningMode;
    }

    /**
     * Creates an empty id2outcome container.
     */
    public Id2Outcome()
    {
        this.outcomes = new HashSet<SingleOutcome>();
    }

    /**
     * Creates an {@link Id2Outcome} object from a file.
     * 
     * @param id2outcomeFile
     *            a file formatted according to the Id2Outcome layout
     * @param learningMode
     *            the learning mode, i.e. the value of {@link Constants#DIM_LEARNING_MODE}
     * @throws IOException
     * 			in case of errors
     */
    public Id2Outcome(File id2outcomeFile, String learningMode)
        throws IOException
    {
        this.outcomes = new HashSet<SingleOutcome>();
        this.learningMode = learningMode;

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(id2outcomeFile), "utf-8"));
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
                    throw new IOException("Wrong file format in id2outcome file ["
                            + id2outcomeFile.getAbsolutePath() + "]");
                }
                // line might contain several '=', split at the last one
                int idxMostRightHandEqual = line.lastIndexOf("=");
                String id = line.substring(0, idxMostRightHandEqual);
                String evaluationData = line.substring(idxMostRightHandEqual + 1);

                String[] splittedEvaluationData = evaluationData.split(";");
                String[] predictionS = splittedEvaluationData[0].split(",");
                String[] goldS = splittedEvaluationData[1].split(",");
                double bipartitionThreshold;
                try {
                    bipartitionThreshold = Double.valueOf(splittedEvaluationData[2]);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    bipartitionThreshold = -1;
                }

                double[] goldstandard;
                double[] predictions;

                if (learningMode.equals(Constants.LM_REGRESSION)) {
                    goldstandard = new double[1];
                    predictions = new double[1];

                    // one pair of pred/gold unless it is multi label
                    goldstandard[0] = Double.parseDouble(goldS[0]);
                    predictions[0] = Double.parseDouble(predictionS[0]);
                }
                else if (predictionS.length == 1 && goldS.length == 1) {
                    // singleLabel
                    goldstandard = new double[labelList.size()];
                    predictions = new double[labelList.size()];
                    goldstandard[Integer.parseInt(goldS[0])] = 1.;
                    predictions[Integer.parseInt(predictionS[0])] = 1.;
                }
                else {
                    // re-init arrays to make enough space for the labels
                    goldstandard = new double[labelList.size()];
                    predictions = new double[labelList.size()];

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
                    outcome = new SingleOutcome(goldstandard, predictions, labelList, id);
                }
                else {
                    outcome = new SingleOutcome(goldstandard, predictions, bipartitionThreshold,
                            labelList, id);
                }
                outcomes.add(outcome);
            }
        }
        br.close();
        if (outcomes.size() == 0) {
            throw new IOException("id2outcome file contains no outcomes ["
                    + id2outcomeFile.getAbsolutePath() + "]");
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
     * 			in case of encoding issues
     */
    public static List<String> getLabels(String line)
        throws UnsupportedEncodingException
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
     * Retrieve single outcomes from this {@link Id2Outcome} object.
     * 
     * @return the outcomes contained in this {@link Id2Outcome} object
     */
    public Set<SingleOutcome> getOutcomes()
    {
        return outcomes;
    }

    /**
     * Returns all predictions in a single array.
     * 
     * @return the array of predictions
     */
    public double[] getPredictions()
    {
        double[] predictions = new double[outcomes.size()];
        int i = 0;
        for (SingleOutcome outcome : outcomes) {
            predictions[i] = outcome.getPrediction()[0];
            i++;
        }
        return predictions;
    }

    /**
     * Returns all gold standard values in a single array.
     * 
     * @return the array of gold standard values
     */
    public double[] getGoldValues()
    {
        double[] golds = new double[outcomes.size()];
        int i = 0;
        for (SingleOutcome outcome : outcomes) {
            golds[i] = outcome.getGoldstandard()[0];
            i++;
        }
        return golds;
    }

    /**
     * Adds another {@link Id2Outcome} object to this {@link Id2Outcome}, effectively adding the
     * individual outcomes and setting the learning mode.
     * 
     * @param id2outcome
     *            an {@link Id2Outcome} object
     * @throws TextClassificationException
     */
    public void add(Id2Outcome id2outcome)
        throws TextClassificationException
    {
        if (this.learningMode == null) {
            this.learningMode = id2outcome.getLearningMode();
        }
        else {
            if (!this.learningMode.equals(id2outcome.getLearningMode())) {
                throw new TextClassificationException("Learning modes do not match");
            }
        }
        outcomes.addAll(id2outcome.getOutcomes());
    }

    /**
     * Homogenize the available outcomes data
     * 
     * @return the file location in a temporary file with the homogenized data as a human readable text
     * @throws Exception
     *         when exceptional things happen
     */
    public File homogenizeAggregatedFile()
        throws Exception
    {
        File tmpHomoFile = null;
        Set<SingleOutcome> newOutcomes = new HashSet<SingleOutcome>();

        if (this.learningMode.equals(Constants.LM_SINGLE_LABEL)
                || this.learningMode.equals(Constants.LM_MULTI_LABEL)) {

            Set<String> aggregatedLabels = new TreeSet<String>();
            for (SingleOutcome outcome : this.getOutcomes()) {
                aggregatedLabels.addAll(outcome.getLabels());
            }

            Map<String, Integer> aggregatedLabelsMapping = classNamesToMapping(
                    new LinkedList<String>(aggregatedLabels));
            List<String> aggregatedLabelsList = new ArrayList<String>();
            aggregatedLabelsList.addAll(aggregatedLabels);

            int aggregatedMappingSize = aggregatedLabelsMapping.size();
            for (SingleOutcome outcome : this.getOutcomes()) {
                double bipartitionThreshold = outcome.getBipartitionThreshold();
                String id = outcome.getId();
                List<String> labels = outcome.getLabels();
                Map<String, Integer> localMappingLabelsToIndices = classNamesToMapping(labels);

                double[] oldIndexedGoldstandards = outcome.getGoldstandard();
                double[] oldIndexedPredictions = outcome.getPrediction();

                double[] newIndexedGoldstandards = new double[aggregatedMappingSize];
                double[] newIndexedPredictions = new double[aggregatedMappingSize];
                for (String label : aggregatedLabelsMapping.keySet()) {
                    int index = aggregatedLabelsMapping.get(label);
                    if (localMappingLabelsToIndices.containsKey(label)) {
                        Integer oldLabelsIndex = localMappingLabelsToIndices.get(label);
                        double goldstandardValue = oldIndexedGoldstandards[oldLabelsIndex];
                        newIndexedGoldstandards[index] = goldstandardValue;

                        double predictionValue = oldIndexedPredictions[oldLabelsIndex];
                        newIndexedPredictions[index] = predictionValue;
                    }
                    else {
                        newIndexedGoldstandards[index] = -1;
                        newIndexedPredictions[index] = -1;
                    }
                }

                SingleOutcome newOutcome = new SingleOutcome(newIndexedGoldstandards,
                        newIndexedPredictions, bipartitionThreshold, aggregatedLabelsList, id);
                newOutcomes.add(newOutcome);
            }
            
            tmpHomoFile = File
                    .createTempFile("homoAggregatedFile" + System.currentTimeMillis(), ".tmp");
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(tmpHomoFile), "utf-8"));
            generateOutcomesText(writer,newOutcomes, aggregatedLabelsList);
            writer.close();
        }
        return tmpHomoFile;
    }

    /***
     * Generate outcomes based human readable text
     * 
     * @param singleOutcomes
     * @param labelsList
     * @throws UnsupportedEncodingException
     */
    private void generateOutcomesText(Writer writer, Set<SingleOutcome> singleOutcomes, List<String> labelsList)
        throws Exception
    {
        writer.append(generateHeader(labelsList));

        for (SingleOutcome outcome : singleOutcomes) {
            writer.append("\n");
            writer.append(outcome.getId());
            writer.append("=");
            double[] predictions = outcome.getPrediction();
            for (int i = 0; i < predictions.length; i++) {
                double value = predictions[i];
                String formattedStringValue = getFormattedStringValue(value);
                writer.append(formattedStringValue);

                if (i < predictions.length - 1) {
                    writer.append(WEIGHTING_SEPARATOR_CHAR);
                }
            }

            writer.append(SEPARATOR_CHAR);
            double[] goldstandards = outcome.getGoldstandard();
            for (int i = 0; i < goldstandards.length; i++) {
                double value = goldstandards[i];
                String formattedStringValue = getFormattedStringValue(value);
                writer.append(formattedStringValue);

                if (i < predictions.length - 1) {
                    writer.append(WEIGHTING_SEPARATOR_CHAR);
                }
            }

            writer.append(SEPARATOR_CHAR);
            writer.append(""+outcome.getBipartitionThreshold());
        }
    }

    private String getFormattedStringValue(double value)
    {
        if (Double.isNaN(value)) {
            return String.valueOf(value);
        }
        else {
            DecimalFormat df = new DecimalFormat();
            if (this.learningMode.equals(Constants.LM_SINGLE_LABEL)) {
                df = new DecimalFormat("0");
            }
            else if (this.learningMode.equals(Constants.LM_MULTI_LABEL)) {
                df = new DecimalFormat("0.000");
            }
            String strValue = df.format(value);
            return strValue.replace(WEIGHTING_SEPARATOR_CHAR, DECIMAL_SEPARATOR_CHAR);
        }
    }

    private StringBuilder generateHeader(List<String> labels)
        throws UnsupportedEncodingException
    {
        StringBuilder comment = new StringBuilder();
        comment.append("ID=PREDICTION" + SEPARATOR_CHAR + "GOLDSTANDARD" + SEPARATOR_CHAR
                + "THRESHOLD" + "\n" + "labels");

        // add numbered indexing of labels: e.g. 0=NPg, 1=JJ
        for (int i = 0; i < labels.size(); i++) {
            comment.append(
                    " " + String.valueOf(i) + "=" + URLEncoder.encode(labels.get(i), "UTF-8"));
        }
        return comment;
    }

}
