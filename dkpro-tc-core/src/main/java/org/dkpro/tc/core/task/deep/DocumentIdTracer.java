/*******************************************************************************
 * Copyright 2017
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
package org.dkpro.tc.core.task.deep;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.core.DeepLearningConstants;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * This annotator creates a mapping of the <b>order</b> of the
 * TextClassificationOutcomes to an index. Its purpose is to be able later on -
 * after prediction - to restore which instance has which prediction result. The
 * identity of the target is, thus, determined by the processing order
 */
public class DocumentIdTracer extends JCasAnnotator_ImplBase {
	public static final String PARAM_TARGET_DIRECTORY = "targetDirectory";
	@ConfigurationParameter(name = PARAM_TARGET_DIRECTORY, mandatory = true)
	protected File targetFolder;

	public static final String PARAM_SEQUENCE_ANNOTATION = "sequenceAnnotation";
	@ConfigurationParameter(name = PARAM_SEQUENCE_ANNOTATION, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence")
	protected String sequenceSpanTypeName;

	public static final String PARAM_LEARNING_MODE = "learningMode";
	@ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true)
	protected String learningMode;

	public static final String PARAM_USER_SET_MAXIMUM_LENGTH = "maxLen";
	@ConfigurationParameter(name = PARAM_USER_SET_MAXIMUM_LENGTH, mandatory = true)
	protected Integer maximumLength;

	BufferedWriter writer;
	private Type sequenceSpanType;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		try {
			File f = new File(targetFolder, DeepLearningConstants.FILENAME_TARGET_ID_TO_INDEX);
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "utf-8"));

			writer.write(
					"# processing sequence of the respective document / target - prediction output should conform to this order enabling determination which document/item was classified as X"
							+ System.lineSeparator());

			JCas typeFactory = JCasFactory.createJCas();
			Type type = JCasUtil.getType(typeFactory, Class.forName(sequenceSpanTypeName));
			AnnotationFS sequenceAnno = typeFactory.getCas().createAnnotation(type, 0, 0);
			sequenceSpanType = sequenceAnno.getType();

		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}

	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		switch (learningMode) {
		case DeepLearningConstants.LM_DOCUMENT_TO_LABEL:
			processDocumentMode(aJCas);
			return;
		case DeepLearningConstants.LM_SEQUENCE_TO_SEQUENCE_OF_LABELS:
			processSequenceMode(aJCas);
			return;
		}
	}

	private void processSequenceMode(JCas aJCas) throws AnalysisEngineProcessException {
		int jcasId = JCasUtil.selectSingle(aJCas, JCasId.class).getId();

		try {
			int seqId = 0;
			Collection<AnnotationFS> sequences = CasUtil.select(aJCas.getCas(), sequenceSpanType);
			for (AnnotationFS s : sequences) {

				Collection<TextClassificationTarget> targets = JCasUtil.selectCovered(aJCas,
						TextClassificationTarget.class, s);

				int i = 0;
				for (TextClassificationTarget tco : targets) {
					// This formatted identification will allow sorting the
					// information in sequence. This
					// leads to a human readable id2outcome report
					String identification = String.format("%06d_%06d_%06d", jcasId, seqId, i);
					writer.write(identification + "\t" + tco.getCoveredText() + System.lineSeparator());

					if (maximumLength != null && i + 1 >= maximumLength) {
						break;
					}
					i++;
				}
				writer.write("\n");
				seqId++;
			}
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private void processDocumentMode(JCas aJCas) throws AnalysisEngineProcessException {
		int jcasId = JCasUtil.selectSingle(aJCas, JCasId.class).getId();
		try {
			String documentFile = DocumentMetaData.get(aJCas).getDocumentUri();
			writer.write(jcasId + "\t" + documentFile + System.lineSeparator());
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	@Override
	public void collectionProcessComplete() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}
	}

}
