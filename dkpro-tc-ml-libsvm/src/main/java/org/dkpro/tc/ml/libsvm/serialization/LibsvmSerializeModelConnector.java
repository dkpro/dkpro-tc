/*******************************************************************************
 * Copyright 2019
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

package org.dkpro.tc.ml.libsvm.serialization;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.serialization.LibsvmDataFormatSerializeModelConnector;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.libsvm.api._Training;

public class LibsvmSerializeModelConnector extends LibsvmDataFormatSerializeModelConnector implements Constants {

	@Override
	protected void trainModel(TaskContext aContext, File fileTrain) throws Exception {
		_Training ltm = new _Training();
		File model = new File(outputFolder, Constants.MODEL_CLASSIFIER);
		ltm.run(buildParameters(fileTrain, model));
	}

	@Override
	protected void writeAdapter() throws Exception {
		writeModelAdapterInformation(outputFolder, LibsvmAdapter.class.getName());
	}

	protected String[] buildParameters(File fileTrain, File model) {
		List<String> parameters = new ArrayList<>();
		if (classificationArguments != null) {
			for (int i = 1; i < classificationArguments.size(); i++) {
				String a = (String) classificationArguments.get(i);
				parameters.add(a);
			}
		}
		parameters.add(fileTrain.getAbsolutePath());
		parameters.add(model.getAbsolutePath());
		return parameters.toArray(new String[0]);
	}

}