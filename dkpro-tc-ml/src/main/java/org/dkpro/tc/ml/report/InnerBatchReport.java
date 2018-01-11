/*******************************************************************************
 * Copyright 2017
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.Task;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.evaluation.Id2Outcome;

/**
 * Collects the results from fold-runs in a crossvalidation setting and copies them into the upper
 * level task context.
 */
public class InnerBatchReport
    extends BatchReportBase
    implements Constants
{
    public InnerBatchReport(){
        //required by groovy
    }
    
    @Override
    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();
        Id2Outcome overallOutcome = new Id2Outcome();
        Properties prop = new Properties();
        Set<Object> discriminatorsToExclude = new HashSet<Object>();
        
        
        List<String> mlaContextIds = getContextIdOfMachineLearningAdapter();
        
        for(String mla : mlaContextIds){	
            if (TcTaskTypeUtil.isMachineLearningAdapterTask(store, mla)) {
                Map<String, String> discriminatorsMap = store.retrieveBinary(mla,
                        Task.DISCRIMINATORS_KEY, new PropertiesAdapter()).getMap();
                String mode = getDiscriminatorValue(discriminatorsMap, DIM_LEARNING_MODE);
                File id2outcomeFile = getContext().getStorageService().locateKey(mla,
                        ID_OUTCOME_KEY);
                Id2Outcome id2outcome = new Id2Outcome(id2outcomeFile, mode);
                overallOutcome.add(id2outcome);
                for (Object key : discriminatorsMap.keySet()) {
                    if (prop.containsKey(key)
                            && !((String) prop.get(key)).equals(discriminatorsMap.get(key))) {
                        discriminatorsToExclude.add(key);
                    }
                    prop.setProperty((String) key, discriminatorsMap.get((String) key));
                }
            }
        }
        
        // remove keys with altering values
        for (Object key : discriminatorsToExclude) {
            prop.remove(key);
        }
        getContext().storeBinary(Constants.DISCRIMINATORS_KEY_TEMP, new PropertiesAdapter(prop));

        File folder = getContext().getFolder(Constants.TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE);
        FileOutputStream fos = new FileOutputStream(
                new File(folder, Constants.SERIALIZED_ID_OUTCOME_KEY));
        ObjectOutputStream outputStream = new ObjectOutputStream(fos);
        outputStream.writeObject(overallOutcome);
        outputStream.close();

        // write out a homogenized human readable file
        File homogenizedOverallOutcomes = overallOutcome.homogenizeAggregatedFile();

        writeHomogenizedOutcome(homogenizedOverallOutcomes);
    }
    
    /**
     * Reads the Attributes.txt of the CV task folder which holds the name of the folder belonging to this run
     * @return
     * 		a list of context ids of the machine learning adapter folders
     * @throws Exception
     * 		in case read operations fail
     */
    private List<String> getContextIdOfMachineLearningAdapter() throws Exception {
    	
        File cvTaskAttributeFile = getContext().getFile(Task.ATTRIBUTES_KEY, AccessMode.READONLY);
        List<String> foldersOfSingleRuns = getFoldersOfSingleRuns(cvTaskAttributeFile);
        
        
        List<String> mlaContextIdsOfCvRun = new ArrayList<>();
        for(String f : foldersOfSingleRuns){
        	if (TcTaskTypeUtil.isMachineLearningAdapterTask(getContext().getStorageService(), f)) {
        		mlaContextIdsOfCvRun.add(f);
        	}
        }
    	
		return mlaContextIdsOfCvRun;
	}

	private List<String> getFoldersOfSingleRuns(File attributesTXT)
            throws Exception
        {
            List<String> readLines = FileUtils.readLines(attributesTXT, "utf-8");

            int idx = 0;
            for (String line : readLines) {
                if (line.startsWith("Subtask")) {
                    break;
                }
                idx++;
            }
            String line = readLines.get(idx);
            int start = line.indexOf("[") + 1;
            int end = line.indexOf("]");
            String subTasks = line.substring(start, end);

            String[] tasks = subTasks.split(",");

            List<String> results = new ArrayList<>();

            for (String task : tasks) {
                if (TcTaskTypeUtil.isMachineLearningAdapterTask(getContext().getStorageService(),
                        task.trim())) {
                    results.add(task.trim());
                }
            }

            return results;
        }

    private void writeHomogenizedOutcome(File homogenizedOverallOutcomes) throws Exception
    {
        if(homogenizedOverallOutcomes == null){
            //not single or multi label modoe
            return;
        }
        
        BufferedReader tmpFileReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(homogenizedOverallOutcomes), "utf-8"));
        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(
                        getContext().getFile(ID_HOMOGENIZED_OUTCOME_KEY, AccessMode.READWRITE)),
                "utf-8"));

        String line = null;
        int idx=0;
        while ((line = tmpFileReader.readLine()) != null) {
            if(idx <= 1){
                writer.append("#"+line+"\n");
                idx++;
                continue;
            }
            int idxMostRightHandEqual = line.lastIndexOf("=");
            String id = line.substring(0, idxMostRightHandEqual);
            String evaluationData = line.substring(idxMostRightHandEqual + 1);
            writer.append(id + "=" + evaluationData);
            writer.append("\n");
        }
        writer.close();
        tmpFileReader.close();        
    }
}