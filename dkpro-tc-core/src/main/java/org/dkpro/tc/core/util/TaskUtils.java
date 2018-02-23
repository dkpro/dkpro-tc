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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.core.task.uima.ExtractFeaturesConnector;

/**
 * Utility methods needed in classification tasks (loading instances,
 * serialization of classifiers etc).
 */
public class TaskUtils {

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
		
		if(classificationArguments == null || classificationArguments.isEmpty()){
			throw new ResourceInitializationException(new IllegalArgumentException(
					"The classifcation arguments are empty or missing; The first element in the dimension ["
							+ Constants.DIM_CLASSIFICATION_ARGS
							+ "] has to be an instance of the machine learning adapter!"));
		}
		TcShallowLearningAdapter adapter = (TcShallowLearningAdapter) classificationArguments.get(0);
		
		return adapter;
	}
}