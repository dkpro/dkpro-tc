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
package org.dkpro.tc.core.io;

import java.io.File;
import java.util.Collection;

import org.dkpro.tc.api.features.Instance;

/**
 * Interface for data writers that write instances in the representation format used by machine
 * learning tools.
 */
public interface DataWriter
{
    void writeGenericFormat(Collection<Instance> instances)
        throws Exception;

    /**
     * If the generic data format is/must be used - this method will read the generic file and
     * create the classifier-fitted output format
     * @throws java.lang.Exception
     * 			in case of error
     */
    void transformFromGeneric()
        throws Exception;
    /**
    * @param instances
    * 		   collection of instances
    * @param compress
    * 			compress feature file 
    * @throws java.lang.Exception
    * 			in case of error
    * */
    void writeClassifierFormat(Collection<Instance> instances, boolean compress)
        throws Exception;

    /**
     * @param outputDirectory
     * 			the output directory
     * @param useSparse
     * 			use sparse feature
     * @param learningMode
     * 			the learning model
     * @param applyWeighting
     * 			apply weights
     * @param outcomes
     * 			all outcomes
     * @throws java.lang.Exception
     * 			in case of error
     * */
    void init(File outputDirectory, boolean useSparse, String learningMode, boolean applyWeighting, String [] outcomes)
        throws Exception;

    /**
     * @return boolean if streaming is available
     */
    boolean canStream();

    /**
     * @return boolean if classifier reads compressed files
     */
    boolean classiferReadsCompressed();

    /**
     * @return string holding the generic working file
     */
    String getGenericFileName();
    
    /**
     * @throws java.lang.Exception
     * 			if error occurs
     */
    void close() throws Exception;
}
