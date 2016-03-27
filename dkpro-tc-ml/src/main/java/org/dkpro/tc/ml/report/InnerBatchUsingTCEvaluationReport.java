/*******************************************************************************
 * Copyright 2015
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.Task;
import org.dkpro.lab.task.TaskContextMetadata;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.evaluation.Id2Outcome;

/**
 * Collects the results from fold-runs in a crossvalidation setting and copies them into the upper
 * level task context.
 */
public class InnerBatchUsingTCEvaluationReport
    extends BatchReportBase
    implements Constants
{
    @Override
    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();
        Id2Outcome overallOutcome = new Id2Outcome();
        Properties prop = new Properties();
        Set<Object> discriminatorsToExclude = new HashSet<Object>();
        for (TaskContextMetadata subcontext : getSubtasks()) {
            // FIXME this is a bad hack
            if (subcontext.getType().contains("TestTask")) {
                Map<String, String> discriminatorsMap = store.retrieveBinary(subcontext.getId(),
                        Task.DISCRIMINATORS_KEY, new PropertiesAdapter()).getMap();
                String mode = getDiscriminatorValue(discriminatorsMap, DIM_LEARNING_MODE);
                File id2outcomeFile = getContext().getStorageService().getStorageFolder(subcontext.getId(), ID_OUTCOME_KEY);
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

        FileOutputStream fos = new FileOutputStream(new File(getContext().getStorageLocation(
                Constants.TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE)
                + "/" + Constants.SERIALIZED_ID_OUTCOME_KEY));
        ObjectOutputStream outputStream = new ObjectOutputStream(fos);
        outputStream.writeObject(overallOutcome);
        outputStream.close();
        
        // write out a homogenized human readable file
        String homogenizedOverallOutcomes = overallOutcome.homogenizeAggregatedFile();
        String[] homogenizedLines = homogenizedOverallOutcomes.split("\n");
        
        Properties props = new Properties();
        String header = "";
        //header = header + homogenizedLines[0] + "\n" + homogenizedLines[1];       
        for (int i = 0; i < homogenizedLines.length; i++) {
        	if (i <= 1){
        		header = header + homogenizedLines[i];     
        		if (i == 0){
        			header = header + "\n";
        		}   		        		       		
        	}
        	else{
        		// line might contain several '=', split at the last one
        		int idxMostRightHandEqual = homogenizedLines[i].lastIndexOf("=");
                String id = homogenizedLines[i].substring(0, idxMostRightHandEqual);
                String evaluationData = homogenizedLines[i].substring(idxMostRightHandEqual + 1);
        		props.setProperty(id, evaluationData); 
        	}
		}        
        getContext().storeBinary(ID_HOMOGENIZED_OUTCOME_KEY,
                new PropertiesAdapter(props, header));
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