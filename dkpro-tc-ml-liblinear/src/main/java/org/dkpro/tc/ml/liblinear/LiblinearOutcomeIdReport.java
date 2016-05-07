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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;

/**
 * Creates id 2 outcome report
 */
public class LiblinearOutcomeIdReport
    extends ReportBase
    implements Constants
{

    @Override
    public void execute()
        throws Exception
    {
        Map<Integer, String> id2label = getId2LabelMapping();

        String header = buildHeader(id2label);

        List<String> predictions = readPredictions();

        Properties prop = new Properties();
        int lineCounter = 0;
        for (String line : predictions) {
            if (line.startsWith("#")) {
                continue;
            }
            String[] split = line.split(LiblinearTestTask.SEPARATOR_CHAR);
            String pred = id2label.get(Integer.valueOf(split[0]));
            String gold = id2label.get(Integer.valueOf(split[1]));
            prop.setProperty("" + lineCounter++, pred + LiblinearTestTask.SEPARATOR_CHAR + gold);
        }

        File targetFile = getId2OutcomeFileLocation();

        FileWriterWithEncoding fw = new FileWriterWithEncoding(targetFile, "utf-8");
        prop.store(fw, header);
        fw.close();

    }

    private File getId2OutcomeFileLocation()
    {
        File evaluationFolder = getContext().getFolder("", AccessMode.READWRITE);
        return new File(evaluationFolder, ID_OUTCOME_KEY);
    }

    private List<String> readPredictions()
        throws IOException
    {
        File predFolder = getContext().getFolder(TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE);
        String predFileName = LiblinearAdapter.getInstance().getFrameworkFilename(
                AdapterNameEntries.predictionsFile);
        return FileUtils.readLines(new File(predFolder, predFileName));
    }

    private String buildHeader(Map<Integer, String> id2label)
        throws UnsupportedEncodingException
    {
        StringBuilder header = new StringBuilder();
        header.append("ID=PREDICTION;GOLDSTANDARD" + "\n" + "labels" + " ");
        int numKeys = id2label.keySet().size();
        List<Integer> keys = new ArrayList<Integer>(id2label.keySet());
        for (int i = 0; i < numKeys; i++) {
            Integer key = keys.get(i);
            header.append(key + "=" + URLEncoder.encode(id2label.get(key), "UTF-8"));
            if (i + 1 < numKeys) {
                header.append(" ");
            }
        }
        return header.toString();
    }

    private Map<Integer, String> getId2LabelMapping()
        throws Exception
    {
        File mappingFile = getContext().getFolder(TEST_TASK_INPUT_KEY_TEST_DATA,
                StorageService.AccessMode.READONLY);
        String fileName = LiblinearAdapter.getOutcomeMappingFilename();
        File file = new File(mappingFile, fileName);
        Map<Integer, String> map = new HashMap<Integer, String>();

        List<String> lines = FileUtils.readLines(file);
        for (String line : lines) {
            String[] split = line.split("\t");
            map.put(Integer.valueOf(split[1]), split[0]);
        }

        return map;
    }
}