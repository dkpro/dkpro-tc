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

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;

/**
 * Interface for data writers that write instances in the representation format used by machine
 * learning tools.
 * 
 * @author zesch
 * 
 */
public interface DataWriter
{
    /**
     * Write the contents of the feature store to the output directory.
     * 
     * @param outputDirectory
     * @param featureStore
     * @param useDenseInstances
     * @param learningMode
     * @throws Exception
     */
    public void write(File outputDirectory, FeatureStore featureStore, boolean useDenseInstances,
            String learningMode)
        throws Exception;
}
