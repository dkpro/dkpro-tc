/**
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka.task.serialization;

import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.FM_UNIT;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_CLASSIFIER;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_CLASS_LABELS;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_FEATURE_NAMES;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationFocus;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.core.util.SaveModelUtils;
import de.tudarmstadt.ukp.dkpro.tc.ml.uima.TcAnnotator;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

public class LoadModelConnectorWeka extends ModelSerialization_ImplBase {

	@ConfigurationParameter(name = TcAnnotator.PARAM_TC_MODEL_LOCATION, mandatory = true)
	private File tcModelLocation;

	public static final String PARAM_BIPARTITION_THRESHOLD = "bipartitionThreshold";
	@ConfigurationParameter(name = PARAM_BIPARTITION_THRESHOLD, mandatory = true, defaultValue = "0.5")
	private String bipartitionThreshold;

	@ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
	protected FeatureExtractorResource_ImplBase[] featureExtractors;

	@ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true)
	private String learningMode;

	@ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true)
	private String featureMode;

	private Classifier cls;
	private List<Attribute> attributes;
	private List<String> classLabels;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);

		try {
			loadClassifier();
			loadAttributes();
			loadClassLabels();

			SaveModelUtils.verifyTcVersion(tcModelLocation, getClass());
			SaveModelUtils.writeFeatureMode(tcModelLocation, featureMode);
			SaveModelUtils.writeLearningMode(tcModelLocation, learningMode);
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	private void loadClassLabels() throws IOException {
		classLabels = new ArrayList<>();
		for (String classLabel : FileUtils.readLines(new File(tcModelLocation,
				MODEL_CLASS_LABELS))) {
			classLabels.add(classLabel);
		}
	}

	private void loadAttributes() throws IOException {
		attributes = new ArrayList<>();
		for (String attributeName : FileUtils.readLines(new File(
				tcModelLocation, MODEL_FEATURE_NAMES))) {
			attributes.add(new Attribute(attributeName));
		}
	}

	private void loadClassifier() throws Exception {
		cls = (Classifier) weka.core.SerializationHelper.read(new File(
				tcModelLocation, MODEL_CLASSIFIER).getAbsolutePath());
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		try {
			Instance instance = de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils
					.getSingleInstance(featureMode, featureExtractors, jcas,
							false, false);
			
			boolean isMultiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);
			boolean isRegression = learningMode.equals(Constants.LM_REGRESSION);
			
			if(!isMultiLabel){
				weka.core.Instance wekaInstance = WekaUtils
					.tcInstanceToWekaInstance(instance, attributes,
							classLabels, isRegression);

				String val = classLabels.get((int) cls
					.classifyInstance(wekaInstance));

				TextClassificationOutcome outcome = null;
				if (!FM_UNIT.equals(featureMode)) {
					outcome = JCasUtil.selectSingle(jcas,
							TextClassificationOutcome.class);
				} else {
					outcome = getOutcomeForFocus(jcas);
				}
				outcome.setOutcome(val);
			}
			else{
				weka.core.Instance mekaInstance = WekaUtils
						.tcInstanceToMekaInstance(instance, attributes, classLabels);
				double[] vals = cls.distributionForInstance(mekaInstance);
				List<String> outcomes = new ArrayList<String>();
                for (int i = 0; i < vals.length; i++) {
                    if (vals[i] >= Double.valueOf(bipartitionThreshold)) {
                        String label = mekaInstance.attribute(i).name()
                        		.split(WekaDataWriter.CLASS_ATTRIBUTE_PREFIX)[1];
                        outcomes.add(label);
                    }
                }	

				TextClassificationOutcome outcome = null;
				TextClassificationFocus focus = null;
				if (!FM_UNIT.equals(featureMode)) {
					outcome = JCasUtil.selectSingle(jcas, TextClassificationOutcome.class);
				} else {
					outcome = getOutcomeForFocus(jcas);
					focus = JCasUtil.selectSingle(jcas, TextClassificationFocus.class);
				}
				if(outcomes.size() > 0){
					outcome.setOutcome(outcomes.get(0));
					outcome.addToIndexes();
				}
				if(outcomes.size() > 1){
					// add more outcome annotations
					try {
						for(int i = 1; i < outcomes.size(); i++){
							TextClassificationOutcome newOutcome = new TextClassificationOutcome(jcas);
							if(focus != null){
								newOutcome.setBegin(focus.getBegin());
								newOutcome.setEnd(focus.getEnd());
							}
							newOutcome.setOutcome(outcomes.get(i));
							newOutcome.addToIndexes();
						}
					} catch (Exception ex) {
						String msg = "Error while trying to retrieve TC focus from CAS. Details: "
								+ ex.getMessage();
						Logger.getLogger(getClass()).error(msg, ex);
						throw new RuntimeException(msg, ex);
					}
				}
			}
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(new IllegalStateException(
						e.getMessage()));
		}		
	}

	private TextClassificationOutcome getOutcomeForFocus(JCas jcas) {
		TextClassificationOutcome outcome = null;
		TextClassificationFocus focus = null;

		try {
			focus = JCasUtil.selectSingle(jcas, TextClassificationFocus.class);
		} catch (Exception ex) {
			String msg = "Error while trying to retrieve TC focus from CAS. Details: "
					+ ex.getMessage();
			Logger.getLogger(getClass()).error(msg, ex);
			throw new RuntimeException(msg, ex);
		}

		Iterator<TextClassificationOutcome> outcomeIterator = JCasUtil
				.iterator(focus, TextClassificationOutcome.class,
						false /* ambiguous */, false /* strict */);

		if (!outcomeIterator.hasNext()) {
			throw new IllegalStateException(
					"There should be exactly one TC outcome covered by the TC focus from "
							+ focus.getBegin() + " to " + focus.getEnd());
		}

		outcome = outcomeIterator.next();

		if (outcomeIterator.hasNext()) {
			throw new IllegalStateException(
					"There should be exactly one TC outcome covered by the TC focus from "
							+ focus.getBegin() + " to " + focus.getEnd());
		}

		return outcome;
	}
}