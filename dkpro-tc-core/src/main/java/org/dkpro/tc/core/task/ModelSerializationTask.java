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
package org.dkpro.tc.core.task;

import static org.dkpro.tc.core.task.MetaInfoTask.META_KEY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.uima.fit.factory.ConfigurationParameterFactory;
import org.apache.uima.resource.CustomResourceSpecifier;
import org.apache.uima.resource.ExternalResourceDescription;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.features.meta.MetaDependent;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.util.SaveModelUtils;

public abstract class ModelSerializationTask extends ExecutableTaskBase implements Constants {
	@Discriminator(name = DIM_FEATURE_SET)
	protected TcFeatureSet featureSet;
	@Discriminator(name = DIM_FEATURE_MODE)
	protected String featureMode;
	@Discriminator(name = DIM_LEARNING_MODE)
	protected String learningMode;
	@Discriminator(name = DIM_BIPARTITION_THRESHOLD)
	protected String threshold;

	protected File outputFolder;

	public final static String TCVERSION = "TcVersion";

	public void setOutputFolder(File outputFolder) {
		this.outputFolder = outputFolder;
		this.outputFolder.mkdirs();
	}

	protected void writeModelConfiguration(TaskContext aContext) throws Exception {

		writeModelParameters(aContext, outputFolder, featureSet);
		writeFeatureMode(outputFolder, featureMode);
		writeLearningMode(outputFolder, learningMode);
		writeCurrentVersionOfDKProTC(outputFolder);

		writeAdapter();
	}

	private void writeFeatureMode(File outputFolder, String featureMode) throws IOException {
		Properties properties = new Properties();
		properties.setProperty(DIM_FEATURE_MODE, featureMode);

		File file = new File(outputFolder + "/" + MODEL_FEATURE_MODE);
		FileOutputStream fileOut = new FileOutputStream(file);
		properties.store(fileOut, "Feature mode used to train this model");
		fileOut.close();

	}

	private static void writeLearningMode(File outputFolder, String learningMode) throws IOException {
		Properties properties = new Properties();
		properties.setProperty(DIM_LEARNING_MODE, learningMode);

		File file = new File(outputFolder + "/" + MODEL_LEARNING_MODE);
		FileOutputStream fileOut = new FileOutputStream(file);
		properties.store(fileOut, "Learning mode used to train this model");
		fileOut.close();
	}

	private void writeCurrentVersionOfDKProTC(File outputFolder) throws Exception {
		String version = getCurrentTcVersionFromJar();
		if (version == null) {
			version = getCurrentTcVersionFromWorkspace();
		}
		if (version != null) {
			Properties properties = new Properties();
			properties.setProperty(TCVERSION, version);

			File file = new File(outputFolder + "/" + MODEL_TC_VERSION);
			FileOutputStream fileOut = new FileOutputStream(file);
			properties.store(fileOut, "Version of DKPro TC used to train this model");
			fileOut.close();
		}

	}

	private String getCurrentTcVersionFromJar() {
		Class<?> contextClass = SaveModelUtils.class;

		InputStream resourceAsStream = contextClass
				.getResourceAsStream("/META-INF/maven/org.dkpro.tc/dkpro-tc-core/pom.xml");

		MavenXpp3Reader reader = new MavenXpp3Reader();
		Model model;
		try {
			model = reader.read(resourceAsStream);
		} catch (Exception e) {
			return null;
		}
		String version = model.getParent().getVersion();
		return version;
	}

	private String getCurrentTcVersionFromWorkspace() throws Exception {
		Class<?> contextClass = SaveModelUtils.class;

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
		model = reader.read(new FileInputStream(pomFile));
		String version = model.getParent().getVersion();

		return version;
	}

	private void writeModelParameters(TaskContext aContext, File aOutputFolder, List<TcFeature> featureSet)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		for (TcFeature f : featureSet) {
			copyLuceneMetaResourcesAndGetOverrides(aContext, f, aOutputFolder);
			persistsFeatureClassObject(aContext, f, aOutputFolder);

			sb = copyParameters(aContext, f, sb, aOutputFolder);
		}
		writeFeatureParameters(sb, aOutputFolder);

	}

	private void writeFeatureParameters(StringBuilder sb, File aOutputFolder) throws IOException {
		File file = new File(aOutputFolder, MODEL_FEATURE_EXTRACTOR_CONFIGURATION);
		FileUtils.writeStringToFile(file, sb.toString(), "utf-8");
	}

	private StringBuilder copyParameters(TaskContext aContext, TcFeature f, StringBuilder sb, File aOutputFolder)
			throws IOException {
		sb.append(f.getFeatureName() + "\t");

		ExternalResourceDescription feDesc = f.getActualValue();
		Map<String, Object> parameterSettings = ConfigurationParameterFactory
				.getParameterSettings(feDesc.getResourceSpecifier());
		List<String> keySet = new ArrayList<>(parameterSettings.keySet());
		for (int i = 0; i < keySet.size(); i++) {

			String key = keySet.get(i);
			String value = parameterSettings.get(key).toString();

			if (valueExistAsFileOrFolderInTheFileSystem(value)) {
				String name = new File(value).getName();
				String destination = aOutputFolder + "/" + name;
				copyToTargetLocation(new File(value), new File(destination));
				sb = record(i, keySet, name, sb);
				continue;
			}
			sb = record(i, keySet, parameterSettings, sb);
		}

		sb.append("\n");

		return sb;
	}

	private static boolean valueExistAsFileOrFolderInTheFileSystem(String aValue) {
		return new File(aValue).exists();
	}

	private StringBuilder record(int i, List<String> keySet, String name, StringBuilder sb) {
		String key = keySet.get(i);
		sb.append(key + "=" + name);
		if (i + 1 < keySet.size()) {
			sb.append("\t");
		}
		return sb;
	}

	private StringBuilder record(int i, List<String> keySet, Map<String, Object> parameterSettings, StringBuilder sb) {
		String key = keySet.get(i);
		sb.append(key + "=" + parameterSettings.get(key).toString());
		if (i + 1 < keySet.size()) {
			sb.append("\t");
		}
		return sb;
	}

	private void persistsFeatureClassObject(TaskContext aContext, TcFeature f, File aOutputFolder) throws Exception {
		ExternalResourceDescription feDesc = f.getActualValue();

		String implName;
		if (feDesc.getResourceSpecifier() instanceof CustomResourceSpecifier) {
			implName = ((CustomResourceSpecifier) feDesc.getResourceSpecifier()).getResourceClassName();
		} else {
			implName = feDesc.getImplementationName();
		}

		Class<?> feature = Class.forName(implName);

		InputStream inStream = feature.getResource("/" + implName.replace(".", "/") + ".class").openStream();

		OutputStream outStream = buildOutputStream(aOutputFolder, implName);

		IOUtils.copy(inStream, outStream);
		outStream.close();
		inStream.close();
	}

	private static OutputStream buildOutputStream(File modelFolder, String featureString) throws Exception {

		String packagePath = featureString.substring(0, featureString.lastIndexOf(".")).replaceAll("\\.", "/");
		String featureClassName = featureString.substring(featureString.lastIndexOf(".") + 1) + ".class";

		String folderPath = modelFolder.getAbsolutePath() + "/" + MODEL_FEATURE_CLASS_FOLDER + "/" + packagePath + "/";
		new File(folderPath).mkdirs();
		return new FileOutputStream(new File(folderPath + featureClassName));
	}

	private void copyLuceneMetaResourcesAndGetOverrides(TaskContext aContext, TcFeature f, File aOutputFolder)
			throws Exception {
		ExternalResourceDescription feDesc = f.getActualValue();
		Map<String, Object> parameterSettings = ConfigurationParameterFactory
				.getParameterSettings(feDesc.getResourceSpecifier());

		String implName;
		if (feDesc.getResourceSpecifier() instanceof CustomResourceSpecifier) {
			implName = ((CustomResourceSpecifier) feDesc.getResourceSpecifier()).getResourceClassName();
		} else {
			implName = feDesc.getImplementationName();
		}

		Class<?> feClass = Class.forName(implName);

		// Skip feature extractors that are not dependent on meta collectors
		if (!MetaDependent.class.isAssignableFrom(feClass)) {
			return;
		}

		MetaDependent feInstance = (MetaDependent) feClass.newInstance();

		Map<String, Object> metaOverrides = new HashMap<>();
		Map<String, Object> extractorOverrides = new HashMap<>();

		// Tell the meta collectors where to store their data
		for (MetaCollectorConfiguration conf : feInstance.getMetaCollectorClasses(parameterSettings)) {
			Map<String, String> collectorOverrides = conf.collectorOverrides;
			metaOverrides.putAll(collectorOverrides);
			extractorOverrides.putAll(conf.extractorOverrides);

			for (Entry<String, String> entry : collectorOverrides.entrySet()) {
				File file = new File(aContext.getFolder(META_KEY, AccessMode.READWRITE), entry.getValue().toString());

				String name = file.getName();
				String subFolder = aOutputFolder.getAbsoluteFile() + "/" + name;
				File targetFolder = new File(subFolder);
				copyToTargetLocation(file, targetFolder);
			}
		}
		writeOverrides(aOutputFolder, metaOverrides, META_COLLECTOR_OVERRIDE);
		writeOverrides(aOutputFolder, extractorOverrides, META_EXTRACTOR_OVERRIDE);
	}

	private static void writeOverrides(File aOutputFolder, Map<String, Object> override, String target)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		for (String k : override.keySet()) {
			sb.append(k + "=" + override.get(k));
		}

		FileUtils.write(new File(aOutputFolder, target), sb.toString(), "utf-8");
	}

	private void copyToTargetLocation(File source, File destination) throws IOException {

		if (source.isDirectory()) {
			if (!destination.exists()) {
				destination.mkdir();
			}

			for (String file : source.list()) {
				File src = new File(source, file);
				File dest = new File(destination, file);
				copyToTargetLocation(src, dest);
			}

		} else {
			copySingleFile(source, destination);
		}
	}

	private void copySingleFile(File source, File destination) throws IOException {
		InputStream inputstream = new FileInputStream(source);
		OutputStream outputstream = new FileOutputStream(destination);
		IOUtils.copy(inputstream, outputstream);
		inputstream.close();
		outputstream.close();
	}

	protected void writeModelAdapterInformation(File aOutputFolder, String aModelMeta) throws Exception {
		// as a marker for the type, write the name of the ml adapter class
		// write feature extractors
		FileUtils.writeStringToFile(new File(aOutputFolder, MODEL_META), aModelMeta, "utf-8");
	}

	protected abstract void writeAdapter() throws Exception;

}