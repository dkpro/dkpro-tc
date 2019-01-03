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
package org.dkpro.tc.examples.learningCurves;

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
import org.dkpro.tc.features.ngram.CharacterNGram;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.crfsuite.CrfSuiteAdapter;
import org.dkpro.tc.ml.experiment.builder.ExperimentBuilder;
import org.dkpro.tc.ml.experiment.builder.ExperimentType;

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;

/**
 * Example for NER as sequence classification.
 */
public class ShallowLearningLearningCurve
    implements Constants
{

    public static final String LANGUAGE_CODE = "de";
    public static final int NUM_FOLDS = 2;
    public static final String corpusFilePath = "src/main/resources/data/brown_tei/";

    public static File outputFolder = null;

    public static void main(String[] args) throws Exception
    {
    	System.setProperty("java.util.logging.config.file", "logging.properties");
        DemoUtils.setDkproHome(ShallowLearningLearningCurve.class.getSimpleName());

        ShallowLearningLearningCurve demo = new ShallowLearningLearningCurve();
        demo.runLearningCurve();
    }

    // ##### CV #####
    public void runLearningCurve() throws Exception
    {
        ExperimentBuilder builder = new ExperimentBuilder();
        builder.experiment(ExperimentType.LEARNING_CURVE, "learningCurve")
               .dataReaderTrain(getReaderTrain())
               .numFolds(2)
               .learningCurveLimit(2)
               .featureMode(FeatureMode.SEQUENCE)
               .learningMode(LearningMode.SINGLE_LABEL)
               .featureSets(getFeatureSet())
               .machineLearningBackend(new MLBackend( new CrfSuiteAdapter(), CrfSuiteAdapter.ALGORITHM_LBFGS, "max_iterations=5")
                                       )
               .preprocessing(getPreprocessing())
               .run();
    }

    
    public CollectionReaderDescription getReaderTrain() throws Exception {
        return CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, corpusFilePath,
                TeiReader.PARAM_PATTERNS, "a*.xml");
    }
    
    public TcFeatureSet getFeatureSet() {
		return new TcFeatureSet(TcFeatureFactory.create(CharacterNGram.class));
    }

    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(SequenceOutcomeAnnotator.class);
    }

}
