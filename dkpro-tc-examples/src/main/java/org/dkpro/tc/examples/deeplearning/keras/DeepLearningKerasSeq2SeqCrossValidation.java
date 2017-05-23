/**
 * Copyright 2017
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
package org.dkpro.tc.examples.deeplearning.keras;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.examples.io.anno.SequenceOutcomeAnnotator;
import org.dkpro.tc.ml.DeepLearningExperimentCrossValidation;
import org.dkpro.tc.ml.keras.KerasAdapter;
import org.dkpro.tc.ml.report.BatchCrossValidationReport;

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;

/**
 * This a pure Java-based experiment setup of POS tagging as sequence tagging.
 */
public class DeepLearningKerasSeq2SeqCrossValidation implements Constants {
	public static final String LANGUAGE_CODE = "en";

	public static final String corpusFilePathTrain = "src/main/resources/data/brown_tei/keras";

	public static void main(String[] args) throws Exception {

		// DemoUtils.setDkproHome(DeepLearningKerasSeq2SeqPoSTestDummy.class.getSimpleName());
		System.setProperty("DKPRO_HOME", System.getProperty("user.home") + "/Desktop");

		ParameterSpace pSpace = getParameterSpace();

		DeepLearningKerasSeq2SeqCrossValidation experiment = new DeepLearningKerasSeq2SeqCrossValidation();
		experiment.runCrossValidation(pSpace);
	}

	public static ParameterSpace getParameterSpace() throws ResourceInitializationException {
		// configure training and test data reader dimension
		Map<String, Object> dimReaders = new HashMap<String, Object>();

		CollectionReaderDescription train = CollectionReaderFactory.createReaderDescription(TeiReader.class,
				TeiReader.PARAM_LANGUAGE, "en", TeiReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
				TeiReader.PARAM_PATTERNS, asList(INCLUDE_PREFIX + "a01.xml"));
		dimReaders.put(DIM_READER_TRAIN, train);

		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
				Dimension.create(DIM_LEARNING_MODE, DeepLearningConstants.LM_SEQUENCE_TO_SEQUENCE_OF_LABELS),
				Dimension.create(DeepLearningConstants.DIM_PYTHON_INSTALLATION, "/usr/local/bin/python3"),
				Dimension.create(DeepLearningConstants.DIM_MAXIMUM_LENGTH, 75), Dimension
						.create(DeepLearningConstants.DIM_USER_CODE, "src/main/resources/kerasCode/posTaggingLstm.py"));

		return pSpace;
	}

	protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException {
		return createEngineDescription(SequenceOutcomeAnnotator.class);
	}

	public void runCrossValidation(ParameterSpace pSpace) throws Exception {
		DeepLearningExperimentCrossValidation batch = new DeepLearningExperimentCrossValidation("KerasSeq2SeqCv",
				KerasAdapter.class, 2);
		batch.setParameterSpace(pSpace);
		batch.setPreprocessing(getPreprocessing());
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
		batch.addReport(BatchCrossValidationReport.class);

		Lab.getInstance().run(batch);
	}
}
