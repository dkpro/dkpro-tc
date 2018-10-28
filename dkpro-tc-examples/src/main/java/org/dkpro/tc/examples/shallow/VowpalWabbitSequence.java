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

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.shallow.annotators.SequenceOutcomeAnnotator;
import org.dkpro.tc.features.ngram.CharacterNGram;
import org.dkpro.tc.features.tcu.TargetSurfaceFormContextFeature;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.experiment.builder.ExperimentBuilder;
import org.dkpro.tc.ml.experiment.builder.ExperimentType;
import org.dkpro.tc.ml.vowpalwabbit.VowpalWabbitAdapter;

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;

/**
 * Example for NER as sequence classification.
 */
public class VowpalWabbitSequence
    implements Constants
{

    public static final String LANGUAGE_CODE = "de";
    public static final int NUM_FOLDS = 2;
    public static final String corpusFilePathTrain = "/Users/toobee/Desktop/mock/train";
    public static final String corpusFilePathTest = "/Users/toobee/Desktop/mock/test";

    public static File outputFolder = null;

    public static void main(String[] args) throws Exception
    {
    	System.setProperty("java.util.logging.config.file", "logging.properties");
    	System.setProperty("DKPRO_HOME", System.getProperty("user.home")+"/Desktop/");

        VowpalWabbitSequence demo = new VowpalWabbitSequence();
        demo.runTrainTest();
    }

    // ##### Train Test #####
    public void runTrainTest() throws Exception
    {
        ExperimentBuilder builder = new ExperimentBuilder();
        builder.experiment(ExperimentType.TRAIN_TEST, "trainTestExperiment")
               .dataReaderTrain(getReaderTrain())
               .dataReaderTest(getReaderTest())
               .featureSets(getFeatureSet())
               .featureMode(FeatureMode.SEQUENCE)
               .learningMode(LearningMode.SINGLE_LABEL)
               .machineLearningBackend(
                                       new MLBackend(new VowpalWabbitAdapter(), "--search_history_length", "3", "-b", "20")
                                       , new MLBackend(new VowpalWabbitAdapter(), "--search_history_length", "4", "-b", "20")
                                       , new MLBackend(new VowpalWabbitAdapter(), "--search_history_length", "5", "-b", "20")
                                       , new MLBackend(new VowpalWabbitAdapter())
                                       )
               .preprocessing(getPreprocessing())
               .run();
    }
    
    public CollectionReaderDescription getReaderTrain() throws Exception {
        return CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                TeiReader.PARAM_PATTERNS, "b*");
    }
    
    public CollectionReaderDescription getReaderTest() throws Exception {
        return CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, corpusFilePathTest,
                TeiReader.PARAM_PATTERNS, "a*");
    }
    
    public TcFeatureSet getFeatureSet()
    {
        return new TcFeatureSet(
                TcFeatureFactory.create(TargetSurfaceFormContextFeature.class,
                        TargetSurfaceFormContextFeature.PARAM_RELATIVE_TARGET_ANNOTATION_INDEX, -3),
                TcFeatureFactory.create(TargetSurfaceFormContextFeature.class,
                        TargetSurfaceFormContextFeature.PARAM_RELATIVE_TARGET_ANNOTATION_INDEX, -2),
                TcFeatureFactory.create(TargetSurfaceFormContextFeature.class,
                        TargetSurfaceFormContextFeature.PARAM_RELATIVE_TARGET_ANNOTATION_INDEX, -1),
                TcFeatureFactory.create(TargetSurfaceFormContextFeature.class,
                        TargetSurfaceFormContextFeature.PARAM_RELATIVE_TARGET_ANNOTATION_INDEX, 0),
                TcFeatureFactory.create(CharacterNGram.class, CharacterNGram.PARAM_NGRAM_USE_TOP_K, 2500));
    }

    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(SequenceOutcomeAnnotator.class);
    }

}
