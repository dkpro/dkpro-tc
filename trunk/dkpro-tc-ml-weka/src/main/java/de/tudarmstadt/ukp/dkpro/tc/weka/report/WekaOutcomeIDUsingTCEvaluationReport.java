/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.ContingencyTable;
import de.tudarmstadt.ukp.dkpro.tc.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.WekaTestTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

/**
 * Writes a instanceId / outcome data for each classification instance.
 * 
 * @author zesch
 * @author Andriy Nadolskyy
 * 
 */
public class WekaOutcomeIDUsingTCEvaluationReport
    extends ReportBase
{
    /**
     * Name of the file where the instanceID / outcome pairs are stored
     */
    public static final String ID_OUTCOME_KEY = "id2outcome.txt";
    /**
     * Character that is used for separating fields in the output file
     */
    public static final String SEPARATOR_CHAR = ";";
    
    private static Map<String, Integer> class2number;

    @Override
    public void execute()
        throws Exception
    {
        File storage = getContext().getStorageLocation(WekaTestTask.TEST_TASK_OUTPUT_KEY,
                AccessMode.READONLY);
        File arff = new File(storage.getAbsolutePath()
                + "/"
                + WekaClassificationAdapter.getInstance()
                        .getFrameworkFilename(AdapterNameEntries.predictionsFile));

        boolean multiLabel = getDiscriminators()
                .get(WekaTestTask.class.getName() + "|learningMode")
                .equals(Constants.LM_MULTI_LABEL);
        boolean regression = getDiscriminators()
                .get(WekaTestTask.class.getName() + "|learningMode")
                .equals(Constants.LM_REGRESSION);
        Instances predictions = WekaUtils.getInstances(arff, multiLabel);
        
        /*
         * FIXME: 
         * 1) WekaUtils.getClassLabels(...) - "Only works for single-label outcome" - check it
         * 2) check if variable "labels" should be updated due to "empty prediction":
         * all predictions are under threshold value (in case of multilable)
         */
        List<String> labels = WekaUtils.getClassLabels(predictions, multiLabel);
        class2number = ContingencyTable.classNamesToMapping(labels);
        StringBuilder comment = new StringBuilder();
        comment.append("ID=PREDICTION" + SEPARATOR_CHAR + "GOLDSTANDARD" + 
				SEPARATOR_CHAR + "THRESHOLD" + "\n" + "labels");
        for (String label : labels) {
        	comment.append(" " + label);			
		}        
        Properties props = generateProperties(predictions, multiLabel, regression);
        getContext().storeBinary(ID_OUTCOME_KEY,
                new PropertiesAdapter(props, comment.toString()));
    }

    protected static Properties generateProperties(Instances predictions, boolean isMultiLabel, 
    		boolean isRegression)
    {
        Properties props = new Properties();
        String[] classValues = new String[predictions.numClasses()];

        for (int i = 0; i < predictions.numClasses(); i++) {
            classValues[i] = predictions.classAttribute().value(i);
        }

        int attOffset = predictions.attribute(AddIdFeatureExtractor.ID_FEATURE_NAME).index();
        for (Instance inst : predictions) {
            if (isMultiLabel) {
                List<String> predictionOutcomes = new ArrayList<String>();
                List<String> goldOutcomes = new ArrayList<String>();
                for (int i = 0; i < predictions.classIndex(); i++) {
                    if (inst.value(predictions.attribute(i)) == 1.) {
                        goldOutcomes.add(predictions.attribute(i).name());
                    }
                    if (inst.value(predictions.attribute(i + predictions.classIndex())) == 1.) {
                        predictionOutcomes.add(predictions.attribute(i).name());
                    }
                }
                /*
                 * FIXME: 
                 * 1) add threshold 
                 * 2) check if class2number should be updated due to "empty prediction": 
                 * all predictions are under threshold value
                 * 3) map predictionOutcomes and goldOutcomes to Integer values
                 */
                String s = (StringUtils.join(predictionOutcomes, ",") + SEPARATOR_CHAR + StringUtils
                        .join(goldOutcomes, ","));
                props.setProperty(inst.stringValue(attOffset), s);
            }
            else {
                Double gold;

                try {
                    gold = new Double(inst.value(predictions
                            .attribute(Constants.CLASS_ATTRIBUTE_NAME
                                    + WekaUtils.COMPATIBLE_OUTCOME_CLASS)));
                }
                catch (NullPointerException e) {
                    // if train and test data have not been balanced
                    gold = new Double(inst.value(predictions
                            .attribute(Constants.CLASS_ATTRIBUTE_NAME)));
                }
                Attribute gsAtt = predictions.attribute(WekaTestTask.PREDICTION_CLASS_LABEL_NAME);
                Double prediction = new Double(inst.value(gsAtt));
                if (!isRegression) {
                	Integer predictionAsNumber = class2number.get(gsAtt.value(prediction.intValue()));
                	Integer goldAsNumber = class2number.get(classValues[gold.intValue()]);
                    props.setProperty(
                            inst.stringValue(attOffset), predictionAsNumber + SEPARATOR_CHAR 
                            + goldAsNumber + SEPARATOR_CHAR + String.valueOf(0));
                }
                else {
                    // the outcome is numeric
                    props.setProperty(inst.stringValue(attOffset), prediction + SEPARATOR_CHAR
                            + gold + SEPARATOR_CHAR + String.valueOf(0));
                }
            }
        }
        return props;
    }
}