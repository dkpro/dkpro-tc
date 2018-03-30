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
package org.dkpro.tc.core.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService;

/**
 * Utility methods needed in reports
 */
public class ReportUtils
{
    public static boolean containsExcludePattern(String string, List<String> patterns)
    {
        Pattern matchPattern;
        for (String pattern : patterns) {
            matchPattern = Pattern.compile(pattern);
            if (matchPattern.matcher(string).find()) {
                return true;
            }
        }
        return false;
    }

    public static void writeExcelAndCSV(TaskContext context, String contextLabel,
            TcFlexTable<String> table, String evalFileName, String suffixExcel, String suffixCsv)
        throws IOException
    {
        StorageService store = context.getStorageService();
        // Excel cannot cope with more than 255 columns
        if (table.getColumnIds().length <= 255) {
            context.storeBinary(evalFileName + "_compact" + suffixExcel, table.getExcelWriter());
        }
        context.storeBinary(evalFileName + "_compact" + suffixCsv, table.getCsvWriter());
        table.setCompact(false);
        // Excel cannot cope with more than 255 columns
        if (table.getColumnIds().length <= 255) {
            context.storeBinary(evalFileName + suffixExcel, table.getExcelWriter());
        }
        context.storeBinary(evalFileName + suffixCsv, table.getCsvWriter());

        // output the location of the batch evaluation folder
        // otherwise it might be hard for novice users to locate this
        File dummyFolder = store.locateKey(context.getId(), "dummy");
        // TODO can we also do this without creating and deleting the dummy folder?
        context.getLoggingService().message(contextLabel,
                "Storing detailed results in:\n" + dummyFolder.getParent() + "\n");
        FileUtils.deleteDirectory(dummyFolder);
    }

    public static Map<String, String> clearDiscriminatorsByExcludePattern(
            Map<String, String> discriminatorsMap, List<String> discriminatorsToExclude)
    {
        Map<String, String> cleanedDiscriminatorsMap = new HashMap<String, String>();

        for (Entry<String, String> e : discriminatorsMap.entrySet()) {
            if (!ReportUtils.containsExcludePattern(e.getKey(), discriminatorsToExclude)) {
                cleanedDiscriminatorsMap.put(e.getKey(), e.getValue());
            }
        }
        return cleanedDiscriminatorsMap;
    }

    /**
     * Iterates all entries in a map and cuts off the key value before a pipe (|) symbol, i.e.
     * several tasks might use the same discriminator. The task-specific and the actual key are
     * separated by a pipe. The task-name leads to many redundant entries that clutter the output
     * file, e.g. the feature mode occurs for each task that imports this variable. This method
     * cleans up the keys and removes this redundancy.
     * 
     * @param discriminatorsMap
     *            the map for which the keys are shortened
     * @return
     *          a cleaned map.
     */
    public static Map<String, String> removeKeyRedundancy(Map<String, String> discriminatorsMap)
    {
        Map<String, String> outMap = new HashMap<>();
        
        for(Entry<String,String> e : discriminatorsMap.entrySet()) {
            String key = e.getKey();
            if(key.contains("|")) {
                String[] split = key.split("\\|");
                key = split[1];
            }
            outMap.put(key, e.getValue());
        }
        
        return outMap;
    }

}