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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
                File id2outcomeFile = getContext().getStorageService().locateKey(subcontext.getId(),
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