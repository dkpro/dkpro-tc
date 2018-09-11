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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.Task;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskType;
import org.dkpro.tc.core.task.TcTaskTypeUtil;
import org.dkpro.tc.core.util.ReportUtils;
import org.dkpro.tc.ml.report.util.MetricComputationUtil;

/**
 * Collects the final evaluation results in a cross validation setting.
 * 
 */
public class BatchCrossValidationReport extends TcBatchReportBase implements Constants {
	private Map<String, String> taskMapping = new HashMap<>();
	private int maxId = 1;

	public BatchCrossValidationReport() {
		// required by groovy
	}

	@Override
	public void execute() throws Exception {

		StorageService store = getContext().getStorageService();
		Set<String> idPool = getTaskIdsFromMetaData(getSubtasks());
		String learningMode = determineLearningMode(store, idPool);

		writeId2DiscriminatorMapping(store, learningMode, idPool);

		writeOverallResults(learningMode, store, idPool);
		writeResultsPerFold(learningMode, store, idPool);
//		writeOverallPerCategoryResults(learningMode, store, idPool);

	}

//	private void writeOverallPerCategoryResults(String learningMode, StorageService store, Set<String> idPool)
//			throws Exception {
//
//		if (!isSingleLabelMode(learningMode)) {
//			return;
//		}
//
//		StringBuilder sb = new StringBuilder();
//		boolean writeHeader = true;
//
//		for (String id : idPool) {
//
//			if (!TcTaskTypeUtil.isCrossValidationTask(store, id)) {
//				continue;
//			}
//
//			Map<String, String> values = new HashMap<String, String>();
//
//			// The classification result is always there
//			File combinedId2outcome = store.locateKey(id, FILE_COMBINED_ID_OUTCOME_KEY);
//			Map<String, String> results = MetricComputationUtil.getResults(combinedId2outcome, learningMode);
//			values.putAll(results);
//
//			List<String[]> computeFScores = MetricComputationUtil.computePerCategoryResults(combinedId2outcome,
//					learningMode);
//
//			for (String[] v : computeFScores) {
//
//				String category = v[0];
//				// Long freq = Long.valueOf(v[1]);
//				Double precision = catchNan(Double.valueOf(v[2]));
//				Double recall = catchNan(Double.valueOf(v[3]));
//				Double fscore = catchNan(Double.valueOf(v[4]));
//
//				values.put(category + "-Precision", precision.toString());
//				values.put(category + "-Recall", recall.toString());
//				values.put(category + "-F1", fscore.toString());
//
//			}
//
//			// add keys and values sorted by keys
//			List<String> mapKeys = new ArrayList<String>(values.keySet());
//			Collections.sort(mapKeys);
//			if (writeHeader) {
//				sb.append("Id\tTaskLabel");
//				sb.append(mapKeys.get(0));
//				mapKeys.subList(1, mapKeys.size()).forEach(x -> sb.append("\t" + x));
//				sb.append("\n");
//				writeHeader = false;
//			}
//			sb.append(registerGetMapping(id) + "\t" + getContextLabel(id));
//			for (String k : mapKeys) {
//				sb.append("\t" + values.get(k));
//			}
//			sb.append("\n");
//
//			FileUtils.writeStringToFile(
//					getContext().getFile("OVERALL_" + FILE_SCORE_PER_CATEGORY, AccessMode.READWRITE), sb.toString(),
//					"utf-8");
//
//			// // write additionally a confusion matrix over the combined file
//			File confusionMatrix = getContext().getFile("OVERALL_" + FILE_CONFUSION_MATRIX, AccessMode.READWRITE);
//			MetricComputationUtil.writeConfusionMatrix(combinedId2outcome, confusionMatrix);
//
//		}
//	}
//
//	private double catchNan(double d) {
//
//		if (Double.isNaN(d)) {
//			return 0.0;
//		}
//
//		return d;
//	}

	private void writeOverallResults(String learningMode, StorageService store, Set<String> idPool) throws Exception {

		StringBuilder sb = new StringBuilder();

		boolean writeHeader = true;

		for (String id : idPool) {

			if (!TcTaskTypeUtil.isCrossValidationTask(store, id)) {
				continue;
			}

			Map<String, String> values = new HashMap<String, String>();

			// The classification result is always there
			File combinedId2outcome = store.locateKey(id, FILE_COMBINED_ID_OUTCOME_KEY);
			Map<String, String> results = MetricComputationUtil.getResults(combinedId2outcome, learningMode);
			values.putAll(results);

			addMajorityBaslineResults(learningMode, id, store, values);
			addRandomBaselineResults(learningMode, id, store, values);

			// add keys and values sorted by keys
			List<String> mapKeys = new ArrayList<String>(values.keySet());
			Collections.sort(mapKeys);
			if (writeHeader) {
				sb.append("Id\tTaskLabel");
				mapKeys.forEach(x -> sb.append("\t" + x));
				sb.append("\n");
				writeHeader = false;
			}
			sb.append(registerGetMapping(id) + "\t" + getContextLabel(id));
			for (String k : mapKeys) {
				sb.append("\t" + values.get(k));
			}
			sb.append("\n");

			if (isSingleLabelMode(learningMode)) {
				// write additionally a confusion matrix over the combined file
				File confusionMatrix = getContext().getFile(registerGetMapping(id) + "_" + FILE_CONFUSION_MATRIX,
						AccessMode.READWRITE);
				MetricComputationUtil.writeConfusionMatrix(combinedId2outcome, confusionMatrix);

				File fscoreFile = getContext().getStorageService().locateKey(getContext().getId(),
						registerGetMapping(id) + "_" + FILE_SCORE_PER_CATEGORY);
				ResultPerCategoryCalculator r = new ResultPerCategoryCalculator(combinedId2outcome, learningMode);
				r.writeResults(fscoreFile);
			}

		}

		FileUtils.writeStringToFile(getContext().getFile(EVAL_FILE_NAME + ".tsv", AccessMode.READWRITE), sb.toString(),
				"utf-8");

	}

	private void writeId2DiscriminatorMapping(StorageService store, String learningMode, Set<String> idPool)
			throws Exception {
		StringBuilder sb = new StringBuilder();

		for (String id : idPool) {

			if (!TcTaskTypeUtil.isCrossValidationTask(store, id)) {
				continue;
			}

			Map<String, String> discriminatorsMap = getDiscriminatorsOfMlaSetup(id);

			discriminatorsMap.putAll(getDiscriminatorsForContext(store, id, Task.DISCRIMINATORS_KEY));
			discriminatorsMap = ReportUtils.removeKeyRedundancy(discriminatorsMap);

			Map<String, String> values = new HashMap<String, String>();
			values.putAll(discriminatorsMap);

			// add keys and values sorted by keys
			List<String> mapKeys = new ArrayList<String>(values.keySet());
			Collections.sort(mapKeys);
			sb.append(registerGetMapping(id) + "\t" + getContextLabel(id));
			for (String k : mapKeys) {
				sb.append("\t" + k + "=" + values.get(k));
			}
			sb.append("\n");
		}

		FileUtils.writeStringToFile(getContext().getFile("configurationMapping.tsv", AccessMode.READWRITE),
				sb.toString(), "utf-8");

	}

	private String registerGetMapping(String id) {

		String value = taskMapping.get(id);
		if (value == null) {
			value = maxId < 100 ? (maxId < 10 ? "00" + maxId : "0" + maxId) : "" + maxId;
			taskMapping.put(id, value);
			maxId++;
		}

		return value;
	}

	private List<String> iterateSubtasksPoolFilterFor(Set<String> pool, TcTaskType ttt) throws Exception {

		List<String> tasksIdsOfTargetType = new ArrayList<>();

		for (String id : pool) {
			tasksIdsOfTargetType.addAll(iterateSubtasksFilterFor(id, ttt));
		}

		return tasksIdsOfTargetType;
	}

	private List<String> iterateSubtasksFilterFor(String id, TcTaskType ttt) throws Exception {

		List<String> tasksIdsOfTargetType = new ArrayList<>();

		// get the details of the configuration from a MLA - any will do
		Set<String> collectSubtasks = collectSubtasks(id);
		for (String subid : collectSubtasks) {
			if (TcTaskTypeUtil.getType(getContext().getStorageService(), subid) == ttt) {
				tasksIdsOfTargetType.add(subid);
			}
		}
		return tasksIdsOfTargetType;
	}

	private Map<String, String> getDiscriminatorsOfMlaSetup(String id) throws Exception {
		Map<String, String> discriminatorsMap = new HashMap<>();

		// get the details of the configuration from a MLA - any will do
		Set<String> collectSubtasks = collectSubtasks(id);
		for (String subid : collectSubtasks) {
			if (TcTaskTypeUtil.isMachineLearningAdapterTask(getContext().getStorageService(), subid)) {
				discriminatorsMap.putAll(
						getDiscriminatorsForContext(getContext().getStorageService(), subid, Task.DISCRIMINATORS_KEY));
				break;
			}
		}
		return discriminatorsMap;
	}

	private boolean isSingleLabelMode(String learningMode) {
		return learningMode.equals(Constants.LM_SINGLE_LABEL);
	}

	private void writeResultsPerFold(String learningMode, StorageService store, Set<String> idPool) throws Exception {

		Set<String> allTasks = collectTasks(idPool);

		StringBuilder sb = new StringBuilder();
		boolean writeHeader = true;

		for (String id : allTasks) {
			if (!TcTaskTypeUtil.isMachineLearningAdapterTask(store, id)) {
				continue;
			}

			Map<String, String> values = new HashMap<String, String>();

//			Map<String, String> discriminatorsMap = getDiscriminatorsForContext(store, id, Task.DISCRIMINATORS_KEY);
//			discriminatorsMap = ReportUtils.removeKeyRedundancy(discriminatorsMap);
//
//			Map<String, String> values = new HashMap<String, String>();
//			values.putAll(discriminatorsMap);

			// The classification result is always there
			File foldId2Outcome = store.locateKey(id, ID_OUTCOME_KEY);
			Map<String, String> results = MetricComputationUtil.getResults(foldId2Outcome, learningMode);
			values.putAll(results);
			addMajorityBaslineResults(learningMode, id, store, values);
			addRandomBaselineResults(learningMode, id, store, values);

			// add keys and values sorted by keys
			List<String> mapKeys = new ArrayList<String>(values.keySet());
			Collections.sort(mapKeys);
			if (writeHeader) {
				sb.append("Id\tTaskLabel");
				mapKeys.forEach(x -> sb.append("\t" + x));
				sb.append("\n");
				writeHeader = false;
			}
			sb.append(registerGetMapping(id) + "\t" + getContextLabel(id));
			for (String k : mapKeys) {
				sb.append("\t" + values.get(k));
			}
			sb.append("\n");
		}

		FileUtils.writeStringToFile(getContext().getFile(EVAL_FILE_NAME_PER_FOLD + ".tsv", AccessMode.READWRITE),
				sb.toString(), "utf-8");

	}

	private void addRandomBaselineResults(String learningMode, String id, StorageService store,
			Map<String, String> values) throws Exception {
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
			Map<String, String> values) throws Exception {
		// Majority baseline is not defined for regression i.e. might not be there
		File majBaseline = store.locateKey(id, FILE_COMBINED_BASELINE_MAJORITY_OUTCOME_KEY);
		if (isAvailable(majBaseline)) {
			Map<String, String> r = MetricComputationUtil.getResults(majBaseline, learningMode);
			for (Entry<String, String> e : r.entrySet()) {
				values.put(e.getKey() + ".MajorityBaseline", e.getValue());
			}
		}
	}

	private boolean isAvailable(File f) {
		return f != null && f.exists();
	}

	private String determineLearningMode(StorageService store, Set<String> idPool) throws Exception {
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

	private static Map<String, String> getDiscriminatorsForContext(StorageService store, String contextId,
			String discriminatorsKey) {
		return store.retrieveBinary(contextId, discriminatorsKey, new PropertiesAdapter()).getMap();
	}

}
