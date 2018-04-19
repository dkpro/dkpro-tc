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
package org.dkpro.tc.examples.shallow.crfsuite.sequence;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.shallow.util.anno.SequenceOutcomeAnnotator;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.maxnormalization.TokenLengthRatio;
import org.dkpro.tc.features.ngram.CharacterNGram;
import org.dkpro.tc.ml.builder.ExperimentBuilderV2;
import org.dkpro.tc.ml.builder.ExperimentType;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.crfsuite.CrfSuiteAdapter;

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;

/**
 * This a pure Java-based experiment setup of POS tagging as sequence tagging.
 */
public class CRFSuiteBrownPosDemo
    implements Constants
{
    public static final String LANGUAGE_CODE = "en";

    public static final String corpusFilePathTrain = "src/main/resources/data/brown_tei/";

    public static void main(String[] args) throws Exception
    {

        // This is used to ensure that the required DKPRO_HOME environment
        // variable is set.
        // Ensures that people can run the experiments even if they haven't read
        // the setup
        // instructions first :)
        DemoUtils.setDkproHome(CRFSuiteBrownPosDemo.class.getSimpleName());

        CRFSuiteBrownPosDemo experiment = new CRFSuiteBrownPosDemo();
        experiment.runTrainTest();
        experiment.runCrossValidation();
    }
    
    public static CollectionReaderDescription getTrainReader() throws ResourceInitializationException {
        return CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "en", TeiReader.PARAM_SOURCE_LOCATION,
                corpusFilePathTrain, TeiReader.PARAM_PATTERNS, "a01.xml");
    }
    
    public static CollectionReaderDescription getTestReader() throws ResourceInitializationException {
        return CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "en", TeiReader.PARAM_SOURCE_LOCATION,
                corpusFilePathTrain, TeiReader.PARAM_PATTERNS, "a02.xml");
    }
    
    public static TcFeatureSet getFeatureSet() {
        return new TcFeatureSet(
                TcFeatureFactory.create(TokenLengthRatio.class),
                TcFeatureFactory.create(CharacterNGram.class, CharacterNGram.PARAM_NGRAM_MIN_N, 2,
                        CharacterNGram.PARAM_NGRAM_MAX_N, 4, CharacterNGram.PARAM_NGRAM_USE_TOP_K,
                        50));
    }

    // ##### CV #####
    public void runCrossValidation() throws Exception
    {
        ExperimentBuilderV2 builder = new ExperimentBuilderV2();
        builder.experiment(ExperimentType.CROSS_VALIDATION, "crossValidationExperiment")
        .dataReaderTrain(getTrainReader())
        .numFolds(2)
        .experimentPreprocessing(AnalysisEngineFactory.createEngineDescription(SequenceOutcomeAnnotator.class))
        .experimentReports(new ContextMemoryReport())
        .featureSets(getFeatureSet())
        .learningMode(LearningMode.SINGLE_LABEL)
        .featureMode(FeatureMode.SEQUENCE)
        .machineLearningBackend(new MLBackend(new CrfSuiteAdapter(), CrfSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR))
        .run();
    }

    public void runTrainTest() throws Exception
    {
        ExperimentBuilderV2 builder = new ExperimentBuilderV2();
        builder.experiment(ExperimentType.TRAIN_TEST, "trainTestExperiment")
        .dataReaderTrain(getTrainReader())
        .dataReaderTest(getTestReader())
        .experimentPreprocessing(AnalysisEngineFactory.createEngineDescription(SequenceOutcomeAnnotator.class))
        .experimentReports(new ContextMemoryReport())
        .featureSets(getFeatureSet())
        .featureFilter(FilterLuceneCharacterNgramStartingWithLetter.class.getName())
        .learningMode(LearningMode.SINGLE_LABEL)
        .featureMode(FeatureMode.SEQUENCE)
        .machineLearningBackend(new MLBackend(new CrfSuiteAdapter(), CrfSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR))
        .run();
    }
}
