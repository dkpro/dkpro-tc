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
import java.io.IOException;
import java.util.Collection;

import org.dkpro.tc.api.features.Instance;

/**
 * Interface for data writers that write instances in the representation format used by machine
 * learning tools.
 */
public interface DataStreamWriter
{
    /**
     * Write the feature instances in a generic format. This is necessary if either feature filter
     * are provided or the input data format of the classifier requies a header which can only be
     * created once all feature information e.g. names and outcomes have been seen (WEKA)
     */
    public void writeGenericFormat(Collection<Instance> instances)
        throws Exception;

    /**
     * If the generic data format is/must be used - this method will read the generic file and
     * create the classifier-fitted output format
     */
    public void transformFromGeneric()
        throws Exception;

    /**
     * Writes directly into the data format of the classifier. This is considerably faster and the
     * preferred way.
     */
    public void writeClassifierFormat(Collection<Instance> instances, boolean compress)
        throws Exception;

    /**
     * Lazy initialization of the writer component which writes either a generic file or the
     * classifier file
     */
    public void init(File outputDirectory, boolean useSparse, String learningMode, boolean applyWeighting)
        throws Exception;

    public void close()
        throws IOException;

    public boolean canStream();

    public boolean classiferReadsCompressed();
}
