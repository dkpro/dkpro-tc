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
import java.io.IOException;
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
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.SmallContingencyTables;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.WekaTestTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.MultilabelResult;
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
    private static File mlResults;

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
         mlResults = new File(storage.getAbsolutePath()
                + "/"
                + WekaClassificationAdapter.getInstance()
                        .getFrameworkFilename(AdapterNameEntries.evaluationFile));

        boolean multiLabel = getDiscriminators()
                .get(WekaTestTask.class.getName() + "|" + Constants.DIM_LEARNING_MODE)
                .equals(Constants.LM_MULTI_LABEL);
        boolean regression = getDiscriminators()
                .get(WekaTestTask.class.getName() + "|" + Constants.DIM_LEARNING_MODE)
                .equals(Constants.LM_REGRESSION);

        Instances predictions = WekaUtils.getInstances(arff, multiLabel);
        
       
        List<String> labels = WekaUtils.getClassLabels(predictions, multiLabel);
        class2number = SmallContingencyTables.classNamesToMapping(labels);
        StringBuilder comment = new StringBuilder();
        comment.append("ID=PREDICTION" + SEPARATOR_CHAR + "GOLDSTANDARD" + 
				SEPARATOR_CHAR + "THRESHOLD" + "\n" + "labels");
        
        // add numbered indexing of labels: e.g. 0=NPg, 1=JJ
        for (int i = 0; i < labels.size(); i++) {
        	comment.append(" " + String.valueOf(i) + "=" + labels.get(i));	
		}
    
        Properties props = generateProperties(predictions, multiLabel, regression, labels);
        getContext().storeBinary(ID_OUTCOME_KEY,
                new PropertiesAdapter(props, comment.toString()));
    }

    protected static Properties generateProperties(Instances predictions, boolean isMultiLabel, 
    		boolean isRegression, List<String> labels) throws ClassNotFoundException, IOException
    {
        Properties props = new Properties();
        String[] classValues = new String[predictions.numClasses()];

        for (int i = 0; i < predictions.numClasses(); i++) {
            classValues[i] = predictions.classAttribute().value(i);
        }

        int attOffset = predictions.attribute(Constants.ID_FEATURE_NAME).index();
        
        if (isMultiLabel) {
        	MultilabelResult r = WekaUtils.readMlResultFromFile(mlResults);
        	int[][] goldmatrix = r.getGoldstandard();
        	double[][] predictionsmatrix = r.getPredictions();
        	double bipartition = r.getBipartitionThreshold();
        	
        	for(int i = 0; i < goldmatrix.length; i++){
        		Double[] predList = new Double[labels.size()];
        		Integer[] goldList = new Integer[labels.size()];
        		for(int j = 0; j < goldmatrix[i].length; j++){
        			int classNo = class2number.get(labels.get(j));
        			goldList[classNo] = goldmatrix[i][j];
        			predList[classNo] = predictionsmatrix[i][j];
        		}
                String s = (StringUtils.join(predList, ",") + SEPARATOR_CHAR + StringUtils
                        .join(goldList, ",") + SEPARATOR_CHAR + bipartition);
                props.setProperty(predictions.get(i).stringValue(attOffset), s);
        	}
        }
        // single-label
        else {
            for (Instance inst : predictions) {
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