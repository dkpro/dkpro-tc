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
package de.tudarmstadt.ukp.dkpro.tc.ml.liblinear;

import java.io.File;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;

/**
 * Report that computes evaluation results given the classification results.
 */
public class LiblinearClassificationReport
    extends ReportBase
    implements Constants
{

    @Override
    public void execute()
        throws Exception
    {
    	// only a mock for now - this needs to be rewritten anyway once the evaluation module is ready
    	File evalFile = new File(getContext().getStorageLocation(TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE),
	    		LiblinearAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.evaluationFile));  	
    	
        Properties props = new Properties();
    	for (String line : FileUtils.readLines(evalFile)) {
    		String[] parts = line.split("=");
    		props.setProperty(parts[0], parts[1]);
    	}
	
        // Write out properties
        getContext().storeBinary(Constants.RESULTS_FILENAME, new PropertiesAdapter(props));
        
    }
}