/*******************************************************************************
 * Copyright 2017
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;

/**
 * Collects information about the entire document
 * 
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
    public static String INPUT_MAPPING = "mappingInput";

    @Discriminator(name = Constants.DIM_LEARNING_MODE)
    private String learningMode;

    @Discriminator(name = DeepLearningConstants.DIM_PRETRAINED_EMBEDDINGS)
    private File embedding;

    Map<String, String> tokenIdMap;
    
    String unknownVector=null;

    int lenVec=-1;
    
    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        if (embedding == null) {
            return;
        }

        tokenIdMap = loadMap(aContext);

        BufferedReader reader = getReader(aContext);
        BufferedWriter writer = getWriter(aContext);

        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            
            int indexOf = line.indexOf(" ");
            String token = line.substring(0, indexOf);
            String vector = line.substring(indexOf+1);
            
            if (tokenIdMap.containsKey(token)) {
                writer.write(tokenIdMap.get(token) + "\t" + vector + System.lineSeparator());
                tokenIdMap.remove(token);
            }
            if(lenVec < 0){
                lenVec = vector.split(" ").length;
            }
        }

        for(String k : tokenIdMap.keySet()){
            initUnknown(lenVec);
            writer.write(tokenIdMap.get(k) + "\t" + unknownVector + System.lineSeparator());
        }

        writer.close();
        reader.close();

    }

    private void initUnknown(int len)
    {
        if(unknownVector!=null){
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        for(int i=0; i < len; i++){
            sb.append("1.0 ");
        }
        unknownVector = sb.toString().trim();
    }

    private BufferedReader getReader(TaskContext aContext)
        throws Exception
    {
        return new BufferedReader(new InputStreamReader(new FileInputStream(embedding), "utf-8"));

    }

    private BufferedWriter getWriter(TaskContext aContext)
        throws Exception
    {
        return new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(
                                new File(aContext.getFolder(OUTPUT_KEY, AccessMode.READWRITE),
                                        DeepLearningConstants.FILENAME_PRUNED_EMBEDDING)),
                        "utf-8"));
    }

    private Map<String, String> loadMap(TaskContext aContext)
        throws IOException
    {
        File mappingFolder = aContext.getFolder(INPUT_MAPPING, AccessMode.READONLY);
        File mappingFile = new File(mappingFolder, DeepLearningConstants.FILENAME_INSTANCE_MAPPING);

        List<String> lines = FileUtils.readLines(mappingFile, "utf-8");
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
}