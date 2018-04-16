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
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
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

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
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

    private static File runFeatureExtraction(String train, File luceneFolder,
            Object[] ngramParameter, String outputPathTrain)
        throws Exception
    {
        runPipeline(
                // Reader
                createReaderDescription(FolderwiseDataReader.class,
                        FolderwiseDataReader.PARAM_SOURCE_LOCATION, train,
                        FolderwiseDataReader.PARAM_LANGUAGE, "en"),
                // Preprocessing
                createEngineDescription(JCasIdSetter.class),
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class),

                // Feature extraction
                createEngineDescription(ExtractFeaturesConnector.class,
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
                        asList(createExternalResourceDescription(WordNGram.class,
                                ngramParameter))));

        return new File(outputPathTrain, Constants.FILENAME_DATA_IN_CLASSIFIER_FORMAT);
    }

    private static void runTrainingMetaCollection(String train, File luceneFolder,
            Object[] ngramParameter)
        throws Exception
    {
        // Features such as WordNgram (or any other Ngram-based features) build a Lucene index
        // first, this is done by executing this piece of code. If no Ngram features are used this
        // step is not necessary
        runPipeline(
                createReaderDescription(FolderwiseDataReader.class,
                        FolderwiseDataReader.PARAM_SOURCE_LOCATION, train,
                        FolderwiseDataReader.PARAM_LANGUAGE, "en"),
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(WordNGramMC.class, ngramParameter));
    }

    public List<String> run() throws Exception
    {
        String train = "src/main/resources/data/twentynewsgroups/bydate-train/*/*.txt";
        String outputPathTrain = "target/tn_raw_output/train";
        new File(outputPathTrain).mkdirs();

        String test = "src/main/resources/data/twentynewsgroups/bydate-test/*/*.txt";
        String outputPathTest = "target/tn_raw_output/test";
        new File(outputPathTest).mkdirs();

        File luceneFolder = new File(FileUtils.getTempDirectory(), "luceneDirRawDemo");
        luceneFolder.deleteOnExit();

        Object[] ngramParameter = new Object[] { WordNGram.PARAM_NGRAM_USE_TOP_K, "500",
                WordNGram.PARAM_UNIQUE_EXTRACTOR_NAME, "123", WordNGram.PARAM_SOURCE_LOCATION,
                luceneFolder.toString(), WordNGramMC.PARAM_TARGET_LOCATION,
                luceneFolder.toString() };

        // Extract features from training data - this steps requires building the lucene index for
        // the ngram feature
        runTrainingMetaCollection(train, luceneFolder, ngramParameter);
        File extractedTrainData = runFeatureExtraction(train, luceneFolder, ngramParameter,
                outputPathTrain);

        // Extract features from testing data - we use the lucene index created during training here
        // - no need to run the meta collection
        File extractedTestData = runFeatureExtraction(test, luceneFolder, ngramParameter,
                outputPathTest);

        File modelOut = FileUtil.createTempFile("modeltmp", ".model");
        modelOut.deleteOnExit();
        TcTrainer trainer = new WekaTrainer();
        trainer.train(extractedTrainData, modelOut, Arrays.asList(SMO.class.getName()));

        TcPredictor predictor = new WekaPredictor();
        return predictor.predict(extractedTestData, modelOut);
    }
}
