/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.tc.ml.uima;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.Resource;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

public class TcAnnotatorUtil {

	public static List<ExternalResourceDescription> loadExternalResourceDescriptionOfFeatures(
			String outputPath, String[] featureExtractorClassNames,
			List<Object> convertedParameters)
			throws ResourceInitializationException {

		List<ExternalResourceDescription> extractorResources = new ArrayList<ExternalResourceDescription>();
		try {
			File classFile = new File(outputPath + "/"
					+ Constants.MODEL_FEATURE_CLASS_FOLDER);
			URLClassLoader urlClassLoader = new URLClassLoader(
					new URL[] { classFile.toURI().toURL() });
			
			addToClassPath(classFile.toURI());
			
			for (String featureExtractor : featureExtractorClassNames) {

				Class<? extends Resource> resource = urlClassLoader.loadClass(
						featureExtractor).asSubclass(Resource.class);
				ExternalResourceDescription resourceDescription = createExternalResource(
						resource, convertedParameters);
				extractorResources.add(resourceDescription);

			}
			urlClassLoader.close();
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		return extractorResources;
	}

	private static void addToClassPath(URI uri) throws Exception {
		URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	    Class<URLClassLoader> urlClass = URLClassLoader.class;
	    Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
	    method.setAccessible(true);
	    method.invoke(urlClassLoader, new Object[]{uri.toURL()});
	}

	static ExternalResourceDescription createExternalResource(
			Class<? extends Resource> resource, List<Object> convertedParameters) {
		return ExternalResourceFactory.createExternalResourceDescription(
				resource, convertedParameters.toArray());
	}

	public static List<Object> convertParameters(List<Object> parameters) {
		List<Object> convertedParameters = new ArrayList<Object>();
		if (parameters != null) {
			for (Object parameter : parameters) {
				convertedParameters.add(parameter.toString());
			}
		} else {
			parameters = new ArrayList<Object>();
		}
		return convertedParameters;
	}

	
}
