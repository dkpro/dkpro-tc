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
package de.tudarmstadt.ukp.dkpro.tc.mallet.report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;

import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.mallet.task.MalletTestTask;

/**
 * Writes a instanceId / outcome pair for each classification instance.
 * 
 * @deprecated As of release 0.7.0, only dkpro-tc-ml-crfsuite is supported
 * @author krishperumal11
 * 
 */
public class MalletOutcomeIDReport
    extends ReportBase
{
    public static final String ID_OUTCOME_KEY = "id2outcome.txt";

    @Override
    public void execute()
        throws Exception
    {
        File storage = getContext().getStorageLocation(Constants.TEST_TASK_OUTPUT_KEY, AccessMode.READONLY);
        File filePredictions = new File(storage, MalletTestTask.PREDICTIONS_KEY);
        File fileId2Outcome = new File(storage, ID_OUTCOME_KEY);
        BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filePredictions)), "UTF-8"));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileId2Outcome), "UTF-8"));
        String line = null;
        boolean header = false;
        int outcomeIndex = -1;
        int predictedOutcomeIndex = -1;
        int instanceIdIndex = -1;
        while ((line = br.readLine()) != null) {
        	if (!header) {
        		header = true;
        		String featureNames[] = line.split(" ");
        		for (int i = 0; i < featureNames.length; i++) {
        			if (featureNames[i].equals(MalletTestTask.OUTCOME_CLASS_LABEL_NAME)) {
        				outcomeIndex = i;
        			}
        			else if (featureNames[i].equals(MalletTestTask.PREDICTION_CLASS_LABEL_NAME)) {
        				predictedOutcomeIndex = i;
        			}
        			else if (featureNames[i].equals(Constants.ID_FEATURE_NAME)) {
        				instanceIdIndex = i;
        			}
        		}
        		continue;
        	}
        	if (!line.isEmpty()) {
        		String featureValues[] = line.split(" ");
        		bw.write(featureValues[instanceIdIndex] + "=" + featureValues[predictedOutcomeIndex]
        				+ " (is " + featureValues[outcomeIndex] + ")\n");
        		bw.flush();
        	}
        }
        br.close();
        bw.close();
    }
}