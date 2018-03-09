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
import java.util.Map;

import org.apache.commons.compress.utils.IOUtils;
import org.dkpro.lab.storage.StorageService.AccessMode;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import weka.core.Attribute;

/**
 * Writes a instanceId / outcome data for each classification instance.
 */
public class WekaBaselineMajorityClassIdReport extends WekaOutcomeIDReport {

	private String majorityClass;

	public WekaBaselineMajorityClassIdReport() {
		// required by groovy
	}

	@Override
	protected String getPrediction(Double prediction, Map<String, Integer> class2number, Attribute gsAtt) {
		// is overwritten in baseline reports
		 return class2number
                 .get(majorityClass).toString();
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

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));

			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty() || line.startsWith("@")) {
					continue;
				}

				String[] split = line.split(",");
				
				String v = split[split.length-1];
				if(hasInstanceWeighting(v)){
					v = split[split.length-2];
				}
				
				fd.addSample(v, 1);
			}

		} finally {
			IOUtils.closeQuietly(reader);
		}

		majorityClass = fd.getSampleWithMaxFreq();
	}

	private boolean hasInstanceWeighting(String v) {
		return v.startsWith("{") && v.endsWith("}");
	}

}