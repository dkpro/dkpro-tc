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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import weka.core.SerializationHelper;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants;
import de.tudarmstadt.ukp.dkpro.tc.ml.report.BatchStatisticsTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.evaluation.MekaEvaluationUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.WekaTestTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.MultilabelResult;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;


/**
 * Writes the file needed for {@link BatchStatisticsTrainTestReport}.
 * Collects information used for statistical evaluation.
 * 
 * @author Johannes Daxenberger
 *
 */
public class WekaStatisticsReport
    extends ReportBase
    implements Constants, ReportConstants
{

    @Override
    public void execute()
        throws Exception
    {
        File storage = getContext().getStorageLocation(WekaTestTask.TEST_TASK_OUTPUT_KEY,
                AccessMode.READONLY);
        boolean multiLabel = getDiscriminators().get(WekaTestTask.class.getName() + "|" + DIM_LEARNING_MODE)
                .equals(Constants.LM_MULTI_LABEL);
        String blCl = getDiscriminators().get(WekaTestTask.class.getName() +"|" + DIM_BASELINE_CLASSIFICATION_ARGS);
        String blFs = getDiscriminators().get(WekaTestTask.class.getName() +"|" + DIM_BASELINE_FEATURE_SET);
        String blPp = getDiscriminators().get(WekaTestTask.class.getName() +"|" + DIM_BASELINE_PIPELINE_PARAMS);

        Properties props = new Properties();

        File evaluationFile = new File(storage.getAbsolutePath() + "/"
                + WekaClassificationAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.evaluationFile));
        Map<String, Double> results = new HashMap<String, Double>();
        
        if(blCl != null && blFs != null && blPp != null){
            props.setProperty(DIM_BASELINE_CLASSIFICATION_ARGS, blCl);
            props.setProperty(DIM_BASELINE_FEATURE_SET, blFs);
            props.setProperty(DIM_BASELINE_PIPELINE_PARAMS, blPp);
        }
        else{
            props.setProperty(DIM_BASELINE_CLASSIFICATION_ARGS, "");
            props.setProperty(DIM_BASELINE_FEATURE_SET, "");
            props.setProperty(DIM_BASELINE_PIPELINE_PARAMS, "");
        }

        if (multiLabel) {
            // ============= multi-label setup ======================
        	MultilabelResult r = WekaUtils.readMlResultFromFile(evaluationFile); 

            double t = r.getBipartitionThreshold();
            double[] thresholdArray = new double[r.getGoldstandard()[0].length];
            Arrays.fill(thresholdArray, t);

            Map<String, Double> mekaResults = MekaEvaluationUtils.calcMLStats(r.getPredictions(),
                    r.getGoldstandard(), thresholdArray);
            results = mekaResults;
        }

        // ============= single-label setup ======================
        else {
            weka.classifiers.Evaluation eval = (weka.classifiers.Evaluation) SerializationHelper
                    .read(evaluationFile.getAbsolutePath());
            
            results.put(CORRECT, eval.correct());
            results.put(INCORRECT, eval.incorrect());
            results.put(PCT_CORRECT, eval.pctCorrect());
            results.put(PCT_INCORRECT, eval.pctIncorrect());
            results.put(PCT_UNCLASSIFIED, eval.pctUnclassified());
            results.put(WGT_FMEASURE, eval.weightedFMeasure());
            results.put(WGT_PRECISION, eval.weightedPrecision());
            results.put(WGT_RECALL, eval.weightedRecall());
        }
        // ================================================

    	List<String> buffer = new ArrayList<String>();
        for (String s : results.keySet()) {
        	buffer.add(s + ":" + results.get(s));
        }
        props.setProperty(MEASURES, StringUtils.join(buffer, ";"));

        // Write out properties
        getContext().storeBinary(R_CONNECT_REPORT_TEST_TASK_FILENAME, new PropertiesAdapter(props));

    }
}