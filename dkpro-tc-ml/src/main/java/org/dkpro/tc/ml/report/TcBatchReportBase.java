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
