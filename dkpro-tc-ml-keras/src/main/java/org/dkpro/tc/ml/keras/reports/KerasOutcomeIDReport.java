/*******************************************************************************
 * Copyright 2017
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

package org.dkpro.tc.ml.keras.reports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.core.ml.TcDeepLearningAdapter;
import org.dkpro.tc.ml.keras.KerasTestTask;
import org.dkpro.tc.ml.report.util.SortedKeyProperties;

public class KerasOutcomeIDReport
    extends ReportBase
{

    /**
     * Character that is used for separating fields in the output file
     */
    public static final String SEPARATOR_CHAR = ";";

    private static final String THRESHOLD_DUMMY_CONSTANT = "-1";

    @Override
    public void execute()
        throws Exception
    {
        File prepFolder = getContext().getFolder(TcDeepLearningAdapter.PREPARATION_FOLDER,
                AccessMode.READONLY);
        File mapping = new File(prepFolder, DeepLearningConstants.FILENAME_OUTCOME_MAPPING);
        List<String> outcomeMappings = FileUtils.readLines(mapping, "utf-8");

        StringBuilder header = new StringBuilder();
        header.append("labels ");
        for (String m : outcomeMappings) {
            String[] split = m.split("\t");
            int val = Integer.valueOf(split[1]) - 1; // FIXME: The evaluation module expects the counting to start at zero...
            header.append(val + "=" + split[0]+ " ");
        }

        File file = getContext().getFile(KerasTestTask.PREDICTION_FILE, AccessMode.READONLY);
        List<String> predictions = FileUtils.readLines(file, "utf-8");

        Properties prop = new SortedKeyProperties();
        for (String p : predictions) {
            if(p.startsWith("#Gold")){
                continue;
            }
            
            int id = new Random().nextInt();
            
            String[] split = p.split("\t");
            String gold = Integer.valueOf(Integer.valueOf(split[0])-1).toString(); // FIXME: Shift index to start at zero ... urghs
            String prediction = Integer.valueOf(Integer.valueOf(split[1])-1).toString(); // FIXME: Shift index to start at zero ... urghs
            prop.setProperty("" + id,
                    prediction + SEPARATOR_CHAR + gold + SEPARATOR_CHAR + THRESHOLD_DUMMY_CONSTANT);
        }

        File id2o = getContext().getFile(Constants.ID_OUTCOME_KEY, AccessMode.READWRITE);
        OutputStreamWriter fos = new OutputStreamWriter(new FileOutputStream(id2o), "utf-8");
        prop.store(fos, header.toString());
        fos.close();
    }

}