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
package org.dkpro.tc.ml.vowpalwabbit.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dkpro.tc.ml.base.TcTrainer;

public class VowpalWabbitTrainer extends VowpalWabbit implements TcTrainer {

	public VowpalWabbitTrainer() {
		//
	}

	@Override
	public void train(File aData, File aModel, List<String> parameters) throws Exception {
		File binary = getExecutable();
		List<String> cmd = getTrainCommand(parameters, binary, aData, aModel);
		executeTrainingCommand(cmd);
	}

	public static List<String> getTrainCommand(List<String> parameters, File aBinary, File aData, File aModel)
			throws Exception {
		List<String> trainCommand = new ArrayList<>();
		trainCommand.addAll(minimalTrainingArguments(parameters, aData));
		trainCommand.addAll(Arrays.asList(new String[] { "--final_regressor", aModel.getAbsolutePath() }));
		trainCommand.addAll(Arrays.asList(new String[] { "--data", aData.getAbsolutePath() }))	;

		return assembleCommand(aBinary, trainCommand.toArray(new String[0]));
	}

	public static void executeTrainingCommand(List<String> aCommand) throws Exception {
		Process process = new ProcessBuilder().inheritIO().command(aCommand).start();
		process.waitFor();
	}

}
