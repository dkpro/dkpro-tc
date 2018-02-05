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

package org.dkpro.tc.ml.liblinear.serialization;

import static org.dkpro.tc.core.Constants.MODEL_CLASSIFIER;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatLoadModelConnector;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Problem;

public class LiblinearLoadModelConnector extends LibsvmDataFormatLoadModelConnector {

	private Model liblinearModel;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		try {
			liblinearModel = Linear.loadModel(new File(tcModelLocation, MODEL_CLASSIFIER));
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	protected File runPrediction(File infile) throws Exception {
		
		Problem predictionProblem = Problem.readFromFile(infile, 1.0);
		
		File tmp = File.createTempFile("libLinearePrediction",".txt");
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmp), "utf-8"));
		Feature[][] testInstances = predictionProblem.x;
		for (int i = 0; i < testInstances.length; i++) {
			Feature[] instance = testInstances[i];
			Double prediction = Linear.predict(liblinearModel, instance);
			writer.write(prediction.toString() + "\n");
		}
		
		writer.close();
		
		tmp.deleteOnExit();
		return tmp;
	}

}