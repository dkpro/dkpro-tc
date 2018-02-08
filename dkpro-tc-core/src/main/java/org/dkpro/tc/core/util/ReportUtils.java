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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.impl.PropertiesAdapter;

/**
 * Utility methods needed in reports
 */
public class ReportUtils implements ReportConstants
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

    /**
     * Looks into the {@link TcFlexTable} and outputs general performance numbers if available
     * @param table
     * 		Flextable object
     * 
     * @return 
     * 		table as string
     */
    public static String getPerformanceOverview(TcFlexTable<String> table)
    {
        // output some general performance figures
        // TODO this is a bit of a hack. Is there a better way?
        Set<String> columnIds = new HashSet<String>(Arrays.asList(table.getColumnIds()));
        StringBuffer buffer = new StringBuffer("\n");
        if (columnIds.contains(PCT_CORRECT) && columnIds.contains(PCT_INCORRECT)) {
            int i = 0;
            buffer.append("ID\t% CORRECT\t% INCORRECT\n");
            for (String id : table.getRowIds()) {
                String correct = table.getValueAsString(id, PCT_CORRECT);
                String incorrect = table.getValueAsString(id, PCT_INCORRECT);
                buffer.append(i + "\t" + correct + "\t" + incorrect + "\n");
                i++;
            }
            buffer.append("\n");
        }
        else if (columnIds.contains(CORRELATION)) {
            int i = 0;
            buffer.append("ID\tCORRELATION\n");
            for (String id : table.getRowIds()) {
                String correlation = table.getValueAsString(id, CORRELATION);
                buffer.append(i + "\t" + correlation + "\n");
                i++;
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }


    public static Map<String,String> getDiscriminatorsForContext(StorageService store, String contextId, String discriminatorsKey)
    {
        return store.retrieveBinary(contextId, discriminatorsKey, new PropertiesAdapter()).getMap();
    }

    public static void writeExcelAndCSV(TaskContext context, String contextLabel, TcFlexTable<String> table, String evalFileName, String suffixExcel, String suffixCsv)
    {
        StorageService store = context.getStorageService();
        context.getLoggingService().message(contextLabel,
                ReportUtils.getPerformanceOverview(table));
        // Excel cannot cope with more than 255 columns
        if (table.getColumnIds().length <= 255) {
            context.storeBinary(evalFileName + "_compact" + suffixExcel,
                    table.getExcelWriter());
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
        dummyFolder.delete();        
    }

    public static Map<String, String> clearDiscriminatorsByExcludePattern(
            Map<String, String> discriminatorsMap, List<String> discriminatorsToExclude)
    {
        Map<String, String> cleanedDiscriminatorsMap = new HashMap<String, String>();

        for (String disc : discriminatorsMap.keySet()) {
            if (!ReportUtils.containsExcludePattern(disc, discriminatorsToExclude)) {
                cleanedDiscriminatorsMap.put(disc, discriminatorsMap.get(disc));
            }
        }
        return cleanedDiscriminatorsMap;
    }

}