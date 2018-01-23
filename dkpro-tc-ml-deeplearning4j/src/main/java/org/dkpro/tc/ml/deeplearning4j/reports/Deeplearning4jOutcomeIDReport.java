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

package org.dkpro.tc.ml.deeplearning4j.reports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.core.ml.TcDeepLearningAdapter;
import org.dkpro.tc.core.task.deep.PreparationTask;
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
	public void execute() throws Exception {

		String string = getDiscriminators()
				.get(PreparationTask.class.getName() + "|" + DeepLearningConstants.DIM_VECTORIZE_TO_INTEGER);
		boolean isIntegerMode = Boolean.valueOf(string);

		Map<String, String> map = loadMap(isIntegerMode);

		StringBuilder header = new StringBuilder();
		header.append("labels ");

		List<String> sortedKeys = new ArrayList<>(map.keySet());
		Collections.sort(sortedKeys);
		for (String m : sortedKeys) {
			Integer val = Integer.valueOf(map.get(m));
			header.append(val + "=" + m + " ");
		}

		File file = getContext().getFile(Deeplearning4jTestTask.PREDICTION_FILE, AccessMode.READONLY);
		List<String> predictions = getPredictions(file);

		List<String> nameOfTargets = getNameOfTargets();

		Properties prop = new SortedKeyProperties();

		int shift = 0;
		for (int i = 0; i < predictions.size(); i++) {

			String p = predictions.get(i);
			if (p.startsWith("#Gold")) {
				// header line exists in the prediction file and in the name of
				// targets files
				continue;
			}
			if (p.isEmpty()) {
				shift++;
				continue;
			}

			String id = nameOfTargets.get(i - shift);

			String[] split = p.split("\t");

			String gold = null;
			String prediction = null;
			if (isIntegerMode) {
				// Keras starts counting at 1 for 'content' - zero is reserved
				// as padding value - we have to shift-correct the index
				Integer v = Integer.valueOf(Integer.valueOf(split[0]));
				gold = v.toString();
				v = Integer.valueOf(Integer.valueOf(split[1]));
				prediction = v.toString();
			} else {
				// we have non-integer labels so we have to map them to integers
				// for creating the id2outcome data format
				gold = map.get(split[0]).toString();
				prediction = map.get(split[1]).toString();
			}
			prop.setProperty("" + id, prediction + SEPARATOR_CHAR + gold + SEPARATOR_CHAR + THRESHOLD_DUMMY_CONSTANT);
		}
		
		header.append("\nToken\tPrediction\tGold");

		File id2o = getContext().getFile(Constants.ID_OUTCOME_KEY, AccessMode.READWRITE);
		OutputStreamWriter fos = new OutputStreamWriter(new FileOutputStream(id2o), "utf-8");
		prop.store(fos, header.toString());
		fos.close();
	}

	private Map<String, String> loadMap(boolean isIntegerMode) throws IOException {

		Map<String, String> m = new HashMap<>();

		if (isIntegerMode) {

			File prepFolder = getContext().getFolder(TcDeepLearningAdapter.PREPARATION_FOLDER, AccessMode.READONLY);
			File mapping = new File(prepFolder, DeepLearningConstants.FILENAME_OUTCOME_MAPPING);

			List<String> outcomeMappings = FileUtils.readLines(mapping, "utf-8");
			for (String s : outcomeMappings) {
				String[] split = s.split("\t");
				m.put(split[0], split[1]);
			}
			return m;
		}

		File file = getContext().getFile(Deeplearning4jTestTask.PREDICTION_FILE, AccessMode.READONLY);
		List<String> readLines = FileUtils.readLines(file, "utf-8");

		Set<String> keys = new HashSet<>();

		int mapIdx = 0;
		for (int i = 1; i < readLines.size(); i++) {
			String l = readLines.get(i);
			if (l.isEmpty()) {
				continue;
			}
			String[] e = l.split("\t");

			keys.add(e[0]);
			keys.add(e[1]);
		}

		List<String> sortedKeys = new ArrayList<String>(keys);
		Collections.sort(sortedKeys);

		for (String k : sortedKeys) {
			String string = m.get(k);
			if (string == null) {
				m.put(k, "" + (mapIdx++));
			}
		}

		return m;
	}

	private List<String> getPredictions(File file) throws IOException {
		List<String> readLines = FileUtils.readLines(file, "utf-8");
		return readLines.subList(1, readLines.size());// ignore first-line with
														// comments
	}

	private List<String> getNameOfTargets() throws IOException {
		File targetIdMappingFolder = getContext().getFolder(TcDeepLearningAdapter.TARGET_ID_MAPPING,
				AccessMode.READONLY);
		File targetIdMappingFile = new File(targetIdMappingFolder, DeepLearningConstants.FILENAME_TARGET_ID_TO_INDEX);

		List<String> t = new ArrayList<>();

		List<String> readLines = FileUtils.readLines(targetIdMappingFile, "utf-8");
		for (String s : readLines) {
			if (s.startsWith("#")) {
				continue;
			}
			if (s.isEmpty()) {
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