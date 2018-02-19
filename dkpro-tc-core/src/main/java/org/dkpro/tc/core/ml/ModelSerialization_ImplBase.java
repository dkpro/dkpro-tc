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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.core.task.uima.ConnectorConstants;

public abstract class ModelSerialization_ImplBase extends JCasAnnotator_ImplBase implements ConnectorConstants, Constants {

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

	protected String getCurrentTcVersionFromWorkspace() throws Exception {
		Class<?> contextClass = getClass();

		// Try to determine the location of the POM file belonging to the
		// context object
		URL url = contextClass.getResource(contextClass.getSimpleName() + ".class");
		String classPart = contextClass.getName().replace(".", "/") + ".class";
		String base = url.toString();
		base = base.substring(0, base.length() - classPart.length());
		base = base.substring(0, base.length() - "target/classes/".length());
		File pomFile = new File(new File(URI.create(base)), "pom.xml");

		MavenXpp3Reader reader = new MavenXpp3Reader();
		Model model;

		FileInputStream fis = null;
		try{
			fis = new FileInputStream(pomFile);
			model = reader.read(fis);
		}
		finally {
			IOUtils.closeQuietly(fis);
		}
		String version = model.getParent().getVersion();

		return version;
	}

	protected String getCurrentTcVersionFromJar() throws Exception {
		Class<?> contextClass = getClass();

		InputStream is = null;
		Model model;
		try {
			is = contextClass.getResourceAsStream("/META-INF/maven/org.dkpro.tc/dkpro-tc-core/pom.xml");
			MavenXpp3Reader reader = new MavenXpp3Reader();
			model = reader.read(is);
		} finally {
			IOUtils.closeQuietly(is);
		}
		String version = model.getParent().getVersion();
		return version;
	}

	protected String loadTcVersionFromModel(File modelFolder) throws Exception {
		File file = new File(modelFolder, MODEL_TC_VERSION);
		Properties prop = new Properties();

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			prop.load(fis);
		} finally {
			IOUtils.closeQuietly(fis);
		}

		return prop.getProperty(ModelSerializationTask.TCVERSION);
	}

	protected void writeFeatureMode(File outputFolder, String featureMode) throws IOException {
		Properties properties = new Properties();
		properties.setProperty(DIM_FEATURE_MODE, featureMode);

		File file = new File(outputFolder + "/" + MODEL_FEATURE_MODE);
		FileOutputStream fileOut = null;
		
		try {
			fileOut = new FileOutputStream(file);
			properties.store(fileOut, "Feature mode used to train this model");
		} finally {
			IOUtils.closeQuietly(fileOut);
		}

	}

	protected void writeLearningMode(File outputFolder, String learningMode) throws IOException {
		Properties properties = new Properties();
		properties.setProperty(DIM_LEARNING_MODE, learningMode);

		File file = new File(outputFolder + "/" + MODEL_LEARNING_MODE);
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(file);
			properties.store(fos, "Learning mode used to train this model");
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}
}
