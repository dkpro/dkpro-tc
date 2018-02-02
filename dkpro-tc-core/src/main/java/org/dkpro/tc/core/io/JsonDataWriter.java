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
package org.dkpro.tc.core.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Iterator;

import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;

import com.google.gson.Gson;

/**
 * Writes the feature store to a JSON file. Mainly used for testing purposes.
 */
public class JsonDataWriter
    implements DataWriter, Constants
{
    /**
     * Public name of the JSON dump file
     */
    public static final String JSON_FILE_NAME = "fs.json";
    
    BufferedWriter bw = null;

    private Gson gson = new Gson();

    private File outputDirectory;


    @Override
    public void writeGenericFormat(Collection<Instance> instances)
        throws Exception
    {
        throw new UnsupportedOperationException("Not supported in this implementation - use classifier specific methods instead");
    }

    @Override
    public void transformFromGeneric()
        throws Exception
    {
        throw new UnsupportedOperationException("Not supported in this implementation - use classifier specific methods instead");
    }

    @Override
    public void writeClassifierFormat(Collection<Instance> instances)
        throws Exception
    {
        init();

        Iterator<Instance> iterator = instances.iterator();
        while (iterator.hasNext()) {
            Instance next = iterator.next();
            bw.write(gson.toJson(next) + System.lineSeparator());
        }
        bw.close();
        bw = null;   
    }

    private void init() throws IOException
    {
        if (bw != null) {
            return;
        }
        bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(outputDirectory, JSON_FILE_NAME), true), "utf-8"));        
    }

    @Override
    public void init(File outputDirectory, boolean useSparse, String learningMode,
            boolean applyWeighting, String [] outcomes)
                throws Exception
    {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public boolean canStream()
    {
        return true;
    }

    @Override
    public String getGenericFileName()
    {
        throw new UnsupportedOperationException("Not supported in this implementation - use classifier specific methods instead");
    }

    @Override
    public void close()
        throws Exception
    {
        
    }
}
