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
import org.dkpro.tc.examples.shallow.annotators.UnitOutcomeAnnotator;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.features.ngram.CharacterNGram;
import org.dkpro.tc.ml.builder.ExperimentBuilder;
import org.dkpro.tc.ml.builder.ExperimentType;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.dkpro.tc.ml.xgboost.XgboostAdapter;

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;
import weka.classifiers.bayes.NaiveBayes;

/**
 * This is an example for POS tagging as unit classification. Each POS is treated as a
 * classification unit, but unlike sequence tagging the decision for each POS is taken
 * independently. This will usually give worse results, so this is only to showcase the concept.
 */
public class UnitDemo
    implements Constants
{

    public static final String LANGUAGE_CODE = "en";

    public static final String corpusFilePathTrain = "src/main/resources/data/brown_tei/";

    public static void main(String[] args) throws Exception
    {
        DemoUtils.setDkproHome(UnitDemo.class.getSimpleName());

        new UnitDemo().runTrainTest();
    }
    
    public CollectionReaderDescription getReaderTrain() throws Exception {
        return CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                TeiReader.PARAM_PATTERNS, "*.xml");
    }
    
    public CollectionReaderDescription getReaderTest() throws Exception {
        return CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                TeiReader.PARAM_PATTERNS, "*.xml");
    }
    
    public TcFeatureSet getFeatureSet() {
        return new TcFeatureSet(TcFeatureFactory.create(TokenRatioPerDocument.class),
                TcFeatureFactory.create(CharacterNGram.class,
                        CharacterNGram.PARAM_NGRAM_USE_TOP_K, 50));
    }

    // ##### Train Test #####
    public void runTrainTest() throws Exception
    {

        ExperimentBuilder builder = new ExperimentBuilder();
        builder.experiment(ExperimentType.TRAIN_TEST, "trainTest")
                .dataReaderTrain(getReaderTrain())
                .dataReaderTest(getReaderTest())
                .preprocessing(getPreprocessing())
                .featureSets(getFeatureSet())
                .learningMode(LearningMode.SINGLE_LABEL)
                .featureMode(FeatureMode.UNIT)
                .machineLearningBackend(
                        new MLBackend(new WekaAdapter(), NaiveBayes.class.getName()),
                        new MLBackend(new LibsvmAdapter()),
                        new MLBackend(new LiblinearAdapter()),
                        new MLBackend(new XgboostAdapter())
                        )
                .run();
    }

    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(UnitOutcomeAnnotator.class);
    }
}
