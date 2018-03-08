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
package org.dkpro.tc.io.libsvm.reports;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.io.libsvm.AdapterFormat;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatWriter;
import org.dkpro.tc.ml.report.TcBatchReportBase;
import org.dkpro.tc.ml.report.util.SortedKeyProperties;

public class LibsvmDataFormatRandomBaselineIdReport extends TcBatchReportBase {

	private String THRESHOLD_CONSTANT = "-1";
	
	private Random random = new Random();
	private Double upper=Double.MIN_VALUE;
	private Double lower=Double.MAX_VALUE;

	public LibsvmDataFormatRandomBaselineIdReport() {
		
	}

	@Override
	public void execute() throws Exception {
		
		String threshold = getDiscriminator(getContext(), DIM_BIPARTITION_THRESHOLD);
		if(threshold != null){
			THRESHOLD_CONSTANT = threshold;
		}
		
		boolean isRegression = getDiscriminator(getContext(), DIM_LEARNING_MODE).equals(LM_REGRESSION);
		boolean isUnit = getDiscriminator(getContext(), DIM_FEATURE_MODE).equals(FM_UNIT);
		boolean isSequence = getDiscriminator(getContext(), DIM_FEATURE_MODE).equals(FM_SEQUENCE);

		Map<Integer, String> id2label = getId2LabelMapping(isRegression);
		String header = buildHeader(id2label, isRegression);
		
		List<String> predictions = readPredictions();
		determineRangeOfValues(predictions, isRegression);
		Map<String, String> index2instanceIdMap = getMapping(isUnit || isSequence);

		Properties prop = new SortedKeyProperties();
		int lineCounter = 0;
		
		// we iterate the prediction file but do not use the predicted value but instead
		// use the baseline value computed before as replacement
		for (String line : predictions) {
			if (line.startsWith("#")) {
				continue;
			}
			String[] split = line.split(";");
			String key = index2instanceIdMap.get(lineCounter + "");

			if (isRegression) {
				prop.setProperty(key, getRandomDouble() + ";" + split[1] + ";" + THRESHOLD_CONSTANT);
			} else {
				int pred = Integer.parseInt(getRandomInt());
				int gold = Integer.parseInt(split[1]);
				prop.setProperty(key, pred + ";" + gold + ";" + THRESHOLD_CONSTANT);
			}
			lineCounter++;
		}

		File targetFile = getBaseline2OutcomeFileLocation();

		FileWriterWithEncoding fw = null;
		try{
			fw = new FileWriterWithEncoding(targetFile, "utf-8");
			prop.store(fw, header);
		}finally{
			IOUtils.closeQuietly(fw);
		}

	}

	private void determineRangeOfValues(List<String> predictions, boolean isRegression) {
		
		for(String l : predictions){
			if(l.startsWith("#") || l.isEmpty()){
				continue;
			}
			String[] split = l.split(";");
			
			double gold = Double.parseDouble(split[1]);
			
			if (gold > upper){
				upper = gold;
			}
			if(gold < lower){
				lower = gold;
			}
		}
	}
	
	private String getRandomInt(){
		Integer r = random.nextInt(upper.intValue() - lower.intValue() + 1) + lower.intValue();
		return r.toString();
	}
	
	private String getRandomDouble(){
		Double r = random.nextDouble() * (upper - lower) + lower;
		return r.toString();
	}

	private Map<String, String> getMapping(boolean isUnit) throws IOException {

		File f;
		if (isUnit) {
			f = new File(getContext().getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY),
					LibsvmDataFormatWriter.INDEX2INSTANCEID);
		} else {
			f = new File(getContext().getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY),
					FILENAME_DOCUMENT_META_DATA_LOG);
		}

		Map<String, String> m = new HashMap<>();

		int idx = 0;
		for (String l : FileUtils.readLines(f, "utf-8")) {
			if (l.startsWith("#")) {
				continue;
			}
			if (l.trim().isEmpty()) {
				continue;
			}
			String[] split = l.split("\t");

			m.put(idx + "", split[0]);
			idx++;

		}
		return m;
	}

	private File getBaseline2OutcomeFileLocation() {
		File evaluationFolder = getContext().getFolder("", AccessMode.READWRITE);
		return new File(evaluationFolder, RANDOM_BASELINE_ID_OUTCOME_KEY);
	}

	private List<String> readPredictions() throws IOException {
		File predFolder = getContext().getFolder("", AccessMode.READWRITE);
		return FileUtils.readLines(new File(predFolder, FILENAME_PREDICTIONS), "utf-8");
	}

	private String buildHeader(Map<Integer, String> id2label, boolean isRegression)
			throws UnsupportedEncodingException {
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

	private Map<Integer, String> getId2LabelMapping(boolean isRegression) throws Exception {
		if (isRegression) {
			// no map for regression;
			return new HashMap<>();
		}

		File folder = getContext().getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY);
		String fileName = AdapterFormat.getOutcomeMappingFilename();
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