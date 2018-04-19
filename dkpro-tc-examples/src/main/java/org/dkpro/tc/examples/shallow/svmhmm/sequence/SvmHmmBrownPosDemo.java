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
package org.dkpro.tc.examples.shallow.svmhmm.sequence;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.shallow.crfsuite.sequence.FilterLuceneCharacterNgramStartingWithLetter;
import org.dkpro.tc.examples.shallow.io.BrownCorpusReader;
import org.dkpro.tc.examples.shallow.util.anno.SequenceOutcomeAnnotator;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.features.ngram.CharacterNGram;
import org.dkpro.tc.ml.builder.ExperimentBuilderV2;
import org.dkpro.tc.ml.builder.ExperimentType;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.svmhmm.SvmHmmAdapter;

/**
 * Tests SVMhmm on POS tagging of one file in Brown corpus
 */
public class SvmHmmBrownPosDemo
    implements Constants
{

    public static final String corpusFilePathTrain = "src/main/resources/data/brown_tei";
    private static final int NUM_FOLDS = 3;

    public CollectionReaderDescription getTrainReader() throws Exception{
        return CollectionReaderFactory.createReaderDescription(
                BrownCorpusReader.class, BrownCorpusReader.PARAM_LANGUAGE, "en",
                BrownCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                BrownCorpusReader.PARAM_PATTERNS, "a01.xml");
    }
    
    public CollectionReaderDescription getTestReader() throws Exception{
        return CollectionReaderFactory.createReaderDescription(
                BrownCorpusReader.class, BrownCorpusReader.PARAM_LANGUAGE, "en",
                BrownCorpusReader.PARAM_LANGUAGE, "en", BrownCorpusReader.PARAM_SOURCE_LOCATION,
                corpusFilePathTrain, BrownCorpusReader.PARAM_PATTERNS, "a02.xml");
    }
    
    public TcFeatureSet getFeatureSet() {
        return new TcFeatureSet(TcFeatureFactory.create(TokenRatioPerDocument.class),
                TcFeatureFactory.create(CharacterNGram.class,
                        CharacterNGram.PARAM_NGRAM_USE_TOP_K, 20,
                        CharacterNGram.PARAM_NGRAM_MIN_N, 2,
                        CharacterNGram.PARAM_NGRAM_MAX_N, 3));
    }


    protected void runCrossValidation()
        throws Exception
    {
        ExperimentBuilderV2 builder = new ExperimentBuilderV2();
        builder.experiment(ExperimentType.CROSS_VALIDATION, "trainTestExperiment")
        .dataReaderTrain(getTrainReader())
        .numFolds(NUM_FOLDS)
        .experimentPreprocessing(AnalysisEngineFactory.createEngineDescription(SequenceOutcomeAnnotator.class))
        .experimentReports(new ContextMemoryReport())
        .featureSets(getFeatureSet())
        .featureFilter(FilterLuceneCharacterNgramStartingWithLetter.class.getName())
        .learningMode(LearningMode.SINGLE_LABEL)
        .featureMode(FeatureMode.SEQUENCE)
        .machineLearningBackend(new MLBackend(new SvmHmmAdapter(), "-c", "5.0", "--t", "1", "-m", "0" ))
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
        .machineLearningBackend(new MLBackend(new SvmHmmAdapter(), "-c", "5.0", "--t", "1", "-m", "0" ))
        .run();
    }

    public static void main(String[] args) throws Exception
    {

        DemoUtils.setDkproHome(SvmHmmBrownPosDemo.class.getSimpleName());

        SvmHmmBrownPosDemo experiment = new SvmHmmBrownPosDemo();
        experiment.runTrainTest();
        experiment.runCrossValidation();
    }

}
