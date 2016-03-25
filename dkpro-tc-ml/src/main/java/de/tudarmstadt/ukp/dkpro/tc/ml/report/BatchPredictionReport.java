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
package de.tudarmstadt.ukp.dkpro.tc.ml.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.reporting.FlexTable;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.Task;
import org.dkpro.lab.task.TaskContextMetadata;

import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

/**
 * A report which collects results from all executed subtasks that contain 
 * "ExtractFeaturesAndPredictTask" in their type and writes them into a human-readable format, one
 * file per configuration.
 * 
 * @author daxenberger
 */
public class BatchPredictionReport
    extends BatchReportBase
    implements Constants
{

    private static final String report_name = "PredictionReport";
    private static final String predicted_value = "Prediction";

    @Override
    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();

        FlexTable<String> table = FlexTable.forClass(String.class);

        for (TaskContextMetadata subcontext : getSubtasks()) {
            // FIXME this is a bad hack
            if (subcontext.getType().contains("ExtractFeaturesAndPredictTask")) {

                Map<String, String> discriminatorsMap = store.retrieveBinary(subcontext.getId(),
                        Task.DISCRIMINATORS_KEY, new PropertiesAdapter()).getMap();

                // deserialize file
                FileInputStream f = new FileInputStream(store.getStorageFolder(subcontext.getId(),
                        PREDICTION_MAP_FILE_NAME));
                ObjectInputStream s = new ObjectInputStream(f);
                Map<String, List<String>> resultMap = (Map<String, List<String>>) s.readObject();
                s.close();

                // write one file per batch
                // in files: one line per instance

                for (String id : resultMap.keySet()) {
                    Map<String, String> row = new HashMap<String, String>();
                    row.put(predicted_value, StringUtils.join(resultMap.get(id), ","));
                    table.addRow(id, row);
                }
                // create a separate output folder for each execution of
                // ExtractFeaturesAndPredictTask, 36 is the length of the UUID hash
                File contextFolder = store.getStorageFolder(getContext().getId(),
                        subcontext.getId().substring(subcontext.getId().length() - 36));
                // Excel cannot cope with more than 255 columns
                if (table.getColumnIds().length <= 255) {
                    getContext().storeBinary(
                            contextFolder.getName() + System.getProperty("file.separator")
                                    + report_name
                                    + SUFFIX_EXCEL,
                            table.getExcelWriter());
                }
                getContext().storeBinary(
                        contextFolder.getName() + System.getProperty("file.separator")
                                + report_name
                                + SUFFIX_CSV,
                        table.getCsvWriter());
                getContext()
                        .storeBinary(contextFolder.getName() + System.getProperty("file.separator")
                                + Task.DISCRIMINATORS_KEY, new PropertiesAdapter(discriminatorsMap));
            }
        }

        // output the location of the batch evaluation folder
        // otherwise it might be hard for novice users to locate this
        File dummyFolder = store.getStorageFolder(getContext().getId(), "dummy");
        // TODO can we also do this without creating and deleting the dummy folder?
        getContext().getLoggingService().message(getContextLabel(),
                "Storing detailed results in:\n" + dummyFolder.getParent() + "\n");
        dummyFolder.delete();
    }
}