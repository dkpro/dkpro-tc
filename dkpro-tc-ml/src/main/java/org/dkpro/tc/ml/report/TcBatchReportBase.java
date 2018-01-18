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

import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
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
	 *             in case the file
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

}
