/*******************************************************************************
 * Copyright 2017
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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Task;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;

public abstract class TcBatchReportBase extends BatchReportBase {

	/**
	 * Retrieves the id2outcome file in a train test setup. The behavior of this
	 * method in cross validation tasks is undefined.
	 * 
	 * @return file to the id2 outcome file in the machine learning adapter or
	 *         null if the folder of machine learning adapter was not found
	 * @throws Exception
	 *             in case of errors
	 */
	protected File getId2Outcome() throws Exception {
		StorageService store = getContext().getStorageService();
		for (TaskContextMetadata subcontext : getSubtasks()) {
			if (TcTaskTypeUtil.isMachineLearningAdapterTask(store, subcontext.getId())) {
				File id2outcomeFile = store.locateKey(subcontext.getId(), Constants.ID_OUTCOME_KEY);
				return id2outcomeFile;
			}
		}

		return null;
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
		List<String> foldersOfSingleRuns = getFoldersOfSingleRuns(cvTaskAttributeFile);

		List<String> mlaContextIdsOfCvRun = new ArrayList<>();
		for (String f : foldersOfSingleRuns) {
			if (TcTaskTypeUtil.isMachineLearningAdapterTask(getContext().getStorageService(), f)) {
				mlaContextIdsOfCvRun.add(f);
			}
		}

		return mlaContextIdsOfCvRun;
	}

	private List<String> getFoldersOfSingleRuns(File attributesTXT) throws Exception {
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

}
