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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;

/**
 * Format is
 * outcome TAB index:value TAB index:value TAB ...
 * 
 * Zeros are omitted.
 * Indexes need to be sorted.
 * 
 * For example:
 *  1 1:1  3:1  4:1   6:1
 *  2 2:1  3:1  5:1   7:1
 *  1 3:1  5:1
 */
public class LibsvmDataWriter 
	implements DataWriter
{
    
	@Override
	public void write(File outputDirectory, FeatureStore featureStore,
			boolean useDenseInstances, String learningMode, boolean applyWeighting) 
			        throws Exception 
	{
	 
	}
}