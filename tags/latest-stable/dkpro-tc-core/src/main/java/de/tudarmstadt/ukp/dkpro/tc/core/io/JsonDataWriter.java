/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.core.io;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

/**
 * Writes the feature store to a JSON file. Mainly used for testing purposes.
 * 
 * @author zesch
 * 
 */
public class JsonDataWriter
    implements DataWriter, Constants
{
    /**
     * Public name of the JSON dump file
     */
    public static final String JSON_FILE_NAME = "fs.json";

    private Gson gson = new Gson();

    @Override
    public void write(File outputDirectory, FeatureStore featureStore, boolean useDenseInstances,
            String learningMode)
        throws Exception
    {
        FileUtils.writeStringToFile(new File(outputDirectory, JSON_FILE_NAME),
                gson.toJson(featureStore));
    }
}
