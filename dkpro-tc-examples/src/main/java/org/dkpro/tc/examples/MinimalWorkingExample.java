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
package org.dkpro.tc.examples;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.features.ngram.AvgTokenRatioPerDocument;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.io.FolderwiseDataReader;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.weka.WekaAdapter;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import weka.classifiers.functions.SMO;

public class MinimalWorkingExample implements Constants {
	private static final String CORPUS_FILEPATH_TRAIN = "src/main/resources/data/twentynewsgroups/bydate-train";
	private static final String CORPUS_FILEPATH_TEST = "src/main/resources/data/twentynewsgroups/bydate-test";
	private static final String LANGUAGE_CODE = "en";
	private static final String EXPERIMENT_NAME = "MinimalWorkingExample";

	/**
	 * Starts the experiment.
	 */
	public static void main(String[] args) throws Exception {
		
		
		//This sets the output folder to which the results are written, pre-set to write the results to the Desktop.
		System.setProperty("DKPRO_HOME", System.getProperty("user.home") + "/Desktop");

		CollectionReaderDescription readerTrain = getReader(CORPUS_FILEPATH_TRAIN);
		CollectionReaderDescription readerTest = getReader(CORPUS_FILEPATH_TEST);
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(DIM_READER_TRAIN, readerTrain);
		dimReaders.put(DIM_READER_TEST, readerTest);

		// We want to use Weka as classifier, using a Support Vector Machine (SMO in Weka) 
		@SuppressWarnings("unchecked")
		Dimension<List<Object>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
				asList(new Object[] { new WekaAdapter(), SMO.class.getName() }));

		//Get the features
		Dimension<TcFeatureSet> dimFeatureSets = getFeatureSet();
		
		//Wire feature space with information
		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
				Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL), Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT),
				dimFeatureSets, dimClassificationArgs);

		//Build experiment
		ExperimentTrainTest batch = new ExperimentTrainTest(EXPERIMENT_NAME);
		batch.setPreprocessing(getPreprocessing());
		batch.setParameterSpace(pSpace);
		batch.addReport(BatchTrainTestReport.class); //This report generates an overview-excel file with all results

		// This executes the experiment
		Lab.getInstance().run(batch);
	}

	private static CollectionReaderDescription getReader(String path) throws ResourceInitializationException {
		return CollectionReaderFactory.createReaderDescription(
				FolderwiseDataReader.class, FolderwiseDataReader.PARAM_SOURCE_LOCATION, path,
				FolderwiseDataReader.PARAM_LANGUAGE, LANGUAGE_CODE, FolderwiseDataReader.PARAM_PATTERNS, "*/*.txt");
	}

	private static Dimension<TcFeatureSet> getFeatureSet() {

		//We use the 50 most frequent word-ngrams, this is a low-threshold for demo purposes in actual setups you probably want to increase this value
		TcFeature wordNgramFeature = TcFeatureFactory.create(WordNGram.class, 
														    WordNGram.PARAM_NGRAM_USE_TOP_K, 50,
														    	WordNGram.PARAM_NGRAM_MIN_N, 1, 
														    	WordNGram.PARAM_NGRAM_MAX_N, 3);
		TcFeature avgTokenRatio = TcFeatureFactory.create(AvgTokenRatioPerDocument.class);
		
		//Combine features in a feature set
		TcFeatureSet featureSet = new TcFeatureSet(wordNgramFeature, avgTokenRatio);

		//put the feature set in a dimension
		return Dimension.create(DIM_FEATURE_SET, featureSet);

	}

	private static AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException {
		return createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class,
				BreakIteratorSegmenter.PARAM_LANGUAGE, LANGUAGE_CODE));
	}
}
