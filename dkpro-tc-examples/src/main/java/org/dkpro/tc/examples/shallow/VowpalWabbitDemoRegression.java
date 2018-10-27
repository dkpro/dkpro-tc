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
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.io.DelimiterSeparatedValuesReader;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.experiment.builder.ExperimentBuilder;
import org.dkpro.tc.ml.experiment.builder.ExperimentType;
import org.dkpro.tc.ml.report.RuntimeReport;
import org.dkpro.tc.ml.vowpalwabbit.VowpalWabbitAdapter;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class VowpalWabbitDemoRegression
    implements Constants
{

    public static void main(String[] args) throws Exception
    {

    	System.setProperty("java.util.logging.config.file", "logging.properties");
    	System.setProperty("DKPRO_HOME", System.getProperty("user.home")+"/Desktop/");

        VowpalWabbitDemoRegression experiment = new VowpalWabbitDemoRegression();
        experiment.runTrainTest();
        experiment.runCrossValidation();
    }

    public CollectionReaderDescription getReaderTrain() throws Exception
    {
        return CollectionReaderFactory.createReaderDescription(DelimiterSeparatedValuesReader.class,
                DelimiterSeparatedValuesReader.PARAM_OUTCOME_INDEX, 0,
                DelimiterSeparatedValuesReader.PARAM_TEXT_INDEX, 1,
                DelimiterSeparatedValuesReader.PARAM_SOURCE_LOCATION,
                "src/main/resources/data/essays/train/essay_train.txt",
                DelimiterSeparatedValuesReader.PARAM_LANGUAGE, "en");
    }

    public CollectionReaderDescription getReaderTest() throws Exception
    {
        return CollectionReaderFactory.createReaderDescription(DelimiterSeparatedValuesReader.class,
                DelimiterSeparatedValuesReader.PARAM_OUTCOME_INDEX, 0,
                DelimiterSeparatedValuesReader.PARAM_TEXT_INDEX, 1,
                DelimiterSeparatedValuesReader.PARAM_SOURCE_LOCATION,
                "src/main/resources/data/essays/test/essay_test.txt",
                DelimiterSeparatedValuesReader.PARAM_LANGUAGE, "en");
    }

    public TcFeatureSet getFeatureSet()
    {
        return new TcFeatureSet(
                TcFeatureFactory.create(WordNGram.class)
                );
    }

    // ##### TRAIN-TEST #####
    public void runTrainTest() throws Exception
    {
        ExperimentBuilder builder = new ExperimentBuilder();
        builder.experiment(ExperimentType.TRAIN_TEST, "trainTestRegression")
                .dataReaderTrain(getReaderTrain())
                .dataReaderTest(getReaderTest())
                .featureSets(getFeatureSet())
                .learningMode(LearningMode.REGRESSION)
                .featureMode(FeatureMode.DOCUMENT)
                .preprocessing(getPreprocessing())
                .machineLearningBackend(
                        new MLBackend(new VowpalWabbitAdapter())
                        )
                .run();
    }

    public void runCrossValidation() throws Exception
    {
        ExperimentBuilder builder = new ExperimentBuilder();
        builder.experiment(ExperimentType.CROSS_VALIDATION, "crossValidationRegression").numFolds(2)
                .dataReaderTrain(getReaderTrain())
                .numFolds(2)
                .featureSets(getFeatureSet())
                .learningMode(LearningMode.REGRESSION)
                .featureMode(FeatureMode.DOCUMENT)
                .reports(new ContextMemoryReport(), new RuntimeReport())
                .preprocessing(getPreprocessing())
                .machineLearningBackend(
                        new MLBackend(new VowpalWabbitAdapter())
                        )
                .run();
    }

    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }
}
