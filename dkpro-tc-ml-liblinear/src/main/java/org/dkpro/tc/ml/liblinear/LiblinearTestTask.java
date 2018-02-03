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
package org.dkpro.tc.ml.liblinear;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatTestTask;
import org.dkpro.tc.ml.liblinear.util.LiblinearUtils;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class LiblinearTestTask extends LibsvmDataFormatTestTask implements Constants {

	public static final String SEPARATOR_CHAR = ";";
	public static final double EPISILON_DEFAULT = 0.01;
	public static final double PARAM_C_DEFAULT = 1.0;

	@Override
	public void execute(TaskContext aContext) throws Exception {
		throwExceptionIfMultiLabelMode();

		runPrediction(aContext);
	}
	
	@Override
	protected void runPrediction(TaskContext aContext) throws Exception {

		File fileTrain = getTrainFile(aContext);
		File fileTest = getTestFile(aContext);
		
		File predFolder = aContext.getFolder("", AccessMode.READWRITE);
		File predictionsFile = new File(predFolder, Constants.FILENAME_PREDICTIONS);
		
		// default for bias is -1, documentation says to set it to 1 in order to
		// get results closer
		// to libsvm
		// writer adds bias, so if we de-activate that here for some reason, we
		// need to also
		// deactivate it there
		Problem train = Problem.readFromFile(fileTrain, 1.0);
		SolverType solver = LiblinearUtils.getSolver(classificationArguments);
		double C = LiblinearUtils.getParameterC(classificationArguments);
		double eps = LiblinearUtils.getParameterEpsilon(classificationArguments);
		Linear.setDebugOutput(null);
		Parameter parameter = new Parameter(solver, C, eps);
		Model model = Linear.train(train, parameter);
		Problem test = Problem.readFromFile(fileTest, 1.0);

		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(predictionsFile), "utf-8"));
		writer.append("#PREDICTION;GOLD" + "\n");

		Feature[][] testInstances = test.x;
		for (int i = 0; i < testInstances.length; i++) {
			Feature[] instance = testInstances[i];
			Double prediction = Linear.predict(model, instance);

			writer.write(prediction + SEPARATOR_CHAR + new Double(test.y[i]));
			writer.write("\n");
		}

		writer.close();
	}

}