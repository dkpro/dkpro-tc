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

package org.dkpro.tc.io.libsvm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.util.SaveModelUtils;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.ml.uima.TcAnnotator;

public abstract class LibsvmDataFormatLoadModelConnector extends ModelSerialization_ImplBase {

	protected static String OUTCOME_PLACEHOLDER = "-1";

	@ConfigurationParameter(name = TcAnnotator.PARAM_TC_MODEL_LOCATION, mandatory = true)
	protected File tcModelLocation;

	@ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
	protected FeatureExtractorResource_ImplBase[] featureExtractors;

	@ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true)
	protected String featureMode;

	@ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true)
	protected String learningMode;

	protected Map<String, String> integer2OutcomeMapping;
	protected Map<String, Integer> featureMapping;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		try {
			integer2OutcomeMapping = loadInteger2OutcomeMapping(tcModelLocation);
			featureMapping = loadFeature2IntegerMapping(tcModelLocation);
			SaveModelUtils.verifyTcVersion(tcModelLocation, getClass());
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}

	}

	private Map<String, Integer> loadFeature2IntegerMapping(File tcModelLocation) throws IOException {
		Map<String, Integer> map = new HashMap<>();
		List<String> readLines = FileUtils
				.readLines(new File(tcModelLocation, AdapterFormat.getFeatureNameMappingFilename()), "utf-8");
		for (String l : readLines) {
			String[] split = l.split("\t");
			map.put(split[0], Integer.valueOf(split[1]));
		}
		return map;
	}

	private Map<String, String> loadInteger2OutcomeMapping(File tcModelLocation) throws IOException {
		if (isRegression()) {
			return new HashMap<>();
		}

		Map<String, String> map = new HashMap<>();
		List<String> readLines = FileUtils
				.readLines(new File(tcModelLocation, AdapterFormat.getOutcomeMappingFilename()), "utf-8");
		for (String l : readLines) {
			String[] split = l.split("\t");
			map.put(split[1], split[0]);
		}
		return map;
	}

	private boolean isRegression() {
		return learningMode.equals(Constants.LM_REGRESSION);
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		try {
			File tempFile = createInputFile(jcas);

			File prediction = runPrediction(tempFile);

			List<TextClassificationOutcome> outcomes = getOutcomeAnnotations(jcas);
			List<String> writtenPredictions = FileUtils.readLines(prediction, "utf-8");

			checkErrorConditionNumberOfOutcomesEqualsNumberOfPredictions(outcomes, writtenPredictions);

			for (int i = 0; i < outcomes.size(); i++) {

				if (isRegression()) {
					String val = writtenPredictions.get(i);
					outcomes.get(i).setOutcome(val);
				} else {
					String val = writtenPredictions.get(i).replaceAll("\\.0", "");
					String pred = integer2OutcomeMapping.get(val);
					outcomes.get(i).setOutcome(pred);
				}

			}

		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}

	}

	private List<TextClassificationOutcome> getOutcomeAnnotations(JCas jcas) {
		return new ArrayList<>(JCasUtil.select(jcas, TextClassificationOutcome.class));
	}

	private void checkErrorConditionNumberOfOutcomesEqualsNumberOfPredictions(List<TextClassificationOutcome> outcomes,
			List<String> readLines) {
		if (outcomes.size() != readLines.size()) {
			throw new IllegalStateException(
					"Expected [" + outcomes.size() + "] predictions but were [" + readLines.size() + "]");
		}
	}

	protected abstract File runPrediction(File tempFile) throws Exception;

	private File createInputFile(JCas jcas) throws Exception {
		File tempFile = FileUtil.createTempFile("libsvm", ".txt");
		tempFile.deleteOnExit();
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "utf-8"));

		List<Instance> instances = TaskUtils.getMultipleInstancesUnitMode(featureExtractors, jcas, true, true);

		for (Instance instance : instances) {
			bw.write(OUTCOME_PLACEHOLDER);
			
			bw.write(injectSequenceId(instance));
			
			for (Feature f : instance.getFeatures()) {
				if (!sanityCheckValue(f)) {
					continue;
				}
				bw.write("\t");
				bw.write(featureMapping.get(f.getName()) + ":" + f.getValue());
			}
			bw.write("\n");
		}
		bw.close();

		return tempFile;
	}

	protected String injectSequenceId(Instance instance) {
		
		//SvmHmm extension nothing to do here in the normal case
		
		return "";
	}

	private boolean sanityCheckValue(Feature f) {
		if (f.getValue() instanceof Number) {
			return true;
		}
		if (f.getName().equals(Constants.ID_FEATURE_NAME)) {
			return false;
		}

		try {
			Double.valueOf((String) f.getValue());
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Feature [" + f.getName() + "] has a non-numeric value [" + f.getValue() + "]", e);
		}
		return false;
	}

}