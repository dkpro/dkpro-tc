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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.Task;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;
import org.dkpro.tc.core.util.ReportUtils;
import org.dkpro.tc.ml.report.util.MetricComputationUtil;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Collects the final evaluation results in a train/test setting.
 */
public class TrainTestReport
    extends TcAbstractReport
    implements Constants
{
    private final List<String> discriminatorsToExclude = Arrays
            .asList(new String[] { DIM_FILES_VALIDATION, DIM_FILES_TRAINING });

    private Map<String, String> taskMapping = new HashMap<>();
    private int maxId = 1;

    private static final String baselineFolder = "baselineResults";
    private static final String SEP = "\t";
    private static final String FILE_ENDING = ".tsv";

    public TrainTestReport()
    {
        // required by groovy
    }

    @Override
    public void execute() throws Exception
    {
        writeDiscriminators();

        writeOverallResults(ID_OUTCOME_KEY, false, "");
        writeOverallResults(BASELINE_MAJORITIY_ID_OUTCOME_KEY, true, "majorityBaseline");
        writeOverallResults(BASELINE_RANDOM_ID_OUTCOME_KEY, true, "randomBaseline");

        writeCategoricalResults();
    }

    private void writeDiscriminators() throws Exception
    {
        StringBuilder sb = new StringBuilder();
        StorageService store = getContext().getStorageService();

        Set<String> idPool = getTaskIdsFromMetaData(getSubtasks());

        for (String id : idPool) {

            // Shallow TC uses always the facade task at
            // this point Deep TC comes directly with the MLA
            if (!TcTaskTypeUtil.isFacadeTask(store, id)
                    && !TcTaskTypeUtil.isMachineLearningAdapterTask(store, id)) {
                continue;
            }

            Set<String> wrapped = new HashSet<>();
            wrapped.add(id);
            Set<String> subTaskId = collectTasks(wrapped);

            for (String sid : subTaskId) {

                if (!TcTaskTypeUtil.isMachineLearningAdapterTask(store, sid)) {
                    continue;
                }

                Map<String, String> discriminatorsMap = getDiscriminators(store, sid);
                discriminatorsMap = ReportUtils.clearDiscriminatorsByExcludePattern(
                        discriminatorsMap, discriminatorsToExclude);
                discriminatorsMap = ReportUtils.removeKeyRedundancy(discriminatorsMap);

                sb.append(registerGetMapping(sid) + SEP + getContextLabel(sid));

                for (Entry<String, String> es : discriminatorsMap.entrySet()) {
                    sb.append(SEP + es.getKey() + "=" + es.getValue());
                }
                sb.append("\n");
            }
        }

        File mapping = getContext().getFile("configurationMapping.tsv", AccessMode.READWRITE);
        FileUtils.writeStringToFile(mapping, sb.toString(), UTF_8);
    }

    private String registerGetMapping(String id)
    {

        String value = taskMapping.get(id);
        if (value == null) {
            value = maxId < 100 ? maxId < 10 ? "00" + maxId : "0" + maxId : "" + maxId;
            taskMapping.put(id, value);
            maxId++;
        }

        return value;
    }

    private void writeCategoricalResults() throws Exception
    {
        StorageService store = getContext().getStorageService();

        Set<String> idPool = getTaskIdsFromMetaData(getSubtasks());
        String learningMode = getDiscriminator(store, idPool, DIM_LEARNING_MODE);

        if (!isSingleLabelMode(learningMode)) {
            return;
        }

        for (String id : idPool) {

            if (!TcTaskTypeUtil.isFacadeTask(store, id) // Shallow TC uses always the facade task at
                                                        // this point
                    && !TcTaskTypeUtil.isMachineLearningAdapterTask(store, id)) // Deep TC comes
                                                                                // direclty with MLA
                                                                                // //
                                                                                // directly with the
            {
                continue;
            }

            Set<String> wrapped = new HashSet<>();
            wrapped.add(id);
            Set<String> subTaskId = collectTasks(wrapped);

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%20s\t%5s\t%10s%10s%10s%n", "Category", "Freq", "Precision",
                    "Recall", "F1"));

            for (String sid : subTaskId) {

                if (!TcTaskTypeUtil.isMachineLearningAdapterTask(store, sid)) {
                    continue;
                }

                // The classification result is always there
                File id2outcome = store.locateKey(sid, ID_OUTCOME_KEY);

                List<String[]> computeFScores = MetricComputationUtil
                        .computePerCategoryResults(id2outcome, learningMode);

                for (String[] v : computeFScores) {

                    String category = v[0];
                    Long freq = Long.valueOf(v[1]);
                    String precision = catchNan(v[2]);
                    String recall = catchNan(v[3]);
                    String fscore = catchNan(v[4]);

                    sb.append(String.format("%20s\t%5d\t%10s%10s%10s%n", category, freq, precision,
                            recall, fscore));
                }

                File file = getContext().getFile(getMLSetup(sid) + FILE_SCORE_PER_CATEGORY + "_"
                        + registerGetMapping(sid) + FILE_ENDING, AccessMode.READWRITE);
                FileUtils.writeStringToFile(file, sb.toString(), UTF_8);

                file = getContext().getFile(getMLSetup(sid) + FILE_CONFUSION_MATRIX + "_"
                        + registerGetMapping(sid) + FILE_ENDING, AccessMode.READWRITE);
                MetricComputationUtil.writeConfusionMatrix(id2outcome, file);
            }
        }

    }

    private String getMLSetup(String id) throws Exception
    {

        Map<String, String> discriminatorsMap = getDiscriminatorsForContext(
                getContext().getStorageService(), id, Task.DISCRIMINATORS_KEY);
        discriminatorsMap = ReportUtils.removeKeyRedundancy(discriminatorsMap);

        String args = discriminatorsMap.get(Constants.DIM_CLASSIFICATION_ARGS);

        if (args == null || args.isEmpty()) {
            return "";
        }

        args = args.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "_");

        return args + "_";
    }

    private static Map<String, String> getDiscriminatorsForContext(StorageService store,
            String contextId, String discriminatorsKey)
    {
        return store.retrieveBinary(contextId, discriminatorsKey, new PropertiesAdapter()).getMap();
    }

    private String catchNan(String v)
    {

        if (v.equals("NaN")) {
            return String.format(Locale.getDefault(), "%f", 0.0);
        }

        return v;
    }

    private boolean isSingleLabelMode(String learningMode)
    {
        return learningMode.equals(Constants.LM_SINGLE_LABEL);
    }

    private void writeOverallResults(String idOutcomeKey, boolean isBaseline, String prefix)
        throws Exception
    {

        StringBuilder sb = new StringBuilder();
        StorageService store = getContext().getStorageService();

        Set<String> idPool = getTaskIdsFromMetaData(getSubtasks());

        boolean writeHeader = true;

        for (String id : idPool) {

            // Shallow TC uses always the facade task at
            // this point Deep TC comes directly with the MLA
            if (!TcTaskTypeUtil.isFacadeTask(store, id)
                    && !TcTaskTypeUtil.isMachineLearningAdapterTask(store, id)) {
                continue;
            }

            Set<String> wrapped = new HashSet<>();
            wrapped.add(id);
            Set<String> subTaskId = collectTasks(wrapped);

            for (String subId : subTaskId) {

                if (!TcTaskTypeUtil.isMachineLearningAdapterTask(store, subId)) {
                    continue;
                }

                // add the results into the discriminator map
                File id2o = store.locateKey(subId, idOutcomeKey);

                if (!id2o.exists()) {
                    return;
                }

                String mode = getDiscriminator(store, subId, DIM_LEARNING_MODE);

                Map<String, String> resultMap = MetricComputationUtil.getResults(id2o, mode);
                List<String> mapKeys = new ArrayList<>(resultMap.keySet());
                Collections.sort(mapKeys);

                if (writeHeader) {
                    sb.append("ID" + SEP + "TaskLabel");
                    mapKeys.forEach(x -> sb.append(SEP + x));
                    sb.append("\n");
                    writeHeader = false;
                }

                sb.append(registerGetMapping(subId) + SEP + getContextLabel(subId));

                for (String k : mapKeys) {
                    sb.append(SEP + resultMap.get(k));
                }
                sb.append("\n");

            }
        }

        File targetFile = null;
        if (isBaseline) {
            File folder = getContext().getFolder(baselineFolder, AccessMode.READWRITE);
            targetFile = new File(folder, prefix + EVAL_FILE_NAME + FILE_ENDING);
        }
        else {
            targetFile = getContext().getFile(prefix + EVAL_FILE_NAME + FILE_ENDING,
                    AccessMode.READWRITE);
        }

        FileUtils.writeStringToFile(targetFile, sb.toString(), UTF_8);

    }

    private Map<String, String> getDiscriminators(StorageService store, String id)
    {
        return store.retrieveBinary(id, Task.DISCRIMINATORS_KEY, new PropertiesAdapter()).getMap();
    }
}