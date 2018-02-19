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
package org.dkpro.tc.core.ml;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.core.task.uima.ConnectorConstants;

public abstract class ModelSerialization_ImplBase extends JCasAnnotator_ImplBase implements ConnectorConstants, Constants, ModelVersionIO {

	public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";
	@ConfigurationParameter(name = PARAM_OUTPUT_DIRECTORY, mandatory = true)
	protected File outputDirectory;

	protected TcShallowLearningAdapter initMachineLearningAdapter(File tcModelLocation) throws Exception {
		File modelMeta = new File(tcModelLocation, MODEL_META);
		String fileContent = FileUtils.readFileToString(modelMeta, "utf-8");
		Class<?> classObj = Class.forName(fileContent);
		return (TcShallowLearningAdapter) classObj.newInstance();
	}

	protected void verifyTcVersion(File tcModelLocation, Class<? extends ModelSerialization_ImplBase> class1)
			throws Exception {
		String loadedVersion = loadTcVersionFromModel(tcModelLocation);
		String currentVersion = getCurrentTcVersionFromJar();

		if (currentVersion == null) {
			currentVersion = getCurrentTcVersionFromWorkspace();
		}

		if (loadedVersion.equals(currentVersion)) {
			return;
		}
		Logger.getLogger(class1).warn(
				"The model was created under version [" + loadedVersion + "], you are using [" + currentVersion + "]");
	}

	protected String loadTcVersionFromModel(File modelFolder) throws Exception {
		File file = new File(modelFolder, MODEL_TC_VERSION);
		Properties prop = new Properties();

		FileInputStream fos = null;
		
		try {
			fos = new FileInputStream(file);
			prop.load(fos);
		} finally {
			IOUtils.closeQuietly(fos);
		}

		return prop.getProperty(ModelSerializationTask.TCVERSION);
	}

}
