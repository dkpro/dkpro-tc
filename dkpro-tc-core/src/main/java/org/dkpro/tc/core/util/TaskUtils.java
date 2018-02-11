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
package org.dkpro.tc.core.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.internal.ReflectionUtil;
import org.apache.uima.resource.CustomResourceSpecifier;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.core.task.uima.ExtractFeaturesConnector;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Utility methods needed in classification tasks (loading instances,
 * serialization of classifiers etc).
 */
public class TaskUtils {
	/**
	 * Loads the JSON file as a system resource, parses it and returnd the
	 * JSONObject.
	 *
	 * @param path
	 *            path to the config file
	 * @return the JSONObject containing all config parameters
	 * @throws IOException
	 *             in case of an error
	 */
	public static JSONObject getConfigFromJSON(String path) throws IOException {
		String jsonPath = FileUtils.readFileToString(new File(ClassLoader.getSystemResource(path).getFile()), "utf-8");
		return (JSONObject) JSONSerializer.toJSON(jsonPath);
	}

	public static Set<String> getRequiredTypesFromFeatureExtractors(List<ExternalResourceDescription> featureSet)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Set<String> requiredTypes = new HashSet<String>();

		for (ExternalResourceDescription element : featureSet) {

			String implName;
			if (element.getResourceSpecifier() instanceof CustomResourceSpecifier) {
				implName = ((CustomResourceSpecifier) element.getResourceSpecifier()).getResourceClassName();
			} else {
				implName = element.getImplementationName();
			}

			TypeCapability annotation = ReflectionUtil.getAnnotation(Class.forName(implName), TypeCapability.class);

			if (annotation != null) {
				requiredTypes.addAll(Arrays.asList(annotation.inputs()));
			}
		}

		return requiredTypes;
	}

	public static AnalysisEngineDescription getFeatureExtractorConnector(String outputPath, String dataWriter,
			String learningMode, String featureMode, boolean useSparseFeatures, boolean addInstanceId,
			 boolean isTesting, boolean applyWeighting, List<String> filters,
			List<ExternalResourceDescription> extractorResources, String [] outcomes) throws ResourceInitializationException {
		List<Object> parameters = new ArrayList<>();
		parameters.addAll(Arrays.asList(ExtractFeaturesConnector.PARAM_ADD_INSTANCE_ID, addInstanceId,
				ExtractFeaturesConnector.PARAM_OUTPUT_DIRECTORY, outputPath,
				ExtractFeaturesConnector.PARAM_APPLY_WEIGHTING, applyWeighting,
				ExtractFeaturesConnector.PARAM_DATA_WRITER_CLASS, dataWriter,
				ExtractFeaturesConnector.PARAM_FEATURE_FILTERS, filters.toArray(new String[0]),
				ExtractFeaturesConnector.PARAM_FEATURE_MODE, featureMode,
				ExtractFeaturesConnector.PARAM_LEARNING_MODE, learningMode,
				ExtractFeaturesConnector.PARAM_IS_TESTING, isTesting,
				ExtractFeaturesConnector.PARAM_USE_SPARSE_FEATURES, useSparseFeatures,
				ExtractFeaturesConnector.PARAM_FEATURE_EXTRACTORS, extractorResources,
				ExtractFeaturesConnector.PARAM_OUTCOMES, outcomes));

		return AnalysisEngineFactory.createEngineDescription(ExtractFeaturesConnector.class,
				parameters.toArray());

	}

	public static TcShallowLearningAdapter getAdapter(List<Object> classificationArguments) throws ResourceInitializationException{
		
		if(classificationArguments == null || classificationArguments.size() < 0 ){
			throw new ResourceInitializationException(new IllegalArgumentException(
					"The classifcation arguments are empty or missing; The first element in the dimension ["
							+ Constants.DIM_CLASSIFICATION_ARGS
							+ "] has to be an instance of the machine learning adapter!"));
		}
		TcShallowLearningAdapter adapter = (TcShallowLearningAdapter) classificationArguments.get(0);
		
		return adapter;
	}
}