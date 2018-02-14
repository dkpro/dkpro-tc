/**
 * Copyright 2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.tc.examples.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dkpro.lab.storage.StorageService;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;
import org.dkpro.tc.ml.report.TcBatchReportBase;

/**
 * This is a slightly ugly solution for recording the DKPro Lab output folder of
 * an experiment to read result files in JUnit tests
 */
public class ContextMemoryReport extends TcBatchReportBase {

	public static List<File> id2outcomeFiles = new ArrayList<>();
	
	public static List<File> crossValidationCombinedIdFiles = new ArrayList<>();
	
	public static List<String> allIds = new ArrayList<String>();

	@Override
	public void execute() throws Exception {
		
		id2outcomeFiles = new ArrayList<>();
		crossValidationCombinedIdFiles = new ArrayList<>();
		allIds = new ArrayList<>();
		
		StorageService storageService = getContext().getStorageService();
		
		Set<String> taskIds = getTaskIdsFromMetaData(getSubtasks());
		allIds.addAll(taskIds);
		for (String id : taskIds) {
			processFacadeId(storageService, id);
			processMachineLearningAdapterId(storageService, id);
			processCrossValidationId(storageService, id);
		}
	}

	private void processFacadeId(StorageService storageService, String id) throws Exception {
		if (!TcTaskTypeUtil.isFacadeTask(storageService, id)) {
			return;
		}
		
		Set<String> collectSubtasks = collectSubtasks(id);
		for(String subid : collectSubtasks){
			processMachineLearningAdapterId(storageService, subid);
		}
	}

	private void processCrossValidationId(StorageService storageService, String id) throws IOException {
		if (!TcTaskTypeUtil.isCrossValidationTask(storageService, id)) {
			return;
		}
		
			File f = storageService.locateKey(id, Constants.FILE_COMBINED_ID_OUTCOME_KEY);
			id2outcomeFiles.add(f);
			crossValidationCombinedIdFiles.add(f);
	}

	private void processMachineLearningAdapterId(StorageService storageService, String id) throws IOException {
		if (!TcTaskTypeUtil.isMachineLearningAdapterTask(storageService, id)) {
			return;
		}
			id2outcomeFiles.add(storageService.locateKey(id, Constants.ID_OUTCOME_KEY));
	}

}
