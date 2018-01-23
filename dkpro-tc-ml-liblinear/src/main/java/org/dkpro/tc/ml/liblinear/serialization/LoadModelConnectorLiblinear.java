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

package org.dkpro.tc.ml.liblinear.serialization;

import static org.dkpro.tc.core.Constants.MODEL_CLASSIFIER;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.core.util.SaveModelUtils;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.uima.TcAnnotator;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Problem;

public class LoadModelConnectorLiblinear extends ModelSerialization_ImplBase {

	@ConfigurationParameter(name = TcAnnotator.PARAM_TC_MODEL_LOCATION, mandatory = true)
	private File tcModelLocation;

	@ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
	protected FeatureExtractorResource_ImplBase[] featureExtractors;

	@ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true)
	private String featureMode;

	@ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true)
	private String learningMode;

	private Model liblinearModel;
	private Map<Integer, String> outcomeMapping;

	private Map<String, Integer> featureMapping;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		try {
			liblinearModel = Linear.loadModel(new File(tcModelLocation, MODEL_CLASSIFIER));
			outcomeMapping = loadOutcome2IntegerMapping(tcModelLocation);
			featureMapping = loadFeature2IntegerMapping(tcModelLocation);
			SaveModelUtils.verifyTcVersion(tcModelLocation, getClass());
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}

	}


    private Map<String, Integer> loadFeature2IntegerMapping(File tcModelLocation) throws IOException {
		Map<String, Integer> map = new HashMap<>();
		List<String> readLines = FileUtils
				.readLines(new File(tcModelLocation, LiblinearAdapter.getFeatureNameMappingFilename()), "utf-8");
		for (String l : readLines) {
			String[] split = l.split("\t");
			map.put(split[0],Integer.valueOf(split[1]));
		}
		return map;
	}

	private Map<Integer, String> loadOutcome2IntegerMapping(File tcModelLocation) throws IOException {
		Map<Integer, String> map = new HashMap<>();
		List<String> readLines = FileUtils
				.readLines(new File(tcModelLocation, LiblinearAdapter.getOutcomeMappingFilename()), "utf-8");
		for (String l : readLines) {
			String[] split = l.split("\t");
			map.put(Integer.valueOf(split[1]), split[0]);
		}
		return map;
	}
	
	 private Double toValue(Object value)
	    {
	        double v;
	        if (value instanceof Number) {
	            v = ((Number) value).doubleValue();
	        }
	        else {
	            v = 1.0;
	        }

	        return v;
	    }
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		try {
			List<Instance> instances = TaskUtils.getMultipleInstancesUnitMode(featureExtractors, jcas, true,
					new LiblinearAdapter().useSparseFeatures());
			
			StringBuilder sb = new StringBuilder();
			  for (Instance inst : instances) {
		            Map<Integer, Double> entry = new HashMap<>();
		            for (org.dkpro.tc.api.features.Feature f : inst.getFeatures()) {
		                Integer id = featureMapping.get(f.getName());
		                Double val = toValue(f.getValue());

		                if (Math.abs(val) < 0.00000001) {
		                    // skip zero values
		                    continue;
		                }

		                entry.put(id, val);
		            }
		            List<Integer> keys = new ArrayList<Integer>(entry.keySet());
		            Collections.sort(keys);
		            
		            sb.append("-1\t"); // dummy label
		            
		            sb.append("1:1.0\t"); //bias entry
		            
		            for (int i = 0; i < keys.size(); i++) {
		                Integer key = keys.get(i);
		                Double value = entry.get(key);
		                sb.append("" + key.toString() + ":" + value.toString());
		                if (i + 1 < keys.size()) {
		                    sb.append("\t");
		                }
		            }
		            sb.append("\n");
			}

			File inputData = File.createTempFile("libLinearePrediction",
					LiblinearAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.featureVectorsFile));
			inputData.deleteOnExit();
			FileUtils.writeStringToFile(inputData, sb.toString(), "utf-8");

			Problem predictionProblem = Problem.readFromFile(inputData, 1.0);

			List<TextClassificationOutcome> outcomes = new ArrayList<>(
					JCasUtil.select(jcas, TextClassificationOutcome.class));
			Feature[][] testInstances = predictionProblem.x;
			for (int i = 0; i < testInstances.length; i++) {
				Feature[] instance = testInstances[i];
				Double prediction = Linear.predict(liblinearModel, instance);

				if (learningMode.equals(Constants.LM_REGRESSION)) {
					outcomes.get(i).setOutcome(prediction.toString());
				} else {
					String predictedLabel = outcomeMapping.get(prediction.intValue());
					outcomes.get(i).setOutcome(predictedLabel);
				}
			}

		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}

	}

}