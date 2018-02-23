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
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.dkpro.tc.core.Constants;

public interface ModelVersionIO extends Constants {
	
	final static String TCVERSION = "TcVersion";
	
	default String getCurrentTcVersionFromWorkspace() throws Exception {
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
		} finally {
			IOUtils.closeQuietly(fis);
		}
		String version = model.getParent().getVersion();

		return version;
	}

	default String getCurrentTcVersionFromJar() throws Exception {
		Class<?> contextClass = getClass();

		InputStream stream = contextClass
				.getResourceAsStream("/META-INF/maven/org.dkpro.tc/dkpro-tc-core/pom.xml");

		MavenXpp3Reader reader = new MavenXpp3Reader();
		Model model;
		try {
			model = reader.read(stream);
		} finally{
			IOUtils.closeQuietly(stream);
		}
		
		String version = model.getParent().getVersion();
		return version;
	}
	
	default void writeFeatureMode(File outputFolder, String featureMode) throws Exception {
		Properties properties = new Properties();
		properties.setProperty(DIM_FEATURE_MODE, featureMode);
		File file = new File(outputFolder, MODEL_FEATURE_MODE);
		
		writeModelParameter(file, properties, "Feature mode used to train this model");
	}

	default void writeLearningMode(File outputFolder, String learningMode) throws Exception {
		Properties properties = new Properties();
		properties.setProperty(DIM_LEARNING_MODE, learningMode);
		File file = new File(outputFolder, MODEL_LEARNING_MODE);
		
		writeModelParameter(file, properties, "Learning mode used to train this model");
	}
	
	default void writeCurrentVersionOfDKProTC(File outputFolder) throws Exception {
		String version = getCurrentTcVersionFromJar();
		if (version == null) {
			version = getCurrentTcVersionFromWorkspace();
		}
		if (version != null) {
			Properties properties = new Properties();
			properties.setProperty(TCVERSION, version);
			File file = new File(outputFolder, MODEL_TC_VERSION);
			
			writeModelParameter(file, properties, "Version of DKPro TC used to train this model");
		}
	}
	
	default void writeModelParameter(File file, Properties properties, String text) throws Exception {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			properties.store(fos, text);
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}
}
