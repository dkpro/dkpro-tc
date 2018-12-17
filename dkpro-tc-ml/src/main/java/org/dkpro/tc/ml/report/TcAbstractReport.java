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

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class TcAbstractReport
    extends BatchReportBase
    implements Constants
{

    /**
     * Collects recursively all <b>subtasks</b> stored in the <i>attributes.txt</i>. of a task and
     * the tasks located in a lower level in the hierarchy.
     * 
     * @param subtasks
     *            set of subtasks to be iterated
     * @return set of all task ids including the one passed as parameter
     * @throws Exception
     *             in case of errors
     */
    public Set<String> collectTasks(Set<String> subtasks) throws Exception
    {

        Set<String> ids = new HashSet<>();
        for (String taskId : subtasks) {

            Set<String> taskIds = collectSubtasks(taskId);

            ids.add(taskId);
            ids.addAll(taskIds);
        }

        return ids;
    }

    /**
     * Collects recursively all <b>subtasks</b> stored in the <i>attributes.txt</i>. of a task and
     * the tasks located in a lower level in the hierarchy.
     * 
     * @param contextId
     *            the current context id
     * @return set of all task ids including the one passed as parameter
     * @throws Exception
     *             in case of errors
     */
    public Set<String> collectSubtasks(String contextId) throws Exception
    {
        Set<String> ids = new HashSet<>();
        StorageService store = getContext().getStorageService();
        File attributes = store.locateKey(contextId, Task.ATTRIBUTES_KEY);
        Set<String> taskIds = readSubTasks(attributes);

        ids.add(contextId);
        ids.addAll(taskIds);
        return ids;
    }

    private Set<String> readSubTasks(File attributesTXT) throws Exception
    {
        List<String> readLines = FileUtils.readLines(attributesTXT, UTF_8);

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
            File subAttribute = getContext().getStorageService().locateKey(task.trim(),
                    Task.ATTRIBUTES_KEY);
            results.addAll(readSubTasks(subAttribute));
        }

        return results;
    }

    /**
     * Takes context meta data objects and returns their context ids as string values
     * 
     * @param subtasks
     *            arbitrary number of TaskContextMetadata objects
     * @return collection of strings with context ids extracted from the meta data
     */
    public Set<String> getTaskIdsFromMetaData(TaskContextMetadata... subtasks)
    {

        Set<String> taskIds = new HashSet<>();

        for (TaskContextMetadata tcm : subtasks) {
            taskIds.add(tcm.getId());
        }

        return taskIds;
    }

    public String getDiscriminator(TaskContext aContext, String key)
    {
        return getDiscriminator(aContext.getStorageService(), aContext.getId(), key);
    }

    /**
     * Retrieves the value of a certain key from the discriminators. A key might occur in several
     * tasks but it is assumed that the value of this key is always the same, i.e. the first found
     * entry is returned regardless in which task this key is found
     * 
     * @param store
     *            the storage
     * @param contextId
     *            id of the context in which to look
     * @param constant
     *            the key that is to be found
     * @return value of the key if found otherwise null
     */
    public String getDiscriminator(StorageService store, String contextId, String constant)
    {

        Map<String, String> map = store
                .retrieveBinary(contextId, Task.DISCRIMINATORS_KEY, new PropertiesAdapter())
                .getMap();

        if (map == null) {
            return null;
        }

        for (Entry<String, String> e : map.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();

            if (k.endsWith("|" + constant)) {
                if (v == null || v.equals("null")) {
                    return null;
                }
                return v;
            }
        }

        return null;
    }

    public String getDiscriminator(StorageService store, Set<String> contextIds, String key)
    {

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
