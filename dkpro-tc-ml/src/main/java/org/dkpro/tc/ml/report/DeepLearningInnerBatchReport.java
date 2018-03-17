/*******************************************************************************
 * Copyright 2018
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.Task;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;
import org.dkpro.tc.ml.report.util.ID2OutcomeCombiner;

/**
 * Collects the results from fold-runs in a crossvalidation setting and copies them into the upper
 * level task context.
 */
public class DeepLearningInnerBatchReport
    extends TcBatchReportBase
    implements Constants
{
    public DeepLearningInnerBatchReport()
    {
        // required by groovy
    }

    @Override
    public void execute() throws Exception
    {
        StorageService store = getContext().getStorageService();
        Properties prop = new Properties();

        List<File> id2outcomeFiles = new ArrayList<>();
        Set<String> ids = getTaskIdsFromMetaData(getSubtasks());

        for (String id : ids) {

            if (!TcTaskTypeUtil.isMachineLearningAdapterTask(store, id)) {
                continue;
            }

            Map<String, String> discriminatorsMap = store
                    .retrieveBinary(id, Task.DISCRIMINATORS_KEY, new PropertiesAdapter()).getMap();

            File id2outcomeFile = store.locateKey(id, Constants.ID_OUTCOME_KEY);
            id2outcomeFiles.add(id2outcomeFile);

            for (Entry<String, String> e : discriminatorsMap.entrySet()) {
                String key = e.getKey();
                String value = e.getValue();

                prop.setProperty(key, value);
            }
        }

        String learningMode = getDiscriminator(store, ids, DIM_LEARNING_MODE);

        ID2OutcomeCombiner<String> aggregator = new ID2OutcomeCombiner<>(learningMode);
        for (File id2o : id2outcomeFiles) {
            aggregator.add(id2o, learningMode);
        }

        writeCombinedOutcomeReport(aggregator.generateId2OutcomeFile());
    }

    private void writeCombinedOutcomeReport(String payload) throws Exception
    {
        File file = getContext().getFile(FILE_COMBINED_ID_OUTCOME_KEY, AccessMode.READWRITE);
        Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), "utf-8"));

        writer.write(payload);

        writer.close();
    }
}