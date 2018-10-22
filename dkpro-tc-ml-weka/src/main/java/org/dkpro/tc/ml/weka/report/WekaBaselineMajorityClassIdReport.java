/**
 * Copyright 2018
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
package org.dkpro.tc.ml.weka.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.IOUtils;
import org.dkpro.lab.storage.StorageService.AccessMode;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import weka.core.Attribute;

/**
 * Writes a instanceId / outcome data for each classification instance.
 */
public class WekaBaselineMajorityClassIdReport extends WekaOutcomeIDReport {

	private static final String X_PLACE_HOLDER_ZERO_VALUED_SPARSE_INSTANCE_OUTCOME = "x-undefined";
	private String majorityClass;

	public WekaBaselineMajorityClassIdReport() {
		// required by groovy
	}

	@Override
	public void execute() throws Exception {

		init();

		if (isRegression) {
			return;
		}

		super.execute();
	}

	@Override
	protected String getPrediction(Double prediction, List<String> labels, Attribute gsAtt) {
		// is overwritten in baseline reports
		Map<String, Integer> class2number = classNamesToMapping(labels);
		return class2number.get(majorityClass).toString();
	}

	@Override
	protected void prepareBaseline() throws Exception {
		File folder = getContext().getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY);
		File file = new File(folder, FILENAME_DATA_IN_CLASSIFIER_FORMAT);
		determineMajorityClass(file);
	}

	@Override
	protected File getTargetOutputFile() {
		File evaluationFolder = getContext().getFolder("", AccessMode.READWRITE);
		return new File(evaluationFolder, BASELINE_MAJORITIY_ID_OUTCOME_KEY);
	}

	private void determineMajorityClass(File file) throws Exception {

		FrequencyDistribution<String> fd = new FrequencyDistribution<>();
		String zeroOutcome = "x-init";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));

			String line = null;
			boolean isSparseMode = false;
			Integer attributeCounter = -1;
			String outcomesLine = "";
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty() || line.startsWith("@")) {
					if (line.startsWith("@attribute")) {
						attributeCounter++;
					}
					outcomesLine=line;
					continue;
				}
				
				if (outcomesLine != null) {
					String tmp = outcomesLine.replaceAll(".*\\{", "");
					tmp = tmp.replaceAll("\\}", "");
					String[] split = tmp.split(",");
					zeroOutcome = split[0];
					outcomesLine = null;
				}

				if (line.contains("{")) {
					line = line.replaceAll("\\{", "");
					line = line.replaceAll("\\}", "");
					isSparseMode = true;
				}

				String[] split = line.split(",");

				String v = split[split.length - 1];
				if (!isSparseMode) {
					if (hasInstanceWeighting(v)) {
						v = split[split.length - 2];
					}
				} else {
					String[] outcomeField = v.split(" ");

					if (attributeCounter.toString().equals(outcomeField[0])) {
						v = outcomeField[1];
					} else {
						// if the label is mapped to the value zero it is omitted by Weka policy in the
						// .arff in sparse feature mode.
						// its not a missing value, just zero values are not explizitly shown anymore.
						// Thus, the outcome will not be listed in sparse mode if it is zero!
						v = X_PLACE_HOLDER_ZERO_VALUED_SPARSE_INSTANCE_OUTCOME;
					}
				}

				fd.addSample(v, 1);
			}

		} finally {
			IOUtils.closeQuietly(reader);
		}
		
		majorityClass = fd.getSampleWithMaxFreq();

		// if the zero-label is the most frequent one assigned the restored label
		if (majorityClass.equals(X_PLACE_HOLDER_ZERO_VALUED_SPARSE_INSTANCE_OUTCOME)) {
			majorityClass = zeroOutcome;
		}
	}

	private boolean hasInstanceWeighting(String v) {
		return v.startsWith("{") && v.endsWith("}");
	}

}