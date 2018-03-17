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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.BatchTask;
import org.dkpro.lab.task.Task;
import org.dkpro.tc.core.task.TcTaskTypeUtil;
import org.dkpro.tc.ml.report.TcBatchReportBase;

/**
 * This is a slightly ugly solution for recording the DKPro Lab output folder of an experiment to
 * read result files in JUnit tests
 */
public class CvContextMemoryReport
    extends TcBatchReportBase
{
    public static List<String> mlaAdapters;

    @Override
    public void execute() throws Exception
    {
        mlaAdapters = getContextIdOfMachineLearningAdapter();
    }

    /**
     * Retrieves the context ids of all machine learning adapter folders that have been created in
     * this cross-validation run. Behavior undefined if this method is called in a train test setup
     * 
     * @return a list of context ids of the machine learning adapter folders
     * @throws Exception
     *             in case read operations fail
     */
    public List<String> getContextIdOfMachineLearningAdapter() throws Exception
    {

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

    private List<String> getSubTasks(File attributesTXT) throws Exception
    {
        List<String> readLines = FileUtils.readLines(attributesTXT, "utf-8");

        int idx = 0;
        for (String line : readLines) {
            if (line.startsWith(BatchTask.SUBTASKS_KEY)) {
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
            if (TcTaskTypeUtil.isMachineLearningAdapterTask(getContext().getStorageService(),
                    task.trim())) {
                results.add(task.trim());
            }
        }

        return results;
    }

}
