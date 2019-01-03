/*******************************************************************************
 * Copyright 2019
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.core.Constants;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class PreTrainedModelProviderUnitMode extends PreTrainedModelProviderAbstract {

	/**
	 * This parameter is relevant for sequence and unit classification tasks but is
	 * not needed for document classification. The units will become the
	 * classification targets. Typically, the unit is the {@link Token} annotation
	 * but other annotations can be specified by providing the respective type name.
	 */
	public static final String PARAM_NAME_TARGET_ANNOTATION = "tcTargetAnnotation";
	@ConfigurationParameter(name = PARAM_NAME_TARGET_ANNOTATION, mandatory = true)
	private String targetName;

	protected void validateUimaParameter() {
		if (!featureMode.equals(FM_UNIT)) {
			throw new IllegalArgumentException(
					"This model loader works only for feature mode [" + FM_UNIT + "] but is [" + featureMode + "]");
		}

		boolean unitAnno = targetName != null && !targetName.isEmpty();

		if (unitAnno) {
			return;
		}
		throw new IllegalArgumentException(
				"Learning mode [" + Constants.FM_UNIT + "] requires an annotation name for [unit] (e.g. Token)");

	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		if (!JCasUtil.exists(aJCas, JCasId.class)) {
			JCasId id = new JCasId(aJCas);
			id.setId(jcasId++);
			id.addToIndexes();
		}

		processUnit(aJCas);

		if (conversionAnnotator != null && conversionAnnotator.length > 0) {
			callConversionEngine(aJCas);
		}

		if (!retainTargets) {
			removeTargets(aJCas);
		}
	}

	private void processUnit(JCas aJCas) throws AnalysisEngineProcessException {
		Type type = aJCas.getCas().getTypeSystem().getType(targetName);
		Collection<AnnotationFS> typeSelection = CasUtil.select(aJCas.getCas(), type);
		List<AnnotationFS> targetAnnotation = new ArrayList<AnnotationFS>(typeSelection);
		TextClassificationOutcome tco = null;
		List<String> outcomes = new ArrayList<String>();

		// iterate the units and set on each a prepared dummy outcome
		for (AnnotationFS target : targetAnnotation) {
			TextClassificationTarget tcs = new TextClassificationTarget(aJCas, target.getBegin(), target.getEnd());
			tcs.addToIndexes();

			tco = new TextClassificationOutcome(aJCas, target.getBegin(), target.getEnd());
			tco.setOutcome(Constants.TC_OUTCOME_DUMMY_VALUE);
			tco.addToIndexes();

			engine.process(aJCas);

			// store the outcome
			outcomes.add(tco.getOutcome());
			tcs.removeFromIndexes();
			tco.removeFromIndexes();
		}

		// iterate again to set for each unit the outcome
		for (int i = 0; i < targetAnnotation.size(); i++) {
			AnnotationFS target = targetAnnotation.get(i);
			tco = new TextClassificationOutcome(aJCas, target.getBegin(), target.getEnd());
			tco.setOutcome(outcomes.get(i));
			tco.addToIndexes();
		}

	}
 
}
