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
package org.dkpro.tc.examples.single.document;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.io.TwentyNewsgroupsCorpusReader;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.length.NrOfTokens;
import org.dkpro.tc.features.ngram.LuceneNGram;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.report.BatchCrossValidationReport;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.weka.WekaAdapter;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;

public class MultiClassifierExperiment implements Constants {
	public static final String LANGUAGE_CODE = "en";

	public static final int NUM_FOLDS = 3;

	public static final String corpusFilePathTrain = "src/main/resources/data/twentynewsgroups/bydate-train";
	public static final String corpusFilePathTest = "src/main/resources/data/twentynewsgroups/bydate-test";

	public static void main(String[] args) throws Exception {

		DemoUtils.setDkproHome(MultiClassifierExperiment.class.getSimpleName());

		ParameterSpace pSpace = getParameterSpace();

		MultiClassifierExperiment experiment = new MultiClassifierExperiment();
		experiment.runTrainTest(pSpace);
	}

	public static ParameterSpace getParameterSpace()
			throws ResourceInitializationException {
		// configure training and test data reader dimension
		// train/test will use both, while cross-validation will only use the
		// train part
		Map<String, Object> dimReaders = new HashMap<String, Object>();

		CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
				TwentyNewsgroupsCorpusReader.class, TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION,
				corpusFilePathTrain, TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, LANGUAGE_CODE,
				TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
				Arrays.asList(TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"));
		dimReaders.put(DIM_READER_TRAIN, readerTrain);

		CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
				TwentyNewsgroupsCorpusReader.class, TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION,
				corpusFilePathTest, TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, LANGUAGE_CODE,
				TwentyNewsgroupsCorpusReader.PARAM_PATTERNS, TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt");
		dimReaders.put(DIM_READER_TEST, readerTest);

		Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
				new TcFeatureSet("DummyFeatureSet", TcFeatureFactory.create(NrOfTokens.class),
						TcFeatureFactory.create(LuceneNGram.class, LuceneNGram.PARAM_NGRAM_USE_TOP_K, 500,
								LuceneNGram.PARAM_NGRAM_MIN_N, 1, LuceneNGram.PARAM_NGRAM_MAX_N, 3)));

		@SuppressWarnings("unchecked")
		Dimension<List<Object>> dimClassArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
				Arrays.asList(new Object[] { new WekaAdapter(), SMO.class.getName(), "-C", "1.0", "-K",
                      PolyKernel.class.getName() + " " + "-C -1 -E 2" }),
				Arrays.asList(new Object[] { new WekaAdapter(), SMO.class.getName(), "-C", "100.0"}),
				Arrays.asList(new Object[] { new LibsvmAdapter() , "-s", LibsvmAdapter.PARAM_SVM_TYPE_NU_SVC_MULTI_CLASS}),
				Arrays.asList(new Object[] { new LibsvmAdapter() , "-s", LibsvmAdapter.PARAM_SVM_TYPE_C_SVC_MULTI_CLASS}),
				Arrays.asList(new Object[] { new LiblinearAdapter(), "-s", "2", "-c", "10" }),
				Arrays.asList(new Object[] { new LiblinearAdapter(), "-s", "1", "-c", "100" })
				);
		
		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
					Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
					Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), dimFeatureSets, dimClassArgs);

		return pSpace;
	}

	// ##### CV #####
	protected void runCrossValidation(ParameterSpace pSpace) throws Exception {

		ExperimentCrossValidation batch = new ExperimentCrossValidation("TwentyNewsgroupsCV", NUM_FOLDS);
		batch.setPreprocessing(getPreprocessing());
		batch.setParameterSpace(pSpace);
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
		batch.addReport(BatchCrossValidationReport.class);

		// Run
		Lab.getInstance().run(batch);
	}

	// ##### TRAIN-TEST #####
	protected void runTrainTest(ParameterSpace pSpace) throws Exception {

		ExperimentTrainTest batch = new ExperimentTrainTest("TwentyNewsgroupsTrainTest");
		batch.setPreprocessing(getPreprocessing());
		batch.setParameterSpace(pSpace);
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
		batch.addReport(BatchTrainTestReport.class);

		// Run
		Lab.getInstance().run(batch);
	}

	protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException {
		return createEngineDescription(BreakIteratorSegmenter.class);
	}
}
