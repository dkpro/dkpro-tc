/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.ml.report;

import static org.dkpro.tc.core.util.ReportUtils.getDiscriminatorValue;

import java.io.File;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.storage.impl.StringAdapter;
import org.dkpro.lab.task.Task;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.util.ReportConstants;
import org.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import org.dkpro.tc.evaluation.evaluator.EvaluatorFactory;
import org.dkpro.tc.ml.report.util.PrettyPrintUtils;

import au.com.bytecode.opencsv.CSVWriter;


/**
 * Collects statistical evaluation results from TestTasks. Can be run on BatchTask level in
 * TrainTest setups, or on CV BatchTask level in CV setups.
 */
public class BatchStatisticsTrainTestReport
    extends BatchReportBase
    implements Constants, ReportConstants
{

    @Override
    public void execute()
        throws Exception
    {
        StringWriter sWriter = new StringWriter();
        CSVWriter csv = new CSVWriter(sWriter, ';');

        HashSet<String> variableClassifier = new HashSet<String>();
        HashSet<String> variableFeature = new HashSet<String>();

        boolean experimentHasBaseline = false;

        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (TcTaskTypeUtil.isMachineLearningAdapterTask(getContext().getStorageService(), subcontext.getId())) {
  

                Map<String, String> discriminatorsMap = getContext().getStorageService().retrieveBinary(subcontext.getId(),
                        Task.DISCRIMINATORS_KEY, new PropertiesAdapter()).getMap();
                File id2outcomeFile = getContext().getStorageService().locateKey(subcontext.getId(), ID_OUTCOME_KEY);
                String mode = getDiscriminatorValue(discriminatorsMap, DIM_LEARNING_MODE);

                                
                String blCl = getDiscriminatorValue(discriminatorsMap, DIM_BASELINE_CLASSIFICATION_ARGS);
                String blFs = getDiscriminatorValue(discriminatorsMap, DIM_BASELINE_FEATURE_SET);
                
                String trainFiles;
                String testFiles;
                
                // CV
                if(!getDiscriminatorValue(discriminatorsMap, DIM_FILES_TRAINING).equals("null") &&
                        !getDiscriminatorValue(discriminatorsMap, DIM_FILES_VALIDATION).equals("null")){
                     trainFiles = String.valueOf(getDiscriminatorValue(discriminatorsMap, DIM_FILES_TRAINING).hashCode());
                     testFiles = String.valueOf(getDiscriminatorValue(discriminatorsMap, DIM_FILES_VALIDATION).hashCode());
                }
                // TrainTest
                else{
                    trainFiles = String.valueOf(getDiscriminatorValue(discriminatorsMap, DIM_READER_TRAIN).hashCode());
                    testFiles = String.valueOf(getDiscriminatorValue(discriminatorsMap, DIM_READER_TEST).hashCode());
                }
                
                String experimentName = subcontext.getType().split("\\-")[1];
                String train = experimentName + "." + trainFiles;
                String test = experimentName + "." + testFiles;
                
                String cl = getDiscriminatorValue(discriminatorsMap, DIM_CLASSIFICATION_ARGS);
                String fs = getDiscriminatorValue(discriminatorsMap, DIM_FEATURE_SET); 
        		
        		int isBaseline = 0;
                if (blCl.equals(cl) && blFs.equals(fs)) {
                    isBaseline = 1;
                    experimentHasBaseline = true;
                }
                
                EvaluatorBase evaluator = EvaluatorFactory.createEvaluator(id2outcomeFile,
                        mode, true, false);
                Map<String, Double> resultMap = evaluator.calculateMicroEvaluationMeasures();

                for (String mString : resultMap.keySet()) {
                    String mName = mString;
                    String mValue = String.valueOf(resultMap.get(mString));
                    String clShort = PrettyPrintUtils.prettyPrintClassifier(cl);
                    String fsShort = PrettyPrintUtils.prettyPrintFeatureSet(fs, true);
                    String fAllShort = fsShort;
                    // expected format: Train;Test;Classifier;FeatureSet;Measure;Value;IsBaseline
                    csv.writeNext(Arrays.asList(train, test, clShort, fAllShort, mName, mValue,
                            String.valueOf(isBaseline)).toArray(new String[] {}));
                    variableClassifier.add(clShort);
                    variableFeature.add(fAllShort);
                }
            }
        }
        String s = sWriter.toString();
        csv.close();
        if (variableClassifier.size() > 1 && variableFeature.size() > 1 && experimentHasBaseline) {
            throw new TextClassificationException("If you configure a baseline, you may test either only one classifier (arguments) or one feature set (arguments).");
        }

        getContext().storeBinary(STATISTICS_REPORT_FILENAME, new StringAdapter(s));
    }
}

