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
package org.dkpro.tc.examples.shallow;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.shallow.filter.UniformClassDistributionFilter;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.features.ngram.CharacterNGram;
import org.dkpro.tc.io.FolderwiseDataReader;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.experiment.builder.ExperimentBuilder;
import org.dkpro.tc.ml.experiment.builder.ExperimentType;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.dkpro.tc.ml.xgboost.XgboostAdapter;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;

public class DocumentDemo
    implements Constants
{
    public static final String LANGUAGE_CODE = "en";

    public static final int NUM_FOLDS = 2;

    public static final String corpusFilePathTrain = "src/main/resources/data/twentynewsgroups/bydate-train";
    public static final String corpusFilePathTest = "src/main/resources/data/twentynewsgroups/bydate-test";

    public static void main(String[] args) throws Exception
    {

    	System.setProperty("java.util.logging.config.file", "logging.properties");
        DemoUtils.setDkproHome(DocumentDemo.class.getSimpleName());

        DocumentDemo experiment = new DocumentDemo();
        experiment.runTrainTest();
        experiment.runCrossValidation();
    }

    public CollectionReaderDescription getReaderTrain() throws Exception
    {
        return CollectionReaderFactory.createReaderDescription(FolderwiseDataReader.class,
                FolderwiseDataReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                FolderwiseDataReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                FolderwiseDataReader.PARAM_PATTERNS, "*/*.txt");
    }

    public CollectionReaderDescription getReaderTest() throws Exception
    {
        return CollectionReaderFactory.createReaderDescription(FolderwiseDataReader.class,
                FolderwiseDataReader.PARAM_SOURCE_LOCATION, corpusFilePathTest,
                FolderwiseDataReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                FolderwiseDataReader.PARAM_PATTERNS, "*/*.txt");
    }

    private static TcFeatureSet getFeatureSet()
    {
        return new TcFeatureSet("DummyFeatureSet",
                TcFeatureFactory.create(TokenRatioPerDocument.class),
                TcFeatureFactory.create(CharacterNGram.class, CharacterNGram.PARAM_NGRAM_USE_TOP_K, 500,
                		CharacterNGram.PARAM_NGRAM_MIN_N, 1, CharacterNGram.PARAM_NGRAM_MAX_N, 3));
    }

    public void runCrossValidation() throws Exception
    {

        ExperimentBuilder builder = new ExperimentBuilder();
        builder.experiment(ExperimentType.CROSS_VALIDATION, "crossValidation")
                .numFolds(NUM_FOLDS)
                .dataReaderTrain(getReaderTrain())
                .preprocessing(getPreprocessing())
                .featureSets(getFeatureSet())
                .learningMode(LearningMode.SINGLE_LABEL)
                .featureMode(FeatureMode.DOCUMENT)
                .machineLearningBackend(
                        new MLBackend(new XgboostAdapter(), "objective=multi:softmax"),
                        new MLBackend(new WekaAdapter(), SMO.class.getName(), "-C", "1.0", "-K",
                                PolyKernel.class.getName() + " " + "-C -1 -E 2"),
                        new MLBackend(new LiblinearAdapter(), "-s", "4", "-c", "100"),
                        new MLBackend(new LibsvmAdapter(), "-s", "1", "-c", "1000", "-t", "3"))
                .run();

    }

    public void runTrainTest() throws Exception
    {
        ExperimentBuilder builder = new ExperimentBuilder();
        builder.experiment(ExperimentType.TRAIN_TEST, "trainTest")
                .dataReaderTrain(getReaderTrain())
                .dataReaderTest(getReaderTest())
                .preprocessing(getPreprocessing())
                .featureSets(getFeatureSet())
                .featureFilter(UniformClassDistributionFilter.class.getName())
                .learningMode(LearningMode.SINGLE_LABEL)
                .featureMode(FeatureMode.DOCUMENT)
                .machineLearningBackend(
                        new MLBackend(new WekaAdapter(), SMO.class.getName(), "-C", "1.0", "-K",
                                PolyKernel.class.getName() + " " + "-C -1 -E 2"),
                        new MLBackend(new LibsvmAdapter(), "-s", "1", "-c", "1000", "-t", "3")
                )
                .run();
    }
    
    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }
}
