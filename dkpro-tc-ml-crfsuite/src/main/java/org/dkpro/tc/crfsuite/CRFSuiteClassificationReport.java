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

package org.dkpro.tc.crfsuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.core.util.ReportConstants;

public class CRFSuiteClassificationReport
    extends ReportBase
    implements Constants
{

    @Override
    public void execute()
        throws Exception
    {
    	File folder = getContext().getFolder("",
                AccessMode.READWRITE);
    	String predFileName = CRFSuiteAdapter.getInstance().getFrameworkFilename(
                AdapterNameEntries.predictionsFile);
        File predFile = new File(folder,predFileName);
        
        Double correct = 0.0;
        Double incorrect = 0.0;
        
        BufferedReader br = new BufferedReader(new FileReader(predFile));
        String line=null;
        while((line=br.readLine())!=null){
            if(line.startsWith("#")){
                continue;
            }
            if(line.isEmpty()){
                continue;
            }
            String[] split = line.split("\t");
            
            if(split[0].equals(split[1])){
                correct++;
            }else{
                incorrect++;
            }
        }
        br.close();
        
        String evalFileName = Constants.RESULTS_FILENAME;
        File accuracyFile = getContext().getFile(evalFileName, AccessMode.READWRITE);
        
        Double accuracy = correct/(correct+incorrect);
        
        Properties p = new Properties();
        p.setProperty(ReportConstants.CORRECT, correct.toString());
        p.setProperty(ReportConstants.INCORRECT, incorrect.toString());
        p.setProperty(ReportConstants.PCT_CORRECT, accuracy.toString());
        
        FileOutputStream fos = new FileOutputStream(accuracyFile);
        p.store(fos, "Accuracy on test data");
        fos.close();
    }
}
