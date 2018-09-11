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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.task.TcTaskTypeUtil;

/**
 * Collects the final runtime results in a train/test setting.
 */
public class RuntimeReport
    extends TcBatchReportBase
{

    /**
     * Name of the output file where the report stores the runtime results
     */
    public static final String RUNTIME_KEY = "runtime.txt";

    private Map<String, Long> timeMap = new HashMap<String, Long>();

    @Override
    public void execute() throws Exception
    {

        List<String> keyOrdered = new ArrayList<>();

        StorageService store = getContext().getStorageService();

        Set<String> taskIds = getTaskIdsFromMetaData(getSubtasks());

        taskIds = readInnerTasksIfCrossValidation(taskIds);

        for (String id : taskIds) {

            if (TcTaskTypeUtil.isFacadeTask(store, id)) {
                Set<String> subTasks = collectSubtasks(id);
                subTasks.remove(id);
                for (String subId : subTasks) {
                    long executionTime = getExecutionTime(subId);
                    registerTime(subId, executionTime);
                    keyOrdered.add(subId);
                }

                // Facade tasks are not registered they are just a shell and do not much anyway
                continue;
            }

            long executionTime = getExecutionTime(id);
            registerTime(id, executionTime);
            keyOrdered.add(id);
        }

        String output = buildOutput(keyOrdered);
        File runtime = getContext().getFile(RUNTIME_KEY, AccessMode.READWRITE);
        FileUtils.writeStringToFile(runtime, output, "utf-8");
    }

    private Set<String> readInnerTasksIfCrossValidation(Set<String> taskIds) throws Exception
    {

        Set<String> ids = new HashSet<>();

        for (String id : taskIds) {
            if (TcTaskTypeUtil.isCrossValidationTask(getContext().getStorageService(), id)) {
                ids.addAll(collectSubtasks(id));
            }
        }

        ids.addAll(taskIds);
        return ids;
    }

    private String buildOutput(List<String> keyOrdered)
    {

        int maxLen = keyOrdered.stream().max(Comparator.comparingInt(String::length)).get()
                .length();

        StringBuffer buffer = new StringBuffer();
        buffer.append(String.format("#%" + (maxLen - 1) + "s\thh:mm:ss:ms\n", "TaskName"));
        keyOrdered.forEach(k -> buffer.append(formatOutput(maxLen, k, timeMap.get(k)) + "\n"));

        // summary
        long sum = timeMap.values().stream().mapToLong(l -> l.longValue()).sum();
        buffer.append("\n" + formatOutput(maxLen, "Total-time", sum));

        return buffer.toString();
    }

    private void registerTime(String id, long executionTime)
    {
        timeMap.put(id, executionTime);
    }

    private long getExecutionTime(String taskId)
    {
        long begin = getTime(taskId, "begin");
        long end = getTime(taskId, "end");
        return end - begin;
    }

    private long getTime(String taskId, String key)
    {
        Map<String, String> metaMap = getContext().getStorageService()
                .retrieveBinary(taskId, TaskContextMetadata.METADATA_KEY, new PropertiesAdapter())
                .getMap();

        return Long.parseLong(metaMap.get(key));
    }

    private String formatOutput(int maxLen, String key, long time)
    {
        long millis = time % 1000;
        long second = (time / 1000) % 60;
        long minute = (time / (1000 * 60)) % 60;
        long hour = (time / (1000 * 60 * 60)) % 24;

        return String.format("%" + maxLen + "s\t%02d:%02d:%02d:%d", key, hour, minute, second,
                millis);
    }
}
