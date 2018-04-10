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

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.Task;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;
import org.dkpro.tc.core.util.ReportUtils;
import org.dkpro.tc.core.util.TcFlexTable;
import org.dkpro.tc.ml.report.util.MetricComputationUtil;

/**
 * Collects the final evaluation results in a train/test setting.
 */
public class BatchTrainTestReport
    extends TcBatchReportBase
    implements Constants
{
    private final List<String> discriminatorsToExclude = Arrays
            .asList(new String[] { DIM_FILES_VALIDATION, DIM_FILES_TRAINING });

    public BatchTrainTestReport()
    {
        // required by groovy
    }

    @Override
    public void execute() throws Exception
    {
        StorageService store = getContext().getStorageService();
        TcFlexTable<String> table = TcFlexTable.forClass(String.class);
        table.setDefaultValue("");

        Set<String> idPool = getTaskIdsFromMetaData(getSubtasks());

        for (String id : idPool) {

            if (!TcTaskTypeUtil.isFacadeTask(store, id) //Shallow TC uses always the facade task at this point
                    && !TcTaskTypeUtil.isMachineLearningAdapterTask(store, id)) //Deep TC comes directly with the MLA 
            {
                continue;
            }

            Set<String> wrapped = new HashSet<>();
            wrapped.add(id);
            Set<String> subTaskId = collectTasks(wrapped);

            for (String subId : subTaskId) {

                if (!TcTaskTypeUtil.isMachineLearningAdapterTask(store, subId)) {
                    continue;
                }

                Map<String, String> discriminatorsMap = getDiscriminators(store, subId);
                discriminatorsMap = ReportUtils.clearDiscriminatorsByExcludePattern(
                        discriminatorsMap, discriminatorsToExclude);
                discriminatorsMap = ReportUtils.removeKeyRedundancy(discriminatorsMap);

                // add the results into the discriminator map
                File id2o = getId2Outcome(subId);
                String mode = getDiscriminator(store, subId, DIM_LEARNING_MODE);

                Map<String, String> resultMap = MetricComputationUtil.getResults(id2o, mode);
                discriminatorsMap.putAll(resultMap);

                File majBaseline = getBaselineMajorityClassId2Outcome(subId);
                if (isAvailable(majBaseline)) {
                    Map<String, String> results = MetricComputationUtil.getResults(majBaseline,
                            mode);
                    for (Entry<String, String> e : results.entrySet()) {
                        discriminatorsMap.put(e.getKey() + ".MajorityBaseline", e.getValue());
                    }
                }

                File randomBaseline = getBaselineRandomId2Outcome(subId);
                if (isAvailable(randomBaseline)) {
                    Map<String, String> results = MetricComputationUtil.getResults(randomBaseline,
                            mode);
                    for (Entry<String, String> e : results.entrySet()) {
                        discriminatorsMap.put(e.getKey() + ".RandomBaseline", e.getValue());
                    }
                }

                table.addRow(getContextLabel(subId), discriminatorsMap);
            }
        }

        ReportUtils.writeExcelAndCSV(getContext(), getContextLabel(), table, EVAL_FILE_NAME,
                SUFFIX_EXCEL, SUFFIX_CSV);
    }

    private boolean isAvailable(File f)
    {
        return f != null && f.exists();
    }

    private Map<String, String> getDiscriminators(StorageService store, String id)
    {
        return store.retrieveBinary(id, Task.DISCRIMINATORS_KEY, new PropertiesAdapter()).getMap();
    }
}