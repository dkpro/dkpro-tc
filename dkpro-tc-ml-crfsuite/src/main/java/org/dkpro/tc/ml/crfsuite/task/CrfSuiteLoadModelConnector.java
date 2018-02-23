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

package org.dkpro.tc.ml.crfsuite.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.core.feature.InstanceIdFeature;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.ml.crfsuite.writer.CrfSuiteFeatureFormatExtractionIterator;
import org.dkpro.tc.ml.uima.TcAnnotator;

public class CrfSuiteLoadModelConnector extends ModelSerialization_ImplBase {

	@ConfigurationParameter(name = TcAnnotator.PARAM_TC_MODEL_LOCATION, mandatory = true)
	private File tcModelLocation;

	@ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
	protected FeatureExtractorResource_ImplBase[] featureExtractors;

	private File model = null;

	private File executablePath;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		try {
			executablePath = CrfSuiteTestTask.getExecutable();
			model = new File(tcModelLocation, MODEL_CLASSIFIER);
			verifyTcVersion(tcModelLocation, getClass());
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}

	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		try {
			int sequenceId = 0;
			List<Instance> instance = new ArrayList<>();
			for (TextClassificationSequence seq : JCasUtil.select(jcas, TextClassificationSequence.class)) {

				instance.addAll(getInstancesInSequence(featureExtractors, jcas, seq, true, sequenceId++));
			}

			CrfSuiteFeatureFormatExtractionIterator iterator = new CrfSuiteFeatureFormatExtractionIterator(instance);

			// takes N sequences and classifies them - all results are hold in
			// memory
			StringBuilder output = new StringBuilder();
			while (iterator.hasNext()) {

				StringBuilder buffer = new StringBuilder();
				int limit = 5000;
				int idx = 0;
				while (iterator.hasNext()) {
					StringBuilder seqInfo = iterator.next();
					buffer.append(seqInfo);
					idx++;
					if (idx == limit) {
						break;
					}
				}

				List<String> command = buildCommand();
				StringBuilder out = runCommand(command, buffer.toString());
				output.append(out);
			}

			setPredictedOutcome(jcas, output.toString());
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}

	}

	private StringBuilder runCommand(List<String> command, String buffer) throws IOException {
		ProcessBuilder pb = new ProcessBuilder();
		pb.redirectError(Redirect.INHERIT);
		pb.command(command);
		Process process = pb.start();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "utf-8"));
		writer.write(buffer.toString());
		writer.close();
		return CrfSuiteTestTask.captureProcessOutput(process);
	}

	private List<String> buildCommand() throws Exception {
		List<String> command = new ArrayList<String>();
		command.add(executablePath.getAbsolutePath());
		command.add("tag");
		command.add("-m");
		command.add(model.getAbsolutePath());
		command.add("-"); // Read from STDIN

		return command;
	}

	private void setPredictedOutcome(JCas jcas, String aLabels) {
		List<TextClassificationOutcome> outcomes = new ArrayList<TextClassificationOutcome>(
				JCasUtil.select(jcas, TextClassificationOutcome.class));
		String[] labels = aLabels.split("\n");

		for (int i = 0, labelIdx = 0; i < outcomes.size(); i++) {
			if (labels[labelIdx].isEmpty()) {
				// empty lines mark end of sequence
				// shift label index +1 to begin of next sequence
				labelIdx++;
			}
			TextClassificationOutcome o = outcomes.get(i);
			o.setOutcome(labels[labelIdx++]);
		}

	}

	private List<Instance> getInstancesInSequence(FeatureExtractorResource_ImplBase[] featureExtractors, JCas jcas,
			TextClassificationSequence sequence, boolean addInstanceId, int sequenceId) throws Exception {

		List<Instance> instances = new ArrayList<Instance>();
		int jcasId = JCasUtil.selectSingle(jcas, JCasId.class).getId();
		List<TextClassificationTarget> seqTargets = JCasUtil.selectCovered(jcas, TextClassificationTarget.class,
				sequence);
		
		for (TextClassificationTarget aTarget : seqTargets) {

			Instance instance = new Instance();
			if (addInstanceId) {
				instance.addFeature(InstanceIdFeature.retrieve(jcas, aTarget, sequenceId));
			}

			// execute feature extractors and add features to instance
			try {
				for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
					instance.addFeatures(((FeatureExtractor) featExt).extract(jcas, aTarget));
				}
			} catch (TextClassificationException e) {
				throw new AnalysisEngineProcessException(e);
			}

			// set and write outcome label(s)
			instance.setOutcomes(getOutcomes(jcas, aTarget));
			instance.setJcasId(jcasId);
			instance.setSequenceId(sequenceId);
			instance.setSequencePosition(aTarget.getId());

			instances.add(instance);
		}

		return instances;
	}

	private List<String> getOutcomes(JCas jcas, AnnotationFS unit) throws TextClassificationException {
		Collection<TextClassificationOutcome> outcomes;
		if (unit == null) {
			outcomes = JCasUtil.select(jcas, TextClassificationOutcome.class);
		} else {
			outcomes = JCasUtil.selectCovered(jcas, TextClassificationOutcome.class, unit);
		}

		if (outcomes.size() == 0) {
			throw new TextClassificationException("No outcome annotations present in current CAS.");
		}

		List<String> stringOutcomes = new ArrayList<String>();
		for (TextClassificationOutcome outcome : outcomes) {
			stringOutcomes.add(outcome.getOutcome());
		}
		return stringOutcomes;
	}
}