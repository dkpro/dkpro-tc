/**
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
 */
package org.dkpro.tc.ml.svmhmm.task.serialization;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatSerializeModelConnector;
import org.dkpro.tc.ml.svmhmm.SvmHmmAdapter;
import org.dkpro.tc.ml.svmhmm.task.SvmHmmTestTask;
import org.dkpro.tc.ml.svmhmm.util.SvmHmmUtils;

public class SvmhmmSerializeModelConnector
    extends LibsvmDataFormatSerializeModelConnector
    implements Constants
{

	@Override
	protected void writeAdapter() throws Exception {
		writeModelAdapterInformation(outputFolder, SvmHmmAdapter.class.getName());
	}

	@Override
	protected void trainModel(File fileTrain) throws Exception {

		List<String> stringArgs = new ArrayList<>();
		for (int i = 1; i < classificationArguments.size(); i++) {
			stringArgs.add((String) classificationArguments.get(i));
		}

		double paramC = SvmHmmUtils.getParameterC(stringArgs);
		double paramEpsilon = SvmHmmUtils.getParameterEpsilon(stringArgs);
		int paramOrderE = SvmHmmUtils.getParameterOrderE_dependencyOfEmissions(stringArgs);
		int paramOrderT = SvmHmmUtils.getParameterOrderT_dependencyOfTransitions(stringArgs);
		int paramB = SvmHmmUtils.getParameterBeamWidth(stringArgs);

		File model = new File(outputFolder, Constants.MODEL_CLASSIFIER);
		
		File trainBinary = SvmHmmTestTask.resolveSvmHmmLearnCommand();
		
		File newTrainFileLocation = new File(trainBinary.getParentFile(), fileTrain.getName());
		FileUtils.copyFile(fileTrain, newTrainFileLocation);
		
		File tmpModelLocation = new File(trainBinary.getParentFile(), "model.tmp");
		
		List<String> buildTrainCommand = SvmHmmTestTask.buildTrainCommand(trainBinary, newTrainFileLocation, tmpModelLocation, paramC, paramOrderE, paramOrderT, paramEpsilon, paramB);
		SvmHmmTestTask.runCommand(buildTrainCommand);
		
		FileUtils.copyFile(tmpModelLocation, model);
		
		FileUtils.deleteQuietly(tmpModelLocation);
		FileUtils.deleteQuietly(newTrainFileLocation);
		
	}

}
