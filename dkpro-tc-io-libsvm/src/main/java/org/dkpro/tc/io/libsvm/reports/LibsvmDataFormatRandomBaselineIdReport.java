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
import java.util.List;
import java.util.Random;

import org.dkpro.lab.storage.StorageService.AccessMode;

public class LibsvmDataFormatRandomBaselineIdReport extends LibsvmDataFormatOutcomeIdReport {

	private Random random = new Random(42);
	private Double upper = Double.MIN_VALUE;
	private Double lower = Double.MAX_VALUE;

	public LibsvmDataFormatRandomBaselineIdReport() {
		
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
	protected void baslinePreparation() throws IOException{
		List<String> readPredictions = readPredictions();
		determineRangeOfValues(readPredictions);
	}

	private void determineRangeOfValues(List<String> predictions) {
		
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
	
	@Override
	protected String getPrediction(String p){
		Integer r = random.nextInt(upper.intValue() - lower.intValue() + 1) + lower.intValue();
		return r.toString();
	}
	
	@Override
	protected File getTargetOutputFile() {
		File evaluationFolder = getContext().getFolder("", AccessMode.READWRITE);
		return new File(evaluationFolder, BASELINE_RANDOM_ID_OUTCOME_KEY);
	}

}