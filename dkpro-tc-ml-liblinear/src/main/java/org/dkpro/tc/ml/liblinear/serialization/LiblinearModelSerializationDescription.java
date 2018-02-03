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

import java.io.File;

import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.util.SaveModelUtils;
import org.dkpro.tc.io.libsvm.LibsvmModelSerialization;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.liblinear.util.LiblinearUtils;

import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class LiblinearModelSerializationDescription extends LibsvmModelSerialization implements Constants {

	boolean trainModel = true;

	public void trainModel(boolean b) {
		trainModel = b;
	}

	@Override
	protected void trainModel(File fileTrain) throws Exception {
		SolverType solver = LiblinearUtils.getSolver(classificationArguments);
		double C = LiblinearUtils.getParameterC(classificationArguments);
		double eps = LiblinearUtils.getParameterEpsilon(classificationArguments);

		Linear.setDebugOutput(null);

		Parameter parameter = new Parameter(solver, C, eps);
		Problem train = Problem.readFromFile(fileTrain, 1.0);
		Model model = Linear.train(train, parameter);
		model.save(new File(outputFolder, MODEL_CLASSIFIER));
	}

	@Override
	protected void writeAdapter() throws Exception {
		SaveModelUtils.writeModelAdapterInformation(outputFolder, LiblinearAdapter.class.getName());
	}
}