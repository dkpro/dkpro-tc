/*******************************************************************************
 * Copyright 2015
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
package org.dkpro.tc.ml.liblinear;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;

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
        File folder = getContext().getFolder(TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE);
    	File evalFile = new File(folder,
	    		Constants.RESULTS_FILENAME);  	
    	
    	File outputFolder = getContext().getFolder("", AccessMode.READWRITE);
    	FileUtils.moveFile(evalFile, new File(outputFolder, Constants.RESULTS_FILENAME));
    }
}