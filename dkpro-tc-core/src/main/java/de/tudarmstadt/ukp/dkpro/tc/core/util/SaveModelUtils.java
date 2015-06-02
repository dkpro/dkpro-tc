/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.core.util;

import static de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask.META_KEY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

public class SaveModelUtils implements Constants {
	public static void writeFeatureInformation(File outputFolder,
			List<String> featureSet) throws Exception {
		String featureExtractorString = StringUtils.join(featureSet, "\n");
		FileUtils.writeStringToFile(new File(outputFolder,
				MODEL_FEATURE_EXTRACTORS), featureExtractorString);
	}

	public static void writeModelParameters(TaskContext aContext,
			File aOutputFolder, List<String> aFeatureSet,
			List<Object> aFeatureParameters) throws Exception {
		// write meta collector data
		// automatically determine the required metaCollector classes from the
		// provided feature
		// extractors
		Set<Class<? extends MetaCollector>> metaCollectorClasses;
		try {
			metaCollectorClasses = TaskUtils
					.getMetaCollectorsFromFeatureExtractors(aFeatureSet);
		} catch (ClassNotFoundException e) {
			throw new ResourceInitializationException(e);
		} catch (InstantiationException e) {
			throw new ResourceInitializationException(e);
		} catch (IllegalAccessException e) {
			throw new ResourceInitializationException(e);
		}

		// collect parameter/key pairs that need to be set
		Map<String, String> metaParameterKeyPairs = new HashMap<String, String>();
		for (Class<? extends MetaCollector> metaCollectorClass : metaCollectorClasses) {
			try {
				metaParameterKeyPairs.putAll(metaCollectorClass.newInstance()
						.getParameterKeyPairs());
			} catch (InstantiationException e) {
				throw new ResourceInitializationException(e);
			} catch (IllegalAccessException e) {
				throw new ResourceInitializationException(e);
			}
		}

		Properties parameterProperties = new Properties();
		for (Entry<String, String> entry : metaParameterKeyPairs.entrySet()) {
			File file = new File(aContext.getStorageLocation(META_KEY,
					AccessMode.READWRITE), entry.getValue());

			String name = file.getName();
			String subFolder = aOutputFolder.getAbsoluteFile() + "/" + name;
			File targetFolder = new File(subFolder);
			copyToTargetLocation(file, targetFolder);
			parameterProperties.put(entry.getKey(), name);

			// should never be reached
		}

		for (int i = 0; i < aFeatureParameters.size(); i = i + 2) {

			String key = (String) aFeatureParameters.get(i).toString();
			String value = aFeatureParameters.get(i + 1).toString();

			if (valueExistAsFileOrFolderInTheFileSystem(value)) {
				String name = new File(value).getName();
				String destination = aOutputFolder + "/" + name;
				copyToTargetLocation(new File(value), new File(destination));
				parameterProperties.put(key, name);
				continue;
			}
			parameterProperties.put(key, value);
		}

		FileWriter writer = new FileWriter(new File(aOutputFolder,
				MODEL_PARAMETERS));
		parameterProperties.store(writer, "");
		writer.close();
	}

	private static boolean valueExistAsFileOrFolderInTheFileSystem(String aValue) {
		return new File(aValue).exists();
	}

	private static void copyToTargetLocation(File source, File destination)
			throws IOException {

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
			copySingleFiles(source, destination);
		}
	}

	private static void copySingleFiles(File source, File destination)
			throws IOException {
		InputStream inputstream = new FileInputStream(source);
		OutputStream outputstream = new FileOutputStream(destination);

		int length;
		byte[] buffer = new byte[1024];
		while ((length = inputstream.read(buffer)) > 0) {
			outputstream.write(buffer, 0, length);
		}

		inputstream.close();
		outputstream.close();
	}

	public static void writeModelAdapterInformation(File aOutputFolder,
			String aModelMeta) throws Exception {
		// as a marker for the type, write the name of the ml adapter class
		// write feature extractors
		FileUtils.writeStringToFile(new File(aOutputFolder, MODEL_META),
				aModelMeta);
	}

	public static void writeFeatureClassFiles(File outputFolder,
			List<String> featureSet) throws Exception {
		for (String featureString : featureSet) {
			Class<?> feature = Class.forName(featureString);
			String featureClass = featureString.replaceAll("\\.", "/")
					+ ".class";
			String location = feature.getProtectionDomain().getCodeSource()
					.getLocation().getPath()
					+ featureString.replaceAll("\\.", "/") + ".class";

			FileUtils.copyFile(new File(location),
					new File(outputFolder.getAbsolutePath() + "/"
							+ MODEL_FEATURE_CLASS_FOLDER + "/" + featureClass));
		}

	}

}
