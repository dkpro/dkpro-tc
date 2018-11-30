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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.dkpro.lab.storage.StorageService.AccessMode;

import weka.core.Attribute;

/**
 * Writes a instanceId / outcome data for each classification instance.
 */
public class WekaBaselineRandomIdReport extends WekaOutcomeIDReport {

	private static final String X_PLACE_HOLDER_ZERO_VALUED_SPARSE_INSTANCE_OUTCOME = "x-undefined";

	private Random random = new Random(42);

	private List<String> pool = new ArrayList<>();

	public WekaBaselineRandomIdReport() {
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
		Map<String, Integer> class2number = classNamesToMapping(labels);
		Integer idx = random.nextInt(pool.size());
		return class2number.get(pool.get(idx)).toString();
	}

	@Override
	protected void prepareBaseline() throws Exception {
		File folder = getContext().getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY);
		File file = new File(folder, FILENAME_DATA_IN_CLASSIFIER_FORMAT);
		buildPool(file);
	}

	@Override
	protected File getTargetOutputFile() {
		File evaluationFolder = getContext().getFolder("", AccessMode.READWRITE);
		return new File(evaluationFolder, BASELINE_RANDOM_ID_OUTCOME_KEY);
	}

	private void buildPool(File file) throws Exception {

		String zeroOutcome="x-init";
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF_8))){

			String line = null;
			boolean isSparseMode = false;
			Integer attributeCounter = -1;
			String outcomesLine = "";
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty() || line.startsWith("@")) {
					if (line.startsWith("@attribute")) {
						attributeCounter++;
						outcomesLine=line;
					}
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
						// its not a missing value, just zero values are not explicitly shown anymore.
						// Thus, the outcome will not be listed in sparse mode if it is zero!
						v = X_PLACE_HOLDER_ZERO_VALUED_SPARSE_INSTANCE_OUTCOME;
					}
				}

				if (!pool.contains(v)) {
					pool.add(v);
				}
			}

		} 
		
		pool.remove(X_PLACE_HOLDER_ZERO_VALUED_SPARSE_INSTANCE_OUTCOME);
		if (!pool.contains(zeroOutcome)) {
			pool.add(zeroOutcome);
		}
		

 		Collections.shuffle(pool);
	}

	private boolean hasInstanceWeighting(String v) {
		return v.startsWith("{") && v.endsWith("}");
	}

}