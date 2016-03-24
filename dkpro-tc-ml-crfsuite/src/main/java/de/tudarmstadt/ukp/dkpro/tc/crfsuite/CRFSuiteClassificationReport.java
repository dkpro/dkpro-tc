/**
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.crfsuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;

import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants;

public class CRFSuiteClassificationReport
    extends ReportBase
    implements Constants
{

    @Override
    public void execute()
        throws Exception
    {
    	File folder = getContext().getFolder(TEST_TASK_OUTPUT_KEY,
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
        
        
        File accuracyFile = getContext().getFile(Constants.RESULTS_FILENAME, AccessMode.READWRITE);
        
        Double accuracy = correct/(correct+incorrect);
        
        Properties p = new Properties();
        p.setProperty(ReportConstants.CORRECT, correct.toString());
        p.setProperty(ReportConstants.INCORRECT, incorrect.toString());
        p.setProperty(ReportConstants.PCT_CORRECT, accuracy.toString());
        p.store(new FileOutputStream(accuracyFile), "Accuracy on test data");

        br.close();
    }
}
