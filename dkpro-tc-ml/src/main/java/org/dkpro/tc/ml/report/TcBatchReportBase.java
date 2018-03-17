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
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.BatchTask;
import org.dkpro.lab.task.Task;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;

public abstract class TcBatchReportBase extends BatchReportBase implements Constants {

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
		File id2outcomeFile = store.locateKey(id, ID_OUTCOME_KEY);
		return id2outcomeFile;
	}
	
	/**
	 * Retrieves the id2outcome file in a train test setup. The behavior of this
	 * method in cross validation tasks is undefined.
	 * 
	 * @param id
	 *            context id of machine learning adapter
	 * 
	 * @return file to the majority class id2 outcome file in the machine
	 *         learning adapter or null if the folder of machine learning
	 *         adapter was not found, i.e. majority class is not defined for
	 *         regression tasks
	 * @throws Exception
	 *             in case of errors
	 */
	protected File getBaselineMajorityClassId2Outcome(String id) throws Exception {
		StorageService store = getContext().getStorageService();
		File id2outcomeFile = store.locateKey(id, BASELINE_MAJORITIY_ID_OUTCOME_KEY);
		return id2outcomeFile;
	}
	
	/**
	 * Retrieves the id2outcome file in a train test setup. The behavior of this
	 * method in cross validation tasks is undefined.
	 * 
	 * @param id
	 *            context id of machine learning adapter
	 * 
	 * @return file to the majority class id2 outcome file in the machine
	 *         learning adapter or null if the folder of machine learning
	 *         adapter was not found, i.e. random baseline is available for
	 *         regression tasks
	 * @throws Exception
	 *             in case of errors
	 */
	protected File getBaselineRandomId2Outcome(String id) throws Exception {
		StorageService store = getContext().getStorageService();
		File id2outcomeFile = store.locateKey(id, BASELINE_RANDOM_ID_OUTCOME_KEY);
		return id2outcomeFile;
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
				//the line we are looking for
				break;
			}
			if (line.startsWith("#")) {
				// misc. comment line
				continue;
			}
			
			if (!line.startsWith("#")) {
				// something went wrong, we should have found the labels by now
				break;
			}
		}
		reader.close();
		
		if(line == null){
			throw new NullPointerException("Failed to find label-mapping in [" + id2Outcome.getAbsolutePath() + "]");
		}

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
	 * Collects recursively all <b>subtasks</b> stored in the
	 * <i>attributes.txt</i>. of a task and the tasks located in a lower level
	 * in the hierarchy.
	 * 
	 * @param subtasks
	 *            set of subtasks to be iterated
	 * @return set of all task ids including the one passed as parameter
	 * @throws Exception
	 *             in case of errors
	 */
	public Set<String> collectTasks(Set<String> subtasks) throws Exception {

		Set<String> ids = new HashSet<>();
		for (String taskId : subtasks) {

			Set<String> taskIds = collectSubtasks(taskId);

			ids.add(taskId);
			ids.addAll(taskIds);
		}

		return ids;
	}
	

	/**
	 * Collects recursively all <b>subtasks</b> stored in the
	 * <i>attributes.txt</i>. of a task and the tasks located in a lower level
	 * in the hierarchy.
	 * 
	 * @param contextId
	 *            the current context id
	 * @return set of all task ids including the one passed as parameter
	 * @throws Exception
	 *             in case of errors
	 */
	public Set<String> collectSubtasks(String contextId) throws Exception{
		Set<String> ids = new HashSet<>();
		StorageService store = getContext().getStorageService();
		File attributes = store.locateKey(contextId, Task.ATTRIBUTES_KEY);
		Set<String> taskIds = readSubTasks(attributes);

		ids.add(contextId);
		ids.addAll(taskIds);
		return ids;
	}

	private Set<String> readSubTasks(File attributesTXT) throws Exception {
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
			return new HashSet<>();
		}

		String line = readLines.get(idx);
		int start = line.indexOf("[") + 1;
		int end = line.indexOf("]");
		String subTasks = line.substring(start, end);

		String[] tasks = subTasks.split(",");

		Set<String> results = new HashSet<>();

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
	 * @return collection of strings with context ids extracted from the meta data
	 */
	public Set<String> getTaskIdsFromMetaData(TaskContextMetadata... subtasks) {

		Set<String> taskIds = new HashSet<>();

		for (TaskContextMetadata tcm : subtasks) {
			taskIds.add(tcm.getId());
		}

		return taskIds;
	}
	
	public String getDiscriminator(TaskContext aContext, String key){
		return getDiscriminator(aContext.getStorageService(), aContext.getId(), key);
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
	 * @param constant
	 *            the key that is to be found
	 * @return value of the key if found otherwise null
	 */
	public String getDiscriminator(StorageService store, String contextId, String constant) {

		Map<String, String> map = store
				.retrieveBinary(contextId, Task.DISCRIMINATORS_KEY, new PropertiesAdapter()).getMap();

		if (map == null) {
			return null;
		}
		
		for(Entry<String, String> e : map.entrySet()){
			String k = e.getKey();
			String v = e.getValue();
			
			if (k.endsWith("|" + constant)){
				if (v == null || v.equals("null")){
					return null;
				}
				return v;
			}
		}
		
		return null;
	}

	public String getDiscriminator(StorageService store, Set<String> contextIds, String key) {

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
