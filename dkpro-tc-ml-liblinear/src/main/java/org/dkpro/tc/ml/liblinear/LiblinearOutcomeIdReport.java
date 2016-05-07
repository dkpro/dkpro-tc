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
package org.dkpro.tc.ml.liblinear;

import java.io.File;

import org.apache.uima.fit.descriptor.ExternalResource;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.uima.task.TaskContextProvider;
import org.dkpro.tc.core.Constants;

/**
 * Creates id 2 outcome report
 */
public class LiblinearOutcomeIdReport
    extends ReportBase
    implements Constants
{
  @ExternalResource(api = TaskContextProvider.class) 
  private TaskContext ctx; 

    @Override
    public void execute()
        throws Exception
    {
     
        File locateKey = ctx.getStorageService().locateKey(getContext().getId(), LiblinearAdapter.getOutcomeMappingFilename());
        System.out.println(locateKey);
        int a=0;
        
//        
//        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
//                new File(ID_OUTCOME_KEY)), "utf-8"));
//
//        String header = "ID=PREDICTION;GOLDSTANDARD" + "\n" + "labels" + " ";
//
//        File predFolder = getContext().getFolder(TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE);
//        String predFileName = LiblinearAdapter.getInstance().getFrameworkFilename(
//                AdapterNameEntries.predictionsFile);
//
//        List<String> readLines = FileUtils.readLines(predFolder);
//        for (int i = 1; i < readLines.size(); i++) {
//
//        }

    }
}