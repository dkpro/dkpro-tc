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

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.core.Constants;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class PreTrainedModelProviderSequenceMode extends PreTrainedModelProviderAbstract {

	/**
	 * This parameters ensures that the needed DKPro TC annotation are created that
	 * specify what is considered a sequence and the target in the sequence. If this
	 * is set to true (default is false) then
	 * {@link #PARAM_NAME_SEQUENCE_ANNOTATION} (e.g. {@link Sentence}) and
	 * {@link #PARAM_NAME_TARGET_ANNOTATION} (e.g. {@link Token}) has to be provided
	 * additionally.
	 */
	public static final String PARAM_ADD_TC_BACKEND_ANNOTATION = "addTcBackendAnnotation";
	@ConfigurationParameter(name = PARAM_ADD_TC_BACKEND_ANNOTATION, mandatory = false, defaultValue = "false")
	private boolean addAnnotation;

	/**
	 * This name of this annotation marks the span which will be considered as
	 * consecutive sequence, which is annotated internally as
	 * {@link TextClassificationSequence}. The span of this annotation is expected
	 * to contain two or more annotations of the annotation provided as
	 * {@link #PARAM_NAME_TARGET_ANNOTATION}. Typically, a sequence is a
	 * {@link Sentence} annotation with {@link Token} as classification target. The
	 * name of the annotation has to be provided as fully qualified named i.e.
	 * ANNOTATION.class.getName()
	 */
	public static final String PARAM_NAME_SEQUENCE_ANNOTATION = "tcSequenceAnnotation";
	@ConfigurationParameter(name = PARAM_NAME_SEQUENCE_ANNOTATION, mandatory = false)
	private String sequenceName;

	/**
	 * This parameter provides the annotation name that will be annotated as
	 * {@link TextClassificationTarget}, which is typically {@link Token}. The name
	 * of the annotation has to be provided as fully qualified named i.e.
	 * ANNOTATION.class.getName()
	 */
	public static final String PARAM_NAME_TARGET_ANNOTATION = "tcTargetAnnotation";
	@ConfigurationParameter(name = PARAM_NAME_TARGET_ANNOTATION, mandatory = false)
	private String targetName;

	protected void validateUimaParameter() {

		if (!featureMode.equals(FM_SEQUENCE)) {
			throw new IllegalArgumentException(
					"This model loader works only for feature mode [" + FM_SEQUENCE + "] but is [" + featureMode + "]");
		}

		boolean seqAnno = sequenceName != null && !sequenceName.isEmpty();
		boolean targetAnno = targetName != null && !targetName.isEmpty();

		if (seqAnno && targetAnno) {
			return;
		}
		throw new IllegalArgumentException("Feature mode [" + Constants.FM_SEQUENCE
				+ "] requires an annotation name for [sequence] (e.g. Sentence) and [target] (e.g. Token), which was not provided");
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		if (!JCasUtil.exists(aJCas, JCasId.class)) {
			JCasId id = new JCasId(aJCas);
			id.setId(jcasId++);
			id.addToIndexes();
		}

		annotateBackendAnnotation(aJCas);

		engine.process(aJCas);

		if (conversionAnnotator != null && conversionAnnotator.length > 0) {
			callConversionEngine(aJCas);
		}

		if (!retainTargets) {
			removeTargets(aJCas);
		}
	}

	private void annotateBackendAnnotation(JCas aJCas) {
		if (addAnnotation) {
			addTCSequenceAnnotation(aJCas);
			addTcTargetAndOutcomeAnnotation(aJCas);
		}
	}

	private void addTcTargetAndOutcomeAnnotation(JCas aJCas) {
		Type type = aJCas.getCas().getTypeSystem().getType(targetName);

		Collection<AnnotationFS> unitAnnotation = CasUtil.select(aJCas.getCas(), type);
		for (AnnotationFS unit : unitAnnotation) {
			TextClassificationTarget tcs = new TextClassificationTarget(aJCas, unit.getBegin(), unit.getEnd());
			tcs.addToIndexes();
			TextClassificationOutcome tco = new TextClassificationOutcome(aJCas, unit.getBegin(), unit.getEnd());
			tco.setOutcome(Constants.TC_OUTCOME_DUMMY_VALUE);
			tco.addToIndexes();
		}
	}

	private void addTCSequenceAnnotation(JCas jcas) {
		Type type = jcas.getCas().getTypeSystem().getType(sequenceName);

		Collection<AnnotationFS> sequenceAnnotation = CasUtil.select(jcas.getCas(), type);
		for (AnnotationFS seq : sequenceAnnotation) {
			TextClassificationSequence tcs = new TextClassificationSequence(jcas, seq.getBegin(), seq.getEnd());
			tcs.addToIndexes();
		}
	}

}
