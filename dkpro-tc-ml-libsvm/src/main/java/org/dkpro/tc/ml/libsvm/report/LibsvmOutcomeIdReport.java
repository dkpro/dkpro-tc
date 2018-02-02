/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.ml.libsvm.report;

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
import org.dkpro.tc.core.task.ExtractFeaturesTask;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.task.deep.PreparationTask;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.libsvm.LibsvmTestTask;
import org.dkpro.tc.ml.libsvm.writer.LibsvmDataWriter;
import org.dkpro.tc.ml.report.util.SortedKeyProperties;

/**
 * Creates id 2 outcome report
 */
public class LibsvmOutcomeIdReport
    extends ReportBase
    implements Constants
{
    // constant dummy value for setting as threshold which is an expected field in the evaluation
    // module but is not needed/provided by liblinear
    private static final String THRESHOLD_CONSTANT = "-1";
    
    public LibsvmOutcomeIdReport(){
        //required by groovy
    }

    @Override
    public void execute()
        throws Exception
    {
        boolean isRegression = getDiscriminators()
                .get(LibsvmTestTask.class.getName() + "|" + Constants.DIM_LEARNING_MODE)
                .equals(Constants.LM_REGRESSION);
        
        boolean isUnit = getDiscriminators()
                .get(InitTask.class.getName() + "|" + Constants.DIM_FEATURE_MODE)
                .equals(Constants.FM_UNIT);
        
        Map<Integer, String> id2label = getId2LabelMapping(isRegression);
        String header = buildHeader(id2label, isRegression);

        List<String> predictions = readPredictions();
        Map<String, String> index2instanceIdMap = getMapping(isUnit);

        Properties prop = new SortedKeyProperties();
        int lineCounter = 0;
        for (String line : predictions) {
            if (line.startsWith("#")) {
                continue;
            }
            String[] split = line.split(";");
            String key = index2instanceIdMap.get(lineCounter+"");
            
            if (isRegression){
                prop.setProperty(key,
                    split[0] + ";" + split[1] + ";" + THRESHOLD_CONSTANT);
            }else{
                int pred = Double.valueOf(split[0]).intValue();
                int gold = Double.valueOf(split[1]).intValue();
                prop.setProperty(key,
                        pred + ";" + gold + ";" + THRESHOLD_CONSTANT);
            }
            lineCounter++;
        }

        File targetFile = getId2OutcomeFileLocation();

        FileWriterWithEncoding fw = new FileWriterWithEncoding(targetFile, "utf-8");
        prop.store(fw, header);
        fw.close();

    }
    
private Map<String, String> getMapping(boolean isUnit) throws IOException {
		
		File f;
		if (isUnit) {
			f = new File(getContext().getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY),
					LibsvmDataWriter.INDEX2INSTANCEID);
		} else {
			f = new File(getContext().getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY),
					Constants.FILENAME_DOCUMENT_META_DATA_LOG);
		}
		
		Map<String, String> m = new HashMap<>();

		int idx=0;
		for (String l : FileUtils.readLines(f, "utf-8")) {
			if (l.startsWith("#")) {
				continue;
			}
			if (l.trim().isEmpty()) {
				continue;
			}
			String[] split = l.split("\t");

//			if (isUnit) {
				m.put(idx + "", split[0]);
				idx++;
//			} else {
//				m.put(split[0], split[1]);
//			}

		}
		return m;
	}

    private File getId2OutcomeFileLocation()
    {
        File evaluationFolder = getContext().getFolder("", AccessMode.READWRITE);
        return new File(evaluationFolder, ID_OUTCOME_KEY);
    }

    private List<String> readPredictions()
        throws IOException
    {
        File predFolder = getContext().getFolder("", AccessMode.READWRITE);
        return FileUtils.readLines(new File(predFolder, Constants.FILENAME_PREDICTIONS), "utf-8");
    }

    private String buildHeader(Map<Integer, String> id2label, boolean isRegression)
        throws UnsupportedEncodingException
    {
        StringBuilder header = new StringBuilder();
        header.append("ID=PREDICTION;GOLDSTANDARD;THRESHOLD" + "\n" + "labels" + " ");

        if (isRegression) {
            // no label mapping for regression so that is all we have to do
            return header.toString();
        }

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

    private Map<Integer, String> getId2LabelMapping(boolean isRegression)
        throws Exception
    {
        if(isRegression){
            //no map for regression;
            return new HashMap<>();
        }
        
        File folder = getContext().getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY);
        String fileName = LibsvmAdapter.getOutcomeMappingFilename();
        File file = new File(folder, fileName);
        Map<Integer, String> map = new HashMap<Integer, String>();

        List<String> lines = FileUtils.readLines(file, "utf-8");
        for (String line : lines) {
            String[] split = line.split("\t");
            map.put(Integer.valueOf(split[1]), split[0]);
        }

        return map;
    }

}