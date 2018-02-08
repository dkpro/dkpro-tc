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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.BatchTask;
import org.dkpro.lab.task.Task;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;

public abstract class TcBatchReportBase extends BatchReportBase {

	/**
	 * Retrieves the id2outcome file in a train test setup. The behavior of this
	 * method in cross validation tasks is undefined.
	 * 
	 * @param id
	 *            context id of machine learning adapter
	 * 
	 * @return file to the id2 outcome file in the machine learning adapter or
	 *         null if the folder of machine learning adapter was not found
	 * @throws Exception
	 *             in case of errors
	 */
	protected File getId2Outcome(String id) throws Exception {
		StorageService store = getContext().getStorageService();
		File id2outcomeFile = store.locateKey(id, Constants.ID_OUTCOME_KEY);
		return id2outcomeFile;
	}

	/**
	 * Retrieves the training data folder of a train-test run. Behavior is
	 * undefined if called during cross-validation
	 * 
	 * @return file of the training data folder
	 * @throws IOException
	 *             in case of error
	 */
	protected File getTrainDataFolder() throws IOException {
		StorageService store = getContext().getStorageService();

		for (TaskContextMetadata subcontext : getSubtasks()) {
			if (TcTaskTypeUtil.isFeatureExtractionTrainTask(store, subcontext.getId())) {
				File folder = store.locateKey(subcontext.getId(), "");
				return folder;
			}
		}

		return null;
	}

	/**
	 * Retrieves the test data folder of a train-test run. Behavior is undefined
	 * if called during cross-validation
	 * 
	 * @return file of the test data folder
	 * @throws IOException
	 *             in case of error
	 */
	protected File getTestDataFolder() throws IOException {
		StorageService store = getContext().getStorageService();

		for (TaskContextMetadata subcontext : getSubtasks()) {
			if (TcTaskTypeUtil.isFeatureExtractionTestTask(store, subcontext.getId())) {
				File folder = store.locateKey(subcontext.getId(), "");
				return folder;
			}
		}

		return null;
	}

	/**
	 * Loads a mapping from the numeric values to their corresponding label. The
	 * mapping is retrieved from the header of the id2outcome result file. The
	 * map is empty for regression which has no mapping.
	 * 
	 * @param contextId
	 *            context id of context from which the mapping shall be loaded
	 * @return a hashmap with a integer to string mapping
	 * @throws Exception
	 *             in case of error
	 */
	protected Map<String, String> getInteger2LabelMapping(String contextId) throws Exception {

		File id2Outcome = getId2Outcome(contextId);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(id2Outcome), "utf-8"));

		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("#labels")) {
				break;
			}
			if (line.startsWith("#")) {
				continue;
			}
			break;
		}
		reader.close();

		line = line.replaceAll("#labels", "").trim();

		if (line.isEmpty()) {
			// regression mode has no mapping
			return new HashMap<>();
		}

		Map<String, String> m = new HashMap<>();
		String[] entries = line.split(" ");
		for (String e : entries) {
			String[] intLabel = e.split("=");
			m.put(intLabel[0], intLabel[1]);
		}

		return m;
	}

	/**
	 * Retrieves the context ids of all machine learning adapter folders that
	 * have been created in this cross-validation run. Behavior undefined if
	 * this method is called in a train test setup
	 * 
	 * @return a list of context ids of the machine learning adapter folders
	 * @throws Exception
	 *             in case read operations fail
	 */
	public List<String> getContextIdOfMachineLearningAdapter() throws Exception {

		File cvTaskAttributeFile = getContext().getFile(Task.ATTRIBUTES_KEY, AccessMode.READONLY);
		List<String> foldersOfSingleRuns = getSubTasks(cvTaskAttributeFile);

		List<String> mlaContextIdsOfCvRun = new ArrayList<>();
		for (String f : foldersOfSingleRuns) {
			if (TcTaskTypeUtil.isMachineLearningAdapterTask(getContext().getStorageService(), f)) {
				mlaContextIdsOfCvRun.add(f);
			}
		}

		return mlaContextIdsOfCvRun;
	}

	private List<String> getSubTasks(File attributesTXT) throws Exception {
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
			if (TcTaskTypeUtil.isMachineLearningAdapterTask(getContext().getStorageService(), task.trim())) {
				results.add(task.trim());
			}
		}

		return results;
	}

	/**
	 * Collects recursively all <b>subtasks</b> stored in the
	 * <i>attributes.txt</i>. of a task and the tasks located in a lower level
	 * in the hierarchy.
	 * 
	 * @param subtasks
	 *            list of subtasks to be iterated
	 * @return list of all task ids including the one passed as parameter
	 * @throws Exception
	 *             in case of errors
	 */
	public List<String> collectTasks(List<String> subtasks) throws Exception {

		StorageService store = getContext().getStorageService();

		List<String> ids = new ArrayList<>();
		for (String taskId : subtasks) {
			File attributes = store.locateKey(taskId, Task.ATTRIBUTES_KEY);
			List<String> taskIds = readSubTasks(attributes);

			ids.add(taskId);
			ids.addAll(taskIds);
		}

		return ids;
	}

	private List<String> readSubTasks(File attributesTXT) throws Exception {
		List<String> readLines = FileUtils.readLines(attributesTXT, "utf-8");

		int idx = 0;
		boolean found = false;
		for (String line : readLines) {
			if (line.startsWith(BatchTask.SUBTASKS_KEY)) {
				found = true;
				break;
			}
			idx++;
		}

		if (!found) {
			return new ArrayList<>();
		}

		String line = readLines.get(idx);
		int start = line.indexOf("[") + 1;
		int end = line.indexOf("]");
		String subTasks = line.substring(start, end);

		String[] tasks = subTasks.split(",");

		List<String> results = new ArrayList<>();

		for (String task : tasks) {
			results.add(task.trim());
			File subAttribute = getContext().getStorageService().locateKey(task.trim(), Task.ATTRIBUTES_KEY);
			results.addAll(readSubTasks(subAttribute));
		}

		return results;
	}

	/**
	 * Takes context meta data objects and returns their context ids as string
	 * values
	 * 
	 * @param subtasks
	 *            arbitrary number of TaskContextMetadata objects
	 * @return list of strings with context ids extracted from the meta data
	 */
	public List<String> getTaskIdsFromMetaData(TaskContextMetadata... subtasks) {

		List<String> taskIds = new ArrayList<>();

		for (TaskContextMetadata tcm : subtasks) {
			taskIds.add(tcm.getId());
		}

		return taskIds;
	}

	/**
	 * Retrieves the value of a certain key from the discriminators. A key might
	 * occur in several tasks but it is assumed that the value of this key is
	 * always the same, i.e. the first found entry is returned regardless in
	 * which task this key is found
	 * 
	 * @param store
	 *            the storage
	 * @param contextId
	 *            id of the context in which to look
	 * @param key
	 *            the key that is to be found
	 * @return value of the key if found otherwise null
	 */
	public String getDiscriminator(StorageService store, String contextId, String key) {

		Map<String, String> map = store
				.retrieveBinary(contextId, Task.DISCRIMINATORS_KEY, new PropertiesAdapter()).getMap();

		if (map == null) {
			return null;
		}

		for (String k : map.keySet()) {
			if (k.endsWith("|" + key)) {
				return map.get(k);
			}
		}

		return null;
	}

	public String getDiscriminator(StorageService store, List<String> contextIds, String key) {

		for (String id : contextIds) {
			String v = getDiscriminator(store, id, key);
			if (v != null) {
				return v;
			}
		}

		return null;
	}

	
	@Override
	public Map<String, String> getAttributes()
	{
		// we override this method because we want always the attributes of
		// 'this' task - no caching of old results this might leads to errors
		return retrieveBinary(Task.ATTRIBUTES_KEY, new PropertiesAdapter()).getMap();
	}

}
