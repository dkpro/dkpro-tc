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
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.features.style.InitialCharacterUpperCase;
import org.dkpro.tc.ml.builder.ExperimentBuilder;
import org.dkpro.tc.ml.builder.ExperimentType;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.crfsuite.CrfSuiteAdapter;
import org.dkpro.tc.ml.svmhmm.SvmHmmAdapter;

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;

/**
 * Example for NER as sequence classification.
 */
public class SequenceDemo
    implements Constants
{

    public static final String LANGUAGE_CODE = "de";
    public static final int NUM_FOLDS = 2;
    public static final String corpusFilePath = "src/main/resources/data/brown_tei/";

    public static File outputFolder = null;

    public static void main(String[] args) throws Exception
    {
        DemoUtils.setDkproHome(SequenceDemo.class.getSimpleName());

        SequenceDemo demo = new SequenceDemo();
        demo.runCrossValidation();
        demo.runTrainTest();
    }

    // ##### CV #####
    protected void runCrossValidation() throws Exception
    {
        ExperimentBuilder builder = new ExperimentBuilder();
        builder.experiment(ExperimentType.CROSS_VALIDATION, "crossValidationExperiment")
               .dataReaderTrain(getReaderTrain())
               .numFolds(2)
               .featureMode(FeatureMode.SEQUENCE)
               .learningMode(LearningMode.SINGLE_LABEL)
               .featureSets(getFeatureSet())
               .machineLearningBackend(new MLBackend( new CrfSuiteAdapter(), CrfSuiteAdapter.ALGORITHM_LBFGS, "max_iterations=5"),
                                       new MLBackend(new SvmHmmAdapter(), "-c", "1000", "-e", "100")
                                       )
               .preprocessing(getPreprocessing())
               .run();
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
               .machineLearningBackend(new MLBackend( new CrfSuiteAdapter(), CrfSuiteAdapter.ALGORITHM_LBFGS, "max_iterations=5"),
                                       new MLBackend(new SvmHmmAdapter(), "-c", "1000", "-e", "100")
                                       )
               .preprocessing(getPreprocessing())
               .run();
    }
    
    public CollectionReaderDescription getReaderTrain() throws Exception {
        return CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, corpusFilePath,
                TeiReader.PARAM_PATTERNS, "a01.xml");
    }
    
    public CollectionReaderDescription getReaderTest() throws Exception {
        return CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, corpusFilePath,
                TeiReader.PARAM_PATTERNS, "a02.xml");
    }
    
    public TcFeatureSet getFeatureSet() {
        return new TcFeatureSet(TcFeatureFactory.create(TokenRatioPerDocument.class),
                TcFeatureFactory.create(InitialCharacterUpperCase.class));
    }

    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(SequenceOutcomeAnnotator.class);
    }

}
