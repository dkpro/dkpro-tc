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
package org.dkpro.tc.ml.model;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class PreTrainedModelProviderDocumentMode extends PreTrainedModelProviderAbstract {

	/**
	 * This parameters ensures that the needed DKPro TC annotation is created. In
	 * case of document mode, a single {@link TextClassificationTarget} is created
	 * spanning over the entire document text.
	 */
	public static final String PARAM_ADD_TC_BACKEND_ANNOTATION = "addTcBackendAnnotation";
	@ConfigurationParameter(name = PARAM_ADD_TC_BACKEND_ANNOTATION, mandatory = false, defaultValue = "false")
	private boolean addAnnotation;

	protected void validateUimaParameter() {
		if (featureMode.equals(FM_DOCUMENT)) {
			return;
		}
		throw new IllegalArgumentException(
				"This model loader works only for feature mode [" + FM_DOCUMENT + "] but is [" + featureMode + "]");
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		if (!JCasUtil.exists(aJCas, JCasId.class)) {
			JCasId id = new JCasId(aJCas);
			id.setId(jcasId++);
			id.addToIndexes();
		}

		processDocument(aJCas);

		if (conversionAnnotator != null && conversionAnnotator.length > 0) {
			callConversionEngine(aJCas);
		}

		if (!retainTargets) {
			removeTargets(aJCas);
		}
	}

	protected void processDocument(JCas aJCas) throws AnalysisEngineProcessException {

		if (addAnnotation) {
			TextClassificationTarget aTarget = new TextClassificationTarget(aJCas, 0, aJCas.getDocumentText().length());
			aTarget.addToIndexes();

			TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas);
			outcome.setOutcome("");
			outcome.addToIndexes();
		}

		// create new UIMA annotator in order to separate the parameter spaces
		// this annotator will get initialized with its own set of parameters
		// loaded from the model
		try {
			engine.process(aJCas);
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

}
