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
package org.dkpro.tc.examples.deeplearning.keras.multi;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.shallow.io.ReutersCorpusReader;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.experiment.builder.DeepExperimentBuilder;
import org.dkpro.tc.ml.experiment.builder.ExperimentType;
import org.dkpro.tc.ml.keras.KerasAdapter;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class KerasMultiLabel
    implements Constants
{
    public static final String LANGUAGE_CODE = "en";

    static String documentTrainFolderReuters = "src/main/resources/data/reuters/training";
    static String documentTestFolderReuters = "src/main/resources/data/reuters/test";
    static String documentGoldLabelsReuters = "src/main/resources/data/reuters/cats.txt";

    public static void main(String[] args) throws Exception
    {

        // DemoUtils.setDkproHome(DeepLearningTestDummy.class.getSimpleName());
        System.setProperty("DKPRO_HOME", System.getProperty("user.home") + "/Desktop");

        
        DeepExperimentBuilder builder = new DeepExperimentBuilder();
        builder.experiment(ExperimentType.TRAIN_TEST, "kerasTrainTest")
                .dataReaderTrain(getTrainReader())
                .dataReaderTest(getTestReader())
                .learningMode(LearningMode.MULTI_LABEL)
                .bipartitionThreshold(0.5)
                .featureMode(FeatureMode.DOCUMENT)
                .preprocessing(getPreprocessing())
                .pythonPath("/usr/local/bin/python3")
                .embeddingPath("src/test/resources/wordvector/glove.6B.50d_250.txt")
                .maximumLength(50)
                .vectorizeToInteger(true)
                .machineLearningBackend(new MLBackend(new KerasAdapter(),
                        "src/main/resources/kerasCode/multiLabel/multi.py"))
                .run();
        
    }

    private static CollectionReaderDescription getTestReader() throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(
                ReutersCorpusReader.class, ReutersCorpusReader.PARAM_SOURCE_LOCATION,
                documentTrainFolderReuters, ReutersCorpusReader.PARAM_GOLD_LABEL_FILE,
                documentGoldLabelsReuters, ReutersCorpusReader.PARAM_LANGUAGE, "en",
                ReutersCorpusReader.PARAM_PATTERNS, ReutersCorpusReader.INCLUDE_PREFIX + "*.txt");
    }

    private static CollectionReaderDescription getTrainReader() throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(
                ReutersCorpusReader.class, ReutersCorpusReader.PARAM_SOURCE_LOCATION,
                documentTrainFolderReuters, ReutersCorpusReader.PARAM_GOLD_LABEL_FILE,
                documentGoldLabelsReuters, ReutersCorpusReader.PARAM_LANGUAGE, "en",
                ReutersCorpusReader.PARAM_PATTERNS, ReutersCorpusReader.INCLUDE_PREFIX + "*.txt");
    }

    protected static AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }
}
