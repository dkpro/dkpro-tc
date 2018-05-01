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
package org.dkpro.tc.examples.shallow.raw;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.pear.util.FileUtil;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.uima.ExtractFeaturesConnector;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.features.ngram.meta.WordNGramMC;
import org.dkpro.tc.io.FolderwiseDataReader;
import org.dkpro.tc.ml.base.TcPredictor;
import org.dkpro.tc.ml.base.TcTrainer;
import org.dkpro.tc.ml.weka.core.WekaPredictor;
import org.dkpro.tc.ml.weka.core.WekaTrainer;
import org.dkpro.tc.ml.weka.writer.WekaDataWriter;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import weka.classifiers.functions.SMO;

/**
 * Example of "raw" usage of DKPro TC without DKPro Lab parts. This is for advanced users that do
 * want to have full control over what is going on without having to use the lab architecture.
 * 
 */
public class WekaRawDemoUIMAonly
{
    public static void main(String[] args) throws Exception
    {
      new WekaRawDemoUIMAonly().run();
    }

    private static File runFeatureExtraction(String train, Object[] ngramParameter,
            String outputPathTrain)
        throws Exception
    {
        SimplePipeline.runPipeline(
                // Reader
                CollectionReaderFactory.createReaderDescription(FolderwiseDataReader.class,
                        FolderwiseDataReader.PARAM_SOURCE_LOCATION, train,
                        FolderwiseDataReader.PARAM_LANGUAGE, "en"),
                // Preprocessing
                AnalysisEngineFactory.createEngineDescription(JCasIdSetter.class),
                AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class),

                // Feature extraction
                AnalysisEngineFactory.createEngineDescription(ExtractFeaturesConnector.class,
                        ExtractFeaturesConnector.PARAM_OUTPUT_DIRECTORY, outputPathTrain,
                        ExtractFeaturesConnector.PARAM_DATA_WRITER_CLASS,
                        WekaDataWriter.class.getName(),
                        ExtractFeaturesConnector.PARAM_LEARNING_MODE, Constants.LM_SINGLE_LABEL,
                        ExtractFeaturesConnector.PARAM_FEATURE_MODE, Constants.FM_DOCUMENT,
                        ExtractFeaturesConnector.PARAM_ADD_INSTANCE_ID, false,
                        ExtractFeaturesConnector.PARAM_FEATURE_FILTERS, new String[] {},
                        ExtractFeaturesConnector.PARAM_IS_TESTING, false,
                        ExtractFeaturesConnector.PARAM_USE_SPARSE_FEATURES, false,
                        ExtractFeaturesConnector.PARAM_OUTCOMES,
                        new String[] { "alt.atheism", "comp.graphics" },
                        ExtractFeaturesConnector.PARAM_FEATURE_EXTRACTORS,
                        asList(ExternalResourceFactory.createExternalResourceDescription(WordNGram.class,
                                ngramParameter))));

        return new File(outputPathTrain, Constants.FILENAME_DATA_IN_CLASSIFIER_FORMAT);
    }

    private static void runTrainingMetaCollection(String train, Object[] ngramParameter)
        throws Exception
    {
        // Features such as WordNgram (or any other Ngram-based features) build a Lucene index
        // first, this is done by executing this piece of code. If no Ngram features are used this
        // step is not necessary
        SimplePipeline.runPipeline(
                CollectionReaderFactory.createReaderDescription(FolderwiseDataReader.class,
                        FolderwiseDataReader.PARAM_SOURCE_LOCATION, train,
                        FolderwiseDataReader.PARAM_LANGUAGE, "en"),
                AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class),
                AnalysisEngineFactory.createEngineDescription(WordNGramMC.class, ngramParameter));
    }

    public List<String> run() throws Exception
    {
        String train = "src/main/resources/data/twentynewsgroups/bydate-train/*/*.txt";
        String outputPathTrain = "target/tn_raw_output/train";
        ensureFolderExistence(outputPathTrain);

        String test = "src/main/resources/data/twentynewsgroups/bydate-test/*/*.txt";
        String outputPathTest = "target/tn_raw_output/test";
        ensureFolderExistence(outputPathTest);

        File luceneFolder = new File(FileUtils.getTempDirectory(), "luceneDirRawDemo");
        luceneFolder.deleteOnExit();

        Object[] ngramParameter = new Object[] { WordNGram.PARAM_NGRAM_USE_TOP_K, "500",
                WordNGram.PARAM_UNIQUE_EXTRACTOR_NAME, "123", WordNGram.PARAM_SOURCE_LOCATION,
                luceneFolder.toString(), WordNGramMC.PARAM_TARGET_LOCATION,
                luceneFolder.toString() };

        // Extract features from training data - this steps requires building the Lucene index for
        // the ngram feature
        runTrainingMetaCollection(train, ngramParameter);
        File extractedTrainData = runFeatureExtraction(train, ngramParameter,
                outputPathTrain);

        // Extract features from testing data - we use the Lucene index created during training here
        // - no need to run the meta collection again
        File extractedTestData = runFeatureExtraction(test, ngramParameter,
                outputPathTest);

        File modelOut = FileUtil.createTempFile("modeltmp", ".model");
        modelOut.deleteOnExit();
        TcTrainer trainer = new WekaTrainer();
        trainer.train(extractedTrainData, modelOut, asList(SMO.class.getName()));

        TcPredictor predictor = new WekaPredictor();
        return predictor.predict(extractedTestData, modelOut);
    }

    private void ensureFolderExistence(String outputPathTrain)
    {
        File file = new File(outputPathTrain);
        if (file.exists()) {
            return;
        }
        boolean creationSuccessful = file.mkdirs();
        if (!creationSuccessful) {
            throw new IllegalStateException(
                    "Could not create the folder path [" + file.getAbsolutePath() + "]");
        }
    }
}
