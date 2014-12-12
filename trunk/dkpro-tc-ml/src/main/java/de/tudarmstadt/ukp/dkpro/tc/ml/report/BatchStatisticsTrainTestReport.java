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
package de.tudarmstadt.ukp.dkpro.tc.ml.report;


import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import au.com.bytecode.opencsv.CSVWriter;
import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.StringAdapter;
import de.tudarmstadt.ukp.dkpro.lab.task.Task;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants;
import de.tudarmstadt.ukp.dkpro.tc.ml.report.util.PrettyPrintUtils;


/**
 * Collects statistical evaluation results from TestTasks. Can be run on BatchTask level in TrainTest setups,
 * or on CV BatchTask level in CV setups.
 *
 * @author Johannes Daxenberger
 *
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

        boolean experimentHasClassifierBaseline = false;
        boolean experimentHasFeatureBaseline = false;

        for (TaskContextMetadata subcontext : getSubtasks()) {
        	// FIXME this is a bad hack
            if (subcontext.getType().contains("TestTask")) {
  

                Map<String, String> discriminatorsMap = getContext().getStorageService().retrieveBinary(subcontext.getId(),
                        Task.DISCRIMINATORS_KEY, new PropertiesAdapter()).getMap();
                Map<String, String> rConnectReport = getContext().getStorageService().retrieveBinary(subcontext.getId(),
                        STATISTICS_REPORT_TEST_TASK_FILENAME, new PropertiesAdapter()).getMap();

                                
                String blCl = rConnectReport.get(DIM_BASELINE_CLASSIFICATION_ARGS);
                String blFs = rConnectReport.get(DIM_BASELINE_FEATURE_SET);
                String blPp = rConnectReport.get(DIM_BASELINE_PIPELINE_PARAMS);
                
                // these are from DKPro Lab
                String trainFiles = String.valueOf(getDiscriminatorValue(discriminatorsMap, "files_training").hashCode());
                String testFiles = String.valueOf(getDiscriminatorValue(discriminatorsMap, "files_validation").hashCode());
                
                String experimentName = subcontext.getType().split("\\-")[1];
                String train = experimentName + "." + trainFiles;
                String test = experimentName + "." + testFiles;
                
                String cl = getDiscriminatorValue(discriminatorsMap, DIM_CLASSIFICATION_ARGS);
                String fs = getDiscriminatorValue(discriminatorsMap, DIM_FEATURE_SET); 
        		String pp = getDiscriminatorValue(discriminatorsMap, DIM_PIPELINE_PARAMS);
        		
        		int isBaseline = 0;
                if (blCl.equals(cl)) {
                    experimentHasClassifierBaseline = true;
                }
                if (blFs.equals(fs) && blPp.equals(pp)) {
                    experimentHasFeatureBaseline = true;
                }
                if (blCl.equals(cl) && blFs.equals(fs) && blPp.equals(pp)) {
                    isBaseline = 1;
                }
                
                String measuresString = rConnectReport.get(MEASURES);
                for(String mString : measuresString.split(";")){
                	String mName = mString.split(":")[0];
                    String mValue = mString.split(":")[1];
                    String clShort = PrettyPrintUtils.prettyPrintClassifier(cl);
                    String fsShort = PrettyPrintUtils.prettyPrintFeatureSet(fs, true);
                    String ppShort = PrettyPrintUtils.prettyPrintFeatureArgs(pp);
                    String fAllShort = fsShort + "," + ppShort;
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
        if (variableClassifier.size() > 1 && experimentHasFeatureBaseline) {
            throw new TextClassificationException("If you configure a baseline for your feature set, you may not test more than one classifier (argument).");
        }
        if (variableFeature.size() > 1 && experimentHasClassifierBaseline) {
            throw new TextClassificationException("If you configure a baseline for your classifier, you may not test more than one feature sets (or feature arguments).");
        }

        getContext().storeBinary(STATISTICS_REPORT_FILENAME, new StringAdapter(s));
    }
    
    private String getDiscriminatorValue(Map<String, String> discriminatorsMap, String discriminatorName)
        throws TextClassificationException
    {
    	for (String key : discriminatorsMap.keySet()) {
			if(key.split("\\|")[1].equals(discriminatorName)){
				return discriminatorsMap.get(key);
			}
		}
    	throw new TextClassificationException(discriminatorName + " not found in discriminators set.");
    }
}

