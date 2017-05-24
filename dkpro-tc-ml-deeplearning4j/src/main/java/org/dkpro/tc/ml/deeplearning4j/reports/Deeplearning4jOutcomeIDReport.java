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

package org.dkpro.tc.ml.deeplearning4j.reports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.core.ml.TcDeepLearningAdapter;
import org.dkpro.tc.ml.deeplearning4j.Deeplearning4jTestTask;
import org.dkpro.tc.ml.report.util.SortedKeyProperties;

public class Deeplearning4jOutcomeIDReport
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
            int val = Integer.valueOf(split[1]) - 1; // FIXME: The evaluation module expects the
                                                     // counting to start at zero...
            header.append(val + "=" + split[0] + " ");
        }

        File file = getContext().getFile(Deeplearning4jTestTask.PREDICTION_FILE, AccessMode.READONLY);
        List<String> predictions = getPredictions(file);

        List<String> nameOfTargets = getNameOfTargets();

        Properties prop = new SortedKeyProperties();

        int shift = 0;
        for (int i = 0; i < predictions.size(); i++) {

            String p = predictions.get(i);
            if (p.startsWith("#Gold")) {
                // header line exists in the prediction file and in the name of targets files
                continue;
            }
            if (p.isEmpty()) {
                shift++;
                continue;
            }

            String id = nameOfTargets.get(i - shift);

            String[] split = p.split("\t");
            Integer v = Integer.valueOf(Integer.valueOf(split[0]));
            String gold = v.toString(); 
            v = Integer.valueOf(Integer.valueOf(split[1]));
            String prediction = v.toString();  
            prop.setProperty("" + id,
                    prediction + SEPARATOR_CHAR + gold + SEPARATOR_CHAR + THRESHOLD_DUMMY_CONSTANT);
        }

        File id2o = getContext().getFile(Constants.ID_OUTCOME_KEY, AccessMode.READWRITE);
        OutputStreamWriter fos = new OutputStreamWriter(new FileOutputStream(id2o), "utf-8");
        prop.store(fos, header.toString());
        fos.close();
    }

    private List<String> getPredictions(File file)
        throws IOException
    {
        List<String> readLines = FileUtils.readLines(file, "utf-8");
        return readLines.subList(1, readLines.size());// ignore first-line with comments
    }

    private List<String> getNameOfTargets()
        throws IOException
    {
        File targetIdMappingFolder = getContext().getFolder(TcDeepLearningAdapter.TARGET_ID_MAPPING,
                AccessMode.READONLY);
        File targetIdMappingFile = new File(targetIdMappingFolder,
                DeepLearningConstants.FILENAME_TARGET_ID_TO_INDEX);

        List<String> t = new ArrayList<>();

        List<String> readLines = FileUtils.readLines(targetIdMappingFile, "utf-8");
        for (String s : readLines) {
            if (s.startsWith("#")) {
                continue;
            }
            if(s.isEmpty()){
                t.add("");
                continue;
            }
            
            String[] split = s.split("\t");
            if (split[0].contains("_")) {
                t.add(s.replaceAll("\t", "_"));
            } else {
                t.add(split[1]);
            }
        }

        return t;
    }


}