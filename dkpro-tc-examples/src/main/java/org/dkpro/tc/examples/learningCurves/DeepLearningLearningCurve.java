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

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.io.FolderwiseDataReader;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.experiment.builder.DeepExperimentBuilder;
import org.dkpro.tc.ml.experiment.builder.ExperimentType;
import org.dkpro.tc.ml.keras.KerasAdapter;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class DeepLearningLearningCurve
    implements Constants
{
    public static final String LANGUAGE_CODE = "en";

    public static final String corpusFilePath = "src/main/resources/data/twentynewsgroups/bydate-train";

    public static void main(String[] args) throws Exception
    {
    	DemoUtils.setDkproHome(DeepLearningLearningCurve.class.getSimpleName());

        DeepExperimentBuilder builder = new DeepExperimentBuilder();
        builder.experiment(ExperimentType.LEARNING_CURVE_FIXED_TEST_SET, "learningCurve")
               .numFolds(3)
               .learningCurveLimit(2)
               .dataReaderTrain(getTrainReader())
               .learningMode(LearningMode.SINGLE_LABEL)
               .featureMode(FeatureMode.DOCUMENT)
               .preprocessing(getPreprocessing())
               .embeddingPath("src/test/resources/wordvector/glove.6B.50d_250.txt")
               .pythonPath("/usr/local/bin/python3")
               .maximumLength(100)
               .vectorizeToInteger(true)
               .machineLearningBackend(
                           new MLBackend(new KerasAdapter(), "src/main/resources/kerasCode/singleLabel/imdb_cnn_lstm.py")
                       )
               .run();
    	
    }

    private static CollectionReaderDescription getTrainReader() throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(
                FolderwiseDataReader.class, FolderwiseDataReader.PARAM_SOURCE_LOCATION,
                corpusFilePath, FolderwiseDataReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                FolderwiseDataReader.PARAM_PATTERNS, "**/*.txt");
    }

//    public static ParameterSpace getParameterSpace(String python3)
//        throws ResourceInitializationException
//    {
//        // configure training and test data reader dimension
//        // train/test will use both, while cross-validation will only use the
//        // train part
//        Map<String, Object> dimReaders = new HashMap<String, Object>();
//
//        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
//                FolderwiseDataReader.class, FolderwiseDataReader.PARAM_SOURCE_LOCATION,
//                corpusFilePath, FolderwiseDataReader.PARAM_LANGUAGE, LANGUAGE_CODE,
//                FolderwiseDataReader.PARAM_PATTERNS, "**/*.txt");
//        dimReaders.put(DIM_READER_TRAIN, readerTrain);
//
//        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
//                Dimension.create(DIM_FEATURE_MODE, Constants.FM_DOCUMENT),
//                Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
//                Dimension.create(DeepLearningConstants.DIM_PYTHON_INSTALLATION,
//                        "/usr/local/bin/python3"),
//                Dimension.create(DeepLearningConstants.DIM_USER_CODE,
//                        "src/main/resources/kerasCode/singleLabel/imdb_cnn_lstm.py"),
//                Dimension.create(DeepLearningConstants.DIM_MAXIMUM_LENGTH, 250),
//                Dimension.create(DeepLearningConstants.DIM_VECTORIZE_TO_INTEGER, true),
//                Dimension.create(DeepLearningConstants.DIM_PRETRAINED_EMBEDDINGS,
//                        "src/test/resources/wordvector/glove.6B.50d_250.txt"));
//
//        return pSpace;
//    }

    // ##### TRAIN-TEST #####
//    public static void runLearningCurve(ParameterSpace pSpace, ContextMemoryReport contextReport) throws Exception
//    {
//    	DeepLearningExperimentLearningCurve experiment = new DeepLearningExperimentLearningCurve(
//                "KerasCrossValidation", 3, 2);
//        experiment.setPreprocessing(getPreprocessing());
//        experiment.setParameterSpace(pSpace);
//        experiment.addReport(LearningCurveReport.class);
//        experiment.addReport(contextReport);
//        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
//
//        // Run
//        Lab.getInstance().run(experiment);
//    }

    protected static AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }
}
