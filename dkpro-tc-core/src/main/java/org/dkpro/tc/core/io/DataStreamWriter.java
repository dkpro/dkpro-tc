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
     * Write the contents of the feature store to the output directory.
     */
    public void write(Collection<Instance> instances)
        throws Exception;
    
    public void init(File f) throws Exception;
    
	public void transform(File outputDirectory, boolean b, String learningMode,
			boolean applyWeighting) throws Exception;
	
	public void close() throws IOException;
}
