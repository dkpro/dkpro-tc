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
package org.dkpro.tc.core.task.deep;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.DeepLearningConstants;

/**
 * Prunes the embedding if one is provided
 */
public class EmbeddingTask
    extends ExecutableTaskBase
{
    /**
     * Public name of the task key
     */
    public static final String OUTPUT_KEY = "output";
    /**
     * Public name of the folder where meta information will be stored within the task
     */
    public static final String INPUT_MAPPING = "mappingInput";

    @Discriminator(name = DeepLearningConstants.DIM_PRETRAINED_EMBEDDINGS)
    private String embedding;

    @Discriminator(name = DeepLearningConstants.DIM_VECTORIZE_TO_INTEGER)
    private boolean integerVectorization;

    int lenVec = -1;

    @Override
    public void execute(TaskContext aContext) throws Exception
    {
        if (embedding == null) {
            // create folder
            aContext.getFolder(OUTPUT_KEY, AccessMode.READWRITE);
            return;
        }

        if (integerVectorization) {
            integerPreparation(aContext);
        }
        else {
            wordPreparation(aContext);
        }

    }

    private void wordPreparation(TaskContext aContext) throws Exception
    {
        Set<String> vocabulary = loadVocabulary(aContext);

        try (BufferedReader reader = getEmbeddingReader();
                BufferedWriter writer = getPrunedEmbeddingWriter(aContext)) {

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                int indexOf = line.indexOf(" ");
                String token = line.substring(0, indexOf);

                if (vocabulary.contains(token)) {
                    writer.write(line + "\n");
                    vocabulary.remove(token);
                }
                if (lenVec < 0) {
                    String vector = line.substring(indexOf + 1);
                    lenVec = vector.split(" ").length;
                }
            }

            for (String k : vocabulary) {
                writer.write(k + " " + randomVector(lenVec) + "\n");
            }
        }
    }

    private Set<String> loadVocabulary(TaskContext aContext) throws IOException
    {

        File mappingFolder = aContext.getFolder(INPUT_MAPPING, AccessMode.READONLY);
        File mappingFile = new File(mappingFolder, DeepLearningConstants.FILENAME_VOCABULARY);

        List<String> lines = FileUtils.readLines(mappingFile, UTF_8);
        Set<String> m = new HashSet<>();

        for (String l : lines) {
            if (l.isEmpty()) {
                continue;
            }
            m.add(l);
        }

        return m;
    }

    private void integerPreparation(TaskContext aContext) throws Exception
    {
        Map<String, String> tokenIdMap = loadWord2IntegerMap(aContext);

        try (BufferedReader reader = getEmbeddingReader();
                BufferedWriter writer = getPrunedEmbeddingWriter(aContext)) {

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                int indexOf = line.indexOf(" ");
                String token = line.substring(0, indexOf);
                String vector = line.substring(indexOf + 1);

                if (tokenIdMap.containsKey(token)) {
                    writer.write(tokenIdMap.get(token) + " " + vector + "\n");
                    tokenIdMap.remove(token);
                }
                if (lenVec < 0) {
                    lenVec = vector.split(" ").length;
                }
            }

            for (Entry<String, String> e : tokenIdMap.entrySet()) {
                writer.write(e.getValue() + " " + randomVector(lenVec) + "\n");
            }
        }
    }

    private BufferedReader getEmbeddingReader() throws Exception
    {
        return new BufferedReader(
                new InputStreamReader(new FileInputStream(new File(embedding)), UTF_8));

    }

    private BufferedWriter getPrunedEmbeddingWriter(TaskContext aContext) throws Exception
    {
        return new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(aContext.getFolder(OUTPUT_KEY, AccessMode.READWRITE),
                        DeepLearningConstants.FILENAME_PRUNED_EMBEDDING)),
                UTF_8));
    }

    private Map<String, String> loadWord2IntegerMap(TaskContext aContext) throws IOException
    {
        File mappingFolder = aContext.getFolder(INPUT_MAPPING, AccessMode.READONLY);
        File mappingFile = new File(mappingFolder, DeepLearningConstants.FILENAME_INSTANCE_MAPPING);

        List<String> lines = FileUtils.readLines(mappingFile, UTF_8);
        Map<String, String> m = new HashMap<>();

        for (String l : lines) {
            if (l.isEmpty()) {
                continue;
            }
            String[] entry = l.split("\t");
            m.put(entry[0], entry[1]);
        }

        return m;
    }

    public static String randomVector(int aSize, long seed)
    {
        Random rand = new Random(seed);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < aSize; i++) {
            float f = (rand.nextFloat() - 0.5f) / aSize;
            sb.append(String.format(Locale.US, "%.5f", f));
            if (i + 1 < aSize) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public static String randomVector(int aSize)
    {
        return randomVector(aSize, 123456789);
    }
}