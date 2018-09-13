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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

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

    public static Map<String, String> replaceKeyWithConstant(Map<String, String> discriminatorsMap,
            String key, String replacement)
    {
        Map<String,String> map = new HashMap<>();
        
        for(Entry<String,String> e : discriminatorsMap.entrySet()) {
            String v = e.getValue();
            if(e.getKey().equals(key)) {
                v = replacement;
            }
            map.put(e.getKey(), v);
        }
        
        return map;
    }

    public static Map<String, String> prefixKeys(Map<String, String> map,
            String prefix)
    {
        Map<String, String> m = new HashMap<>();

        for (Entry<String, String> e : map.entrySet()) {
            m.put(prefix + e.getKey(), e.getValue());
        }

        return m;
    }

    public static Map<String, String> shortenValuesTo(Map<String, String> discriminatorsMap, final int maxLen)
    {
        Map<String, String> m = new HashMap<>();

        for(Entry<String, String> e : discriminatorsMap.entrySet()) {
            String value = e.getValue();
            if (value.length() > maxLen) {
                value = value.substring(0, maxLen);
            }
            m.put(e.getKey(), value);
        }

        return m;
    }

    

}