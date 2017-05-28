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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
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
import org.dkpro.tc.examples.io.TwentyNewsgroupsCorpusReader;
import org.dkpro.tc.ml.DeepLearningExperimentCrossValidation;
import org.dkpro.tc.ml.keras.KerasAdapter;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class DeepLearningKerasDocumentCrossValidation implements Constants {
	public static final String LANGUAGE_CODE = "en";

	public static final String corpusFilePath = "src/main/resources/data/twentynewsgroups/bydate-train";

	public static void main(String[] args) throws Exception {

		// DemoUtils.setDkproHome(DeepLearningTestDummy.class.getSimpleName());
		System.setProperty("DKPRO_HOME", System.getProperty("user.home") + "/Desktop");

		ParameterSpace pSpace = getParameterSpace();

		DeepLearningKerasDocumentCrossValidation experiment = new DeepLearningKerasDocumentCrossValidation();
		experiment.runCrossValidation(pSpace);
	}

	public static ParameterSpace getParameterSpace() throws ResourceInitializationException {
		// configure training and test data reader dimension
		// train/test will use both, while cross-validation will only use the
		// train part
		Map<String, Object> dimReaders = new HashMap<String, Object>();

		CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
				TwentyNewsgroupsCorpusReader.class, TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION,
				corpusFilePath, TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, LANGUAGE_CODE,
				TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
				Arrays.asList(TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"));
		dimReaders.put(DIM_READER_TRAIN, readerTrain);

		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
				Dimension.create(DIM_FEATURE_MODE, Constants.FM_DOCUMENT),
				Dimension.create(DeepLearningConstants.DIM_PYTHON_INSTALLATION, "/usr/local/bin/python3"),
				Dimension.create(DeepLearningConstants.DIM_USER_CODE,
						"src/main/resources/kerasCode/imdb_cnn_lstm.py"),
				Dimension.create(DeepLearningConstants.DIM_MAXIMUM_LENGTH, 250),
				Dimension.create(DeepLearningConstants.DIM_VECTORIZE_TO_INTEGER, true),
				Dimension.create(
						DeepLearningConstants.DIM_PRETRAINED_EMBEDDINGS, "/Users/toobee/Desktop/glove.6B.50d.txt")
				);

		return pSpace;
	}

	// ##### TRAIN-TEST #####
	protected void runCrossValidation(ParameterSpace pSpace) throws Exception {

		DeepLearningExperimentCrossValidation batch = new DeepLearningExperimentCrossValidation("KerasCrossValidation",
				KerasAdapter.class, 2);
		batch.setPreprocessing(getPreprocessing());
		batch.setParameterSpace(pSpace);
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

		// Run
		Lab.getInstance().run(batch);
	}

	protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException {
		return createEngineDescription(BreakIteratorSegmenter.class);
	}
}
