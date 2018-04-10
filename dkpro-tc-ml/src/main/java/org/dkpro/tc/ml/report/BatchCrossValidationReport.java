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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.Task;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;
import org.dkpro.tc.core.util.ReportUtils;
import org.dkpro.tc.core.util.TcFlexTable;
import org.dkpro.tc.ml.report.util.MetricComputationUtil;

/**
 * Collects the final evaluation results in a cross validation setting.
 * 
 */
public class BatchCrossValidationReport
    extends TcBatchReportBase
    implements Constants
{

    public BatchCrossValidationReport()
    {
        // required by groovy
    }

    @Override
    public void execute() throws Exception
    {

        StorageService store = getContext().getStorageService();
        Set<String> idPool = getTaskIdsFromMetaData(getSubtasks());

        String learningMode = determineLearningMode(store, idPool);

        writeOverallResuls(learningMode, store, idPool);
        writeResultsPerFold(learningMode, store, idPool);

    }

    private void writeOverallResuls(String learningMode, StorageService store, Set<String> idPool)
        throws Exception
    {

        TcFlexTable<String> table = TcFlexTable.forClass(String.class);
        table.setDefaultValue("");

        for (String id : idPool) {
            if (!TcTaskTypeUtil.isCrossValidationTask(store, id)) {
                continue;
            }

            Map<String, String> discriminatorsMap = getDiscriminatorsForContext(store, id,
                    Task.DISCRIMINATORS_KEY);
            discriminatorsMap = ReportUtils.removeKeyRedundancy(discriminatorsMap);

            Map<String, String> values = new HashMap<String, String>();
            values.putAll(discriminatorsMap);

            // The classification result is always there
            File combinedId2outcome = store.locateKey(id, FILE_COMBINED_ID_OUTCOME_KEY);
            Map<String, String> results = MetricComputationUtil.getResults(combinedId2outcome,
                    learningMode);
            values.putAll(results);

            addMajorityBaslineResults(learningMode, id, store, values);
            addRandomBaselineResults(learningMode, id, store, values);

            values = ReportUtils.replaceKeyWithConstant(values, DIM_FILES_VALIDATION, "<OMITTED>");
            values = ReportUtils.replaceKeyWithConstant(values, DIM_FILES_TRAINING, "<OMITTED>");
            table.addRow(getContextLabel(id), values);

            if (isSingleLabelMode(learningMode)) {
                // write additionally a confusion matrix over the combined file
                File confusionMatrix = getContext().getFile(FILE_CONFUSION_MATRIX,
                        AccessMode.READWRITE);
                MetricComputationUtil.writeConfusionMatrix(combinedId2outcome, confusionMatrix);
                
                
                File fscoreFile = getContext().getStorageService().locateKey(getContext().getId(),
                        FILE_FSCORES_PER_LABEL);
                FscoreResultsIO r = new FscoreResultsIO(combinedId2outcome, learningMode);
                r.writeResults(fscoreFile);
            }

        }

        /*
         * TODO: make rows to columns e.g. create a new table and set columns to rows of old table
         * and rows to columns but than must be class FlexTable in this case adapted accordingly:
         * enable setting
         */

        ReportUtils.writeExcelAndCSV(getContext(), getContextLabel(), table, EVAL_FILE_NAME,
                SUFFIX_EXCEL, SUFFIX_CSV);
        
        
    }

    private boolean isSingleLabelMode(String learningMode)
    {
        return learningMode.equals(Constants.LM_SINGLE_LABEL);
    }

    private void writeResultsPerFold(String learningMode, StorageService store, Set<String> idPool)
        throws Exception
    {
        TcFlexTable<String> table = TcFlexTable.forClass(String.class);
        table.setDefaultValue("");

        Set<String> allTasks = collectTasks(idPool);

        for (String id : allTasks) {
            if (!TcTaskTypeUtil.isMachineLearningAdapterTask(store, id)) {
                continue;
            }

            Map<String, String> discriminatorsMap = getDiscriminatorsForContext(store, id,
                    Task.DISCRIMINATORS_KEY);
            discriminatorsMap = ReportUtils.removeKeyRedundancy(discriminatorsMap);

            Map<String, String> values = new HashMap<String, String>();
            values.putAll(discriminatorsMap);

            // The classification result is always there
            File foldId2Outcome = store.locateKey(id, ID_OUTCOME_KEY);
            Map<String, String> results = MetricComputationUtil.getResults(foldId2Outcome,
                    learningMode);
            values.putAll(results);
            addMajorityBaslineResults(learningMode, id, store, values);
            addRandomBaselineResults(learningMode, id, store, values);

            // This key might have arbitrary long file path as values which easily exceed the limit
            // of 32k characers
            values = ReportUtils.replaceKeyWithConstant(values, DIM_FILES_VALIDATION, "<OMITTED>");
            values = ReportUtils.replaceKeyWithConstant(values, DIM_FILES_TRAINING, "<OMITTED>");
            table.addRow(getContextLabel(id), values);
        }
        ReportUtils.writeExcelAndCSV(getContext(), getContextLabel(), table,
                EVAL_FILE_NAME_PER_FOLD, SUFFIX_EXCEL, SUFFIX_CSV);
    }

    private void addRandomBaselineResults(String learningMode, String id, StorageService store,
            Map<String, String> values)
        throws Exception
    {
        // Random baseline is not defined for regression i.e. might not be there
        File randomBaseline = store.locateKey(id, FILE_COMBINED_BASELINE_RANDOM_OUTCOME_KEY);
        if (isAvailable(randomBaseline)) {
            Map<String, String> r = MetricComputationUtil.getResults(randomBaseline, learningMode);
            for (Entry<String, String> e : r.entrySet()) {
                values.put(e.getKey() + ".RandomBaseline", e.getValue());
            }
        }
    }

    private void addMajorityBaslineResults(String id, String learningMode, StorageService store,
            Map<String, String> values)
        throws Exception
    {
        // Majority baseline is not defined for regression i.e. might not be there
        File majBaseline = store.locateKey(id, FILE_COMBINED_BASELINE_MAJORITY_OUTCOME_KEY);
        if (isAvailable(majBaseline)) {
            Map<String, String> r = MetricComputationUtil.getResults(majBaseline, learningMode);
            for (Entry<String, String> e : r.entrySet()) {
                values.put(e.getKey() + ".MajorityBaseline", e.getValue());
            }
        }
    }

    private boolean isAvailable(File f)
    {
        return f != null && f.exists();
    }

    private String determineLearningMode(StorageService store, Set<String> idPool) throws Exception
    {
        String learningMode = getDiscriminator(store, idPool, DIM_LEARNING_MODE);
        if (learningMode == null) {
            for (String id : idPool) {
                Set<String> collectSubtasks = collectSubtasks(id);
                learningMode = getDiscriminator(store, collectSubtasks, DIM_LEARNING_MODE);
                if (learningMode != null) {
                    break;
                }
            }
        }
        return learningMode;
    }

    private static Map<String, String> getDiscriminatorsForContext(StorageService store,
            String contextId, String discriminatorsKey)
    {
        return store.retrieveBinary(contextId, discriminatorsKey, new PropertiesAdapter()).getMap();
    }

}
