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
package org.dkpro.tc.core.task.uima;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.util.TaskUtils;

public class InstanceExtractor implements Constants {

	private String featureMode;
	private FeatureExtractorResource_ImplBase[] featureExtractors;
	private boolean addInstanceId;

	public InstanceExtractor(String featureMode, FeatureExtractorResource_ImplBase[] featureExtractors) {
		this.featureMode = featureMode;
		this.featureExtractors = featureExtractors;
		this.addInstanceId = true;
	}

	public InstanceExtractor(String featureMode, FeatureExtractorResource_ImplBase[] featureExtractors,
			boolean addInstanceId) {
		this.featureMode = featureMode;
		this.featureExtractors = featureExtractors;
		this.addInstanceId = addInstanceId;
	}

	public List<Instance> getInstances(JCas aJCas, boolean extractSparse) throws AnalysisEngineProcessException {

		List<Instance> extractedInstances = new ArrayList<>();

		try {
			if (featureMode.equals(Constants.FM_SEQUENCE)) {
				List<Instance> instances;

				instances = TaskUtils.getMultipleInstancesSequenceMode(featureExtractors, aJCas, addInstanceId,
						extractSparse);
				extractedInstances.addAll(instances);
			} else if (featureMode.equals(Constants.FM_UNIT)) {
				List<Instance> instances = TaskUtils.getMultipleInstancesUnitMode(featureExtractors, aJCas,
						addInstanceId, extractSparse);
				extractedInstances.addAll(instances);
			} else {
				Instance instance = TaskUtils.getSingleInstance(featureMode, featureExtractors, aJCas, addInstanceId,
						extractSparse);
				extractedInstances.add(instance);
			}

		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}

		return extractedInstances;
	}

}