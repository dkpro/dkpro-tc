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

package org.dkpro.tc.ml.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.compress.utils.IOUtils;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;

public class DeepLearningRandomBaseline2OutcomeReport extends DeepLearningId2OutcomeReport
		implements DeepLearningConstants {

	private Random random = new Random(42);
	private List<String> labelPool = new ArrayList<>();
	
	@Override
	public void execute() throws Exception {
		init();

		if (isRegression) {
			return;
		}

		super.execute();
	}

	@Override
	protected void baselinePreparation() throws Exception {
		
		File folder = getContext().getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY);
		File file = new File(folder, FILENAME_OUTCOME_VECTOR);
		buildPool(file);
	}

	private void buildPool(File file) throws Exception {

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] split = line.split(" ");
				for (String v : split) {
					if (!labelPool.contains(v)) {
						labelPool.add(v);
					}
				}
			}
		} finally {
			IOUtils.closeQuietly(reader);
		}
		
		Collections.shuffle(labelPool);
	}
	
	@Override
	protected File getTargetFile() {
		return getContext().getFile(Constants.BASELINE_RANDOM_ID_OUTCOME_KEY, AccessMode.READWRITE);
	}
	
	@Override
	protected List<String> update(List<String> predictions) {
		
		List<String> out = new ArrayList<>();
		
		for (String p : predictions) {
			if(p.isEmpty()){
				continue;
			}
			String[] split = p.split("\t");
			Integer idx = random.nextInt(labelPool.size());
			out.add(split[0] + "\t" + labelPool.get(idx));
		}
		
		return out;
	}

}