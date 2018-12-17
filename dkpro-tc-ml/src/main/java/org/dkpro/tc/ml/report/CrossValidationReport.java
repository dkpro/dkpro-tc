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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.Task;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;
import org.dkpro.tc.core.util.ReportUtils;
import org.dkpro.tc.ml.report.util.MetricComputationUtil;

/**
 * Collects the final evaluation results in a cross validation setting.
 * 
 */
public class CrossValidationReport
    extends TcAbstractReport
    implements Constants
{
    private Map<String, String> taskMapping = new HashMap<>();
    private int maxId = 1;

    private static final String baselineFolder = "baselineResults";
    private static final String SEP = "\t";
    private static final String FILE_ENDING = ".tsv";
    
    /**
     * This switch turns off the sysout printing 
     */
    public static boolean printResultsToSysout = true;

    public CrossValidationReport()
    {
        // required by groovy
    }

    @Override
    public void execute() throws Exception
    {

        StorageService store = getContext().getStorageService();
        Set<String> idPool = getTaskIdsFromMetaData(getSubtasks());
        String learningMode = determineLearningMode(store, idPool);

        writeId2DiscriminatorMapping(store, idPool);

        writeOverallResults(learningMode, store, idPool, FILE_COMBINED_ID_OUTCOME_KEY, false, "");
        writeResultsPerFold(learningMode, store, idPool, ID_OUTCOME_KEY, false, "");

        if (learningMode.equals(LM_SINGLE_LABEL)) {
            writeOverallResults(learningMode, store, idPool,
                    FILE_COMBINED_BASELINE_MAJORITY_OUTCOME_KEY, true, "majorityBaseline_");
            writeOverallResults(learningMode, store, idPool,
                    FILE_COMBINED_BASELINE_RANDOM_OUTCOME_KEY, true, "randomBaseline_");
            writeResultsPerFold(learningMode, store, idPool, BASELINE_MAJORITIY_ID_OUTCOME_KEY,
                    true, "majorityBaseline_");
            writeResultsPerFold(learningMode, store, idPool, BASELINE_RANDOM_ID_OUTCOME_KEY, true,
                    "randomBaseline_");
        }

    }

    private void writeOverallResults(String learningMode, StorageService store, Set<String> idPool,
            String outcomeFileName, boolean isBaseline, String prefix)
        throws Exception
    {

        StringBuilder sb = new StringBuilder();

        boolean writeHeader = true;

        for (String id : idPool) {

            if (!TcTaskTypeUtil.isCrossValidationTask(store, id)) {
                continue;
            }

            Map<String, String> values = new HashMap<String, String>();

            // The classification result is always there
            File combinedId2outcome = store.locateKey(id, outcomeFileName);

            if (!combinedId2outcome.exists()) {
                return;
            }

            Map<String, String> results = MetricComputationUtil.getResults(combinedId2outcome,
                    learningMode);
            values.putAll(results);

            // add keys and values sorted by keys
            List<String> mapKeys = new ArrayList<String>(values.keySet());
            Collections.sort(mapKeys);
            if (writeHeader) {
                sb.append("Id" + SEP + "TaskLabel");
                mapKeys.forEach(x -> sb.append(SEP + x));
                sb.append("\n");
                writeHeader = false;
            }
            sb.append(registerGetMapping(id) + SEP + getContextLabel(id));
            for (String k : mapKeys) {
                sb.append(SEP + values.get(k));
            }
            sb.append("\n");

            if (isSingleLabelMode(learningMode) && !isBaseline) {
                // write additionally a confusion matrix over the combined file
                String matrixName = getMLSetup(id) + registerGetMapping(id) + "_"
                        + FILE_CONFUSION_MATRIX;
                File confusionMatrix = getContext().getFile(matrixName, AccessMode.READWRITE);
                MetricComputationUtil.writeConfusionMatrix(combinedId2outcome, confusionMatrix);

                String catScoreName = getMLSetup(id) + registerGetMapping(id) + "_"
                        + FILE_SCORE_PER_CATEGORY + FILE_ENDING;
                File fscoreFile = getContext().getStorageService().locateKey(getContext().getId(),
                        catScoreName);
                ResultPerCategoryCalculator r = new ResultPerCategoryCalculator(combinedId2outcome,
                        learningMode);
                r.writeResults(fscoreFile);

				if (printResultsToSysout) {
					System.out.println("\n\nRESULT SUMMARY CROSSVALIDATION\n[" + id
							+ "/" + getMLSetup(id) + "]");
					results.keySet().forEach(x -> System.out.println("\t\t" + x + ": " + results.get(x)));
					System.out.println("\nAccumulated results per category:\n" + r.getResults());
					System.out.println("\n");
				} else {
					StringBuilder logMsg = new StringBuilder();
					logMsg.append("[" + id + "/" + getMLSetup(id) + "]: ");
					results.keySet().forEach(x -> logMsg.append(x + "=" + results.get(x)+ " |"));
					LogFactory.getLog(getClass()).info(logMsg.toString());
				}
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

        FileUtils.writeStringToFile(targetFile, sb.toString(), "utf-8");

    }

    private String getMLSetup(String id) throws Exception
    {

        Map<String, String> discriminatorsMap = getDiscriminatorsOfMlaSetup(id);
        discriminatorsMap = ReportUtils.removeKeyRedundancy(discriminatorsMap);

        String args = discriminatorsMap.get(Constants.DIM_CLASSIFICATION_ARGS);

        if (args == null || args.isEmpty()) {
            return "";
        }

        args = args.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "_");

        return args + "_";
    }

    private void writeId2DiscriminatorMapping(StorageService store, Set<String> idPool)
        throws Exception
    {
        StringBuilder sb = new StringBuilder();

        for (String id : idPool) {

            if (!TcTaskTypeUtil.isCrossValidationTask(store, id)) {
                continue;
            }

            Map<String, String> values = getDiscriminatorsOfMlaSetup(id);
            values.putAll(getDiscriminatorsForContext(store, id, Task.DISCRIMINATORS_KEY));
            values = ReportUtils.removeKeyRedundancy(values);
            values = ReportUtils.replaceKeyWithConstant(values, DIM_FILES_ROOT, "<OMITTED>");
            values = ReportUtils.replaceKeyWithConstant(values, DIM_FILES_TRAINING, "<OMITTED>");
            values = ReportUtils.replaceKeyWithConstant(values, DIM_FILES_VALIDATION, "<OMITTED>");

            // add keys and values sorted by keys
            List<String> mapKeys = new ArrayList<String>(values.keySet());
            Collections.sort(mapKeys);
            sb.append(registerGetMapping(id) + SEP + getContextLabel(id));
            for (String k : mapKeys) {
                sb.append(SEP + k + "=" + values.get(k));
            }
            sb.append("\n");

            Set<String> collectSubtasks = collectSubtasks(id);
            for (String sid : collectSubtasks) {
                if (!TcTaskTypeUtil.isMachineLearningAdapterTask(store, sid)) {
                    continue;
                }
                values = getDiscriminatorsOfMlaSetup(sid);
                values.putAll(getDiscriminatorsForContext(store, sid, Task.DISCRIMINATORS_KEY));
                values = ReportUtils.removeKeyRedundancy(values);
                values = ReportUtils.replaceKeyWithConstant(values, DIM_FILES_ROOT, "<OMITTED>");
                values = ReportUtils.replaceKeyWithConstant(values, DIM_FILES_TRAINING,
                        "<OMITTED>");
                values = ReportUtils.replaceKeyWithConstant(values, DIM_FILES_VALIDATION,
                        "<OMITTED>");

                mapKeys = new ArrayList<String>(values.keySet());
                Collections.sort(mapKeys);

                sb.append(registerGetMapping(sid) + SEP + getContextLabel(sid));
                for (String k : mapKeys) {
                    sb.append(SEP + k + "=" + values.get(k));
                }
                sb.append("\n");
            }
        }

        FileUtils.writeStringToFile(
                getContext().getFile(FILE_CONFIGURATION_MAPPING, AccessMode.READWRITE),
                sb.toString(), "utf-8");

    }

    private String registerGetMapping(String id)
    {

        String value = taskMapping.get(id);
        if (value == null) {
            value = maxId < 100 ? (maxId < 10) ? "00" + maxId : "0" + maxId : "" + maxId;
            taskMapping.put(id, value);
            maxId++;
        }

        return value;
    }

    private Map<String, String> getDiscriminatorsOfMlaSetup(String id) throws Exception
    {
        Map<String, String> discriminatorsMap = new HashMap<>();

        // get the details of the configuration from a MLA - any will do
        Set<String> collectSubtasks = collectSubtasks(id);
        for (String subid : collectSubtasks) {
            if (TcTaskTypeUtil.isMachineLearningAdapterTask(getContext().getStorageService(),
                    subid)) {
                discriminatorsMap.putAll(getDiscriminatorsForContext(
                        getContext().getStorageService(), subid, Task.DISCRIMINATORS_KEY));
                break;
            }
        }
        return discriminatorsMap;
    }

    private boolean isSingleLabelMode(String learningMode)
    {
        return learningMode.equals(Constants.LM_SINGLE_LABEL);
    }

    private void writeResultsPerFold(String learningMode, StorageService store, Set<String> idPool,
            String outcomeFileName, boolean isBaseline, String prefix)
        throws Exception
    {

        Set<String> allTasks = collectTasks(idPool);

        StringBuilder sb = new StringBuilder();
        boolean writeHeader = true;

        List<String> content = new ArrayList<>();

        for (String id : allTasks) {
            if (!TcTaskTypeUtil.isMachineLearningAdapterTask(store, id)) {
                continue;
            }

            Map<String, String> values = new HashMap<String, String>();

            // The classification result is always there
            File foldId2Outcome = store.locateKey(id, outcomeFileName);

            if (!foldId2Outcome.exists()) {
                return;
            }

            Map<String, String> results = MetricComputationUtil.getResults(foldId2Outcome,
                    learningMode);
            values.putAll(results);

            // add keys and values sorted by keys
            List<String> mapKeys = new ArrayList<String>(values.keySet());
            Collections.sort(mapKeys);
            if (writeHeader) {
                sb.append("Id" + SEP + "TaskLabel");
                mapKeys.forEach(x -> sb.append(SEP + x));
                sb.append("\n");
                writeHeader = false;
            }
            StringBuilder line = new StringBuilder();

            line.append(registerGetMapping(id) + SEP + getContextLabel(id));
            for (String k : mapKeys) {
                line.append(SEP + values.get(k));
            }
            content.add(line.toString());
        }

        // Sort lines by id
        Collections.sort(content, new Comparator<String>()
        {

            @Override
            public int compare(String o1, String o2)
            {

                String[] split = o1.split(SEP);
                Integer id1 = Integer.valueOf(split[0]);

                split = o2.split(SEP);
                Integer id2 = Integer.valueOf(split[0]);

                return id1.compareTo(id2);
            }
        });

        content.forEach(x -> sb.append(x + "\n"));

        File targetFile = null;
        if (isBaseline) {
            File folder = getContext().getFolder(baselineFolder, AccessMode.READWRITE);
            targetFile = new File(folder, prefix + EVAL_FILE_NAME_PER_FOLD + FILE_ENDING);
        }
        else {
            targetFile = getContext().getFile(prefix + EVAL_FILE_NAME_PER_FOLD + FILE_ENDING,
                    AccessMode.READWRITE);
        }

        FileUtils.writeStringToFile(targetFile, sb.toString(), "utf-8");

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
