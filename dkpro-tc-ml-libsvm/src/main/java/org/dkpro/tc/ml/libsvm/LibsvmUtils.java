/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.ml.libsvm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class LibsvmUtils
{

    public static String outcomeMap2String(Map<String, Integer> map)
    {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, Integer> entry : map.entrySet()) {
            sb.append(entry.getKey());
            sb.append("\t");
            sb.append(entry.getValue());
            sb.append("\n");
        }

        return sb.toString();
    }

    public static Map<String, Integer> createMapping(File... files)
        throws IOException
    {
        Set<String> uniqueOutcomes = new HashSet<>();
        for (File f : files) {
            uniqueOutcomes.addAll(pickOutcomes(f));
        }

        Map<String, Integer> mapping = new HashMap<>();
        int id = 0;
        for (String o : uniqueOutcomes) {
            mapping.put(o, id++);
        }

        return mapping;
    }

    private static Collection<? extends String> pickOutcomes(File file)
        throws IOException
    {
        Set<String> outcomes = new HashSet<>();

        BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "utf-8"));

        String line = null;
        while (((line = br.readLine()) != null)) {
            if (line.isEmpty()) {
                continue;
            }
            int firstTabIdx = line.indexOf("\t");
            outcomes.add(line.substring(0, firstTabIdx));
        }
        br.close();
        return outcomes;
    }

    public static File replaceOutcomeByIntegerValue(File file, Map<String, Integer> outcomeMapping)
        throws IOException
    {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "utf-8"));
        File outFile = File.createTempFile("liblinear" + System.nanoTime(), ".tmp");
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"));

        String line = null;
        while (((line = br.readLine()) != null)) {
            if (line.isEmpty()) {
                continue;
            }
            int firstTabIdx = line.indexOf("\t");
            Integer id = outcomeMapping.get(line.substring(0, firstTabIdx));
            bw.write(id + line.substring(firstTabIdx) + "\n");
        }
        br.close();
        bw.close();

        return outFile;
    }
}
