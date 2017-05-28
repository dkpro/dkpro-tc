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
package org.dkpro.tc.examples.deeplearning.dl4j.seq;

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
import org.dkpro.tc.examples.crossvalidation.WekaManualFoldCrossValidation;
import org.dkpro.tc.examples.io.anno.SequenceOutcomeAnnotator;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.ml.DeepLearningExperimentTrainTest;
import org.dkpro.tc.ml.deeplearning4j.Deeplearning4jAdapter;

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;

/**
 * This a pure Java-based experiment setup of POS tagging as sequence tagging.
 */
public class DeepLearningDl4jSeq2SeqTrainTest implements Constants {
	public static final String LANGUAGE_CODE = "en";

    public static final String corpusFilePathTrain = "src/main/resources/data/brown_tei/keras";
    public static final String corpusFilePathTest = "src/main/resources/data/brown_tei/keras";
	
	public static void main(String[] args) throws Exception {

		// This is used to ensure that the required DKPRO_HOME environment
		// variable is set.
		// Ensures that people can run the experiments even if they haven't read
		// the setup
		// instructions first :)
		// DemoUtils.setDkproHome(DeepLearningKerasSeq2SeqPoSTestDummy.class.getSimpleName());
		System.setProperty("DKPRO_HOME", System.getProperty("user.home") + "/Desktop");

		ParameterSpace pSpace = getParameterSpace();

		DeepLearningDl4jSeq2SeqTrainTest experiment = new DeepLearningDl4jSeq2SeqTrainTest();
		experiment.runTrainTest(pSpace);
	}

	public static ParameterSpace getParameterSpace() throws ResourceInitializationException {
		// configure training and test data reader dimension
		Map<String, Object> dimReaders = new HashMap<String, Object>();

		CollectionReaderDescription train = CollectionReaderFactory.createReaderDescription(TeiReader.class,
				TeiReader.PARAM_LANGUAGE, "en", TeiReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
				TeiReader.PARAM_PATTERNS, "*.xml");
		dimReaders.put(DIM_READER_TRAIN, train);

		CollectionReaderDescription test = CollectionReaderFactory.createReaderDescription(TeiReader.class,
				TeiReader.PARAM_LANGUAGE, "en", TeiReader.PARAM_SOURCE_LOCATION, corpusFilePathTest,
				TeiReader.PARAM_PATTERNS, "*.xml");
		dimReaders.put(DIM_READER_TEST, test);

		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
				Dimension.create(DIM_FEATURE_MODE, Constants.FM_SEQUENCE),
				Dimension.create(DeepLearningConstants.DIM_PRETRAINED_EMBEDDINGS,
						"/Users/toobee/Desktop/glove.6B.50d.txt"),
				Dimension.create(DeepLearningConstants.DIM_VECTORIZE_TO_INTEGER, false), 
				Dimension
						.create(DeepLearningConstants.DIM_USER_CODE, new Dl4jSeq2SeqUserCode()));

		return pSpace;
	}

	protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException {
		return createEngineDescription(SequenceOutcomeAnnotator.class);
	}

	public void runTrainTest(ParameterSpace pSpace) throws Exception {
		
		DemoUtils.setDkproHome(DeepLearningDl4jSeq2SeqTrainTest.class.getSimpleName());
		
		DeepLearningExperimentTrainTest batch = new DeepLearningExperimentTrainTest("dl4jSeq2Seq", Deeplearning4jAdapter.class);
		batch.setParameterSpace(pSpace);
		batch.setPreprocessing(getPreprocessing());
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

		Lab.getInstance().run(batch);
	}
}
