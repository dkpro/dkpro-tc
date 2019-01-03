/**
 * Copyright 2019
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
package org.dkpro.tc.examples.shallow.learningCurve;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.io.FolderwiseDataReader;
import org.dkpro.tc.ml.experiment.ExperimentLearningCurve;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.report.LearningCurveReport;
import org.dkpro.tc.ml.xgboost.XgboostAdapter;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

/**
 * This test just ensures that the experiment runs without throwing any
 * exception.
 */
public class LearningCurveCrossValidationSingleLabelTest extends TestCaseSuperClass implements Constants {

	ContextMemoryReport contextReport;

	public static final String corpusFilePathTrain = "src/main/resources/data/twentynewsgroups/bydate-train";
	public static final String corpusFilePathTest = "src/main/resources/data/twentynewsgroups/bydate-test";

	@Test
	public void testJavaTrainTest() throws Exception {
		runExperimentTrainTest();
		evaluateResults(contextReport.evaluationFolder);
	}

	private void evaluateResults(File evaluationFolder) {

		assertTrue(new File(evaluationFolder, LearningCurveReport.MD5_MAPPING_FILE).exists());

		List<File> folders = new ArrayList<>();
		for (File f : evaluationFolder.listFiles()) {
			if (f.isDirectory()) {
				folders.add(f);
			}
		}
		assertEquals(2, folders.size());
		
		for(File folder : folders) {
			
			assertTrue(new File(folder, "categorical").isDirectory());
			
			long countFiles = Stream.of(folder.listFiles()).map(File::isFile).count();
			assertEquals(3, countFiles);
			
			for(File f : folder.listFiles()) {
				if(f.isHidden() || f.isDirectory()) {
					continue;
				}
				assertTrue(f.getName().endsWith(".txt") || f.getName().endsWith(".pdf"));
			}
		}

	}

	private ParameterSpace getParameterSpace() throws ResourceInitializationException {
		Map<String, Object> dimReaders = new HashMap<String, Object>();

		CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
				FolderwiseDataReader.class, FolderwiseDataReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
				FolderwiseDataReader.PARAM_LANGUAGE, "en", FolderwiseDataReader.PARAM_PATTERNS, "*/*.txt");
		dimReaders.put(DIM_READER_TRAIN, readerTrain);
		//
		CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
				FolderwiseDataReader.class, FolderwiseDataReader.PARAM_SOURCE_LOCATION, corpusFilePathTest,
				FolderwiseDataReader.PARAM_LANGUAGE, "en", FolderwiseDataReader.PARAM_PATTERNS, "*/*.txt");
		dimReaders.put(DIM_READER_TEST, readerTest);

		Map<String, Object> config3 = new HashMap<>();
		config3.put(DIM_CLASSIFICATION_ARGS, new Object[] { new LibsvmAdapter(), "-s", "1", "-c", "1000", "-t", "3" });
		config3.put(DIM_DATA_WRITER, new LibsvmAdapter().getDataWriterClass());
		config3.put(DIM_FEATURE_USE_SPARSE, new LibsvmAdapter().useSparseFeatures());

		Map<String, Object> config4 = new HashMap<>();
		config4.put(DIM_CLASSIFICATION_ARGS, new Object[] { new XgboostAdapter(), "objective=multi:softmax" });
		config4.put(DIM_DATA_WRITER, new XgboostAdapter().getDataWriterClass());
		config4.put(DIM_FEATURE_USE_SPARSE, new XgboostAdapter().useSparseFeatures());

		Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config3, config4);

		Dimension<String> dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL);
		Dimension<String> dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT);
		Dimension<TcFeatureSet> dimFeatureSet = Dimension.create(DIM_FEATURE_SET, getFeatureSet());

		ParameterSpace ps = new ParameterSpace(dimLearningMode, dimFeatureMode, dimFeatureMode, dimFeatureSet, mlas,
				Dimension.createBundle(DIM_READERS, dimReaders));

		return ps;
	}

	private static TcFeatureSet getFeatureSet() {
		return new TcFeatureSet(TcFeatureFactory.create(WordNGram.class, WordNGram.PARAM_NGRAM_USE_TOP_K, 500,
				WordNGram.PARAM_NGRAM_MIN_N, 1, WordNGram.PARAM_NGRAM_MAX_N, 3));
	}

	private void runExperimentTrainTest() throws Exception {
		contextReport = new ContextMemoryReport();

		ExperimentLearningCurve experiment = new ExperimentLearningCurve("LearningCurveSingleLabel", 2, 2);
		experiment.setPreprocessing(getPreprocessing());
		experiment.setParameterSpace(getParameterSpace());
		experiment.addReport(LearningCurveReport.class);
		experiment.addReport(contextReport);
		experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

		Lab.getInstance().run(experiment);
	}

	protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException {
		return createEngineDescription(BreakIteratorSegmenter.class);
	}
}
