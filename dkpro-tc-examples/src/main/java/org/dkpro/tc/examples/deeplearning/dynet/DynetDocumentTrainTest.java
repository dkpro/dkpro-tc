/**
 * Copyright 2018
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
package org.dkpro.tc.examples.deeplearning.dynet;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.ml.DeepLearningExperimentTrainTest;
import org.dkpro.tc.ml.dynet.DynetAdapter;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

/**
 * This a pure Java-based experiment setup of POS tagging as sequence tagging.
 */
public class DynetDocumentTrainTest implements Constants {
	public static final String LANGUAGE_CODE = "en";

	public static final String corpusFilePathTrain = "src/main/resources/data/langId/train";
	public static final String corpusFilePathTest = "src/main/resources/data/langId/test";
	
	public static void main(String[] args) throws Exception {

		// This is used to ensure that the required DKPRO_HOME environment
		// variable is set.
		// Ensures that people can run the experiments even if they haven't read
		// the setup
		// instructions first :)
		// DemoUtils.setDkproHome(DeepLearningKerasSeq2SeqPoSTestDummy.class.getSimpleName());
		System.setProperty("DKPRO_HOME", System.getProperty("user.home") + "/Desktop");

		ParameterSpace pSpace = getParameterSpace("/usr/local/bin/python3");

		DynetDocumentTrainTest experiment = new DynetDocumentTrainTest();
		experiment.runTrainTest(pSpace);
	}

	public static ParameterSpace getParameterSpace(String python3) throws ResourceInitializationException {
		// configure training and test data reader dimension
		Map<String, Object> dimReaders = new HashMap<String, Object>();

		CollectionReaderDescription train = CollectionReaderFactory.createReaderDescription(LinewiseLangIdReader.class,
				LinewiseLangIdReader.PARAM_LANGUAGE, "en", LinewiseLangIdReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
				LinewiseLangIdReader.PARAM_PATTERNS, "*.txt");
		dimReaders.put(DIM_READER_TRAIN, train);

		// Careful - we need at least 2 sequences in the testing file otherwise
		// things will crash
		CollectionReaderDescription test = CollectionReaderFactory.createReaderDescription(LinewiseLangIdReader.class,
				LinewiseLangIdReader.PARAM_LANGUAGE, "en", LinewiseLangIdReader.PARAM_SOURCE_LOCATION, corpusFilePathTest,
				LinewiseLangIdReader.PARAM_PATTERNS, "*.txt");
		dimReaders.put(DIM_READER_TEST, test);

		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
				Dimension.create(DIM_FEATURE_MODE, Constants.FM_DOCUMENT),
				Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
				Dimension.create(DeepLearningConstants.DIM_PYTHON_INSTALLATION, python3),
				Dimension.create(DeepLearningConstants.DIM_RAM_WORKING_MEMORY, "4096"),
				Dimension.create(DeepLearningConstants.DIM_VECTORIZE_TO_INTEGER, true), 
				Dimension
						.create(DeepLearningConstants.DIM_USER_CODE, "src/main/resources/dynetCode/dynetLangId.py"));

		return pSpace;
	}

	public void runTrainTest(ParameterSpace pSpace) throws Exception {
		DeepLearningExperimentTrainTest batch = new DeepLearningExperimentTrainTest("DynetDocument", DynetAdapter.class);
		batch.setParameterSpace(pSpace);
		batch.setPreprocessing(AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class));
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
		batch.addReport(ContextMemoryReport.class);
		Lab.getInstance().run(batch);
	}
}
