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
package org.dkpro.tc.core.task.deep.anno.res;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.core.DeepLearningConstants;
import static java.nio.charset.StandardCharsets.UTF_8;
public class ListMappingAnnotator
    extends LookupResourceAnnotator
{

    Map<String, Integer> map = new HashMap<>();

    int nextId = 0;

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        // do nothing here
    }

    @Override
    public void collectionProcessComplete()
    {

        init(); // initialize at the end, the resources we need are not available during
                // initialization of the annotator!

        List<String> mappedDict = processDictionary(dictionaryPath);
        writeMappedDictionary(dictionaryPath, mappedDict);

        writeUpdatedInstanceMapping();

    }

    private void init()
    {
        try {
            List<String> instanceMappings = FileUtils.readLines(
                    new File(targetFolder, DeepLearningConstants.FILENAME_INSTANCE_MAPPING),
                    UTF_8);
            for (String e : instanceMappings) {
                String[] split = e.split("\t");

                String val = split[0];
                Integer key = Integer.valueOf(split[1]);
                map.put(val, key);

                if (key > nextId) {
                    nextId = key;
                }
            }

            nextId += 1; // next free id

        }
        catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private void writeUpdatedInstanceMapping()
    {

        List<String> mapping = new ArrayList<>();

        List<String> keySet = new ArrayList<>(map.keySet());
        Collections.sort(keySet);

        for (String key : keySet) {
            mapping.add(key + "\t" + map.get(key));
        }

        try {
            FileUtils.writeLines(
                    new File(targetFolder, DeepLearningConstants.FILENAME_INSTANCE_MAPPING),
                    UTF_8.toString(), mapping);
        }
        catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }

    }

    private void writeMappedDictionary(String sourceDict, List<String> dict)
    {
        File file = new File(sourceDict);
        try {
            FileUtils.writeLines(new File(targetFolder, file.getName()), UTF_8.toString(), dict);
        }
        catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private List<String> processDictionary(String dict)
    {
        List<String> mappedDict = new ArrayList<>();
        try {
            List<String> readLines = FileUtils.readLines(new File(dict), UTF_8);
            for (String e : readLines) {

                String word = e.trim();
                Integer integer = map.get(word);
                if (integer == null) {
                    integer = nextId++;
                    map.put(word, integer);
                }
                mappedDict.add(integer.toString());
            }

        }
        catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
        return mappedDict;
    }
}
