/**
 * Copyright 2017
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
package org.dkpro.tc.examples.raw;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.uima.ExtractFeaturesStreamConnector;
import org.dkpro.tc.examples.io.LabeledTweetReader;
import org.dkpro.tc.features.twitter.EmoticonRatio;
import org.dkpro.tc.features.twitter.NumberOfHashTags;
import org.dkpro.tc.ml.weka.writer.WekaDataWriter;

import de.tudarmstadt.ukp.dkpro.core.arktools.ArktweetPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

/**
 * Example of "raw" usage of DKPro TC without DKPro Lab parts. This is for advanced users that do
 * want to have full control over what is going on without having to use the lab architecture.
 * 
 * The pipeline will run all DKPro TC components up to the creation of the input the for the Machine
 * Learning framework. No actual learning or evaluation will be done.
 * 
 */
public class TwitterSentimentRaw
{
    public static void main(String[] args)
        throws Exception
    {
        String corpusFilePathTrain = "src/main/resources/data/twitter/train/*/*.txt";
        String outputPath = "target/ts_raw_output";

        runPipeline(
                // Reader
                createReaderDescription(LabeledTweetReader.class,
                        LabeledTweetReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                        LabeledTweetReader.PARAM_LANGUAGE, "en"),
                // Preprocessing
                createEngineDescription(JCasIdSetter.class),
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(ArktweetPosTagger.class, ArktweetPosTagger.PARAM_LANGUAGE,
                        "en", ArktweetPosTagger.PARAM_VARIANT, "default"),
                // Feature extraction
                createEngineDescription(ExtractFeaturesStreamConnector.class,
                		ExtractFeaturesStreamConnector.PARAM_OUTPUT_DIRECTORY, outputPath,
                		ExtractFeaturesStreamConnector.PARAM_DATA_WRITER_CLASS, WekaDataWriter.class,
                		ExtractFeaturesStreamConnector.PARAM_LEARNING_MODE, Constants.LM_SINGLE_LABEL,
                		ExtractFeaturesStreamConnector.PARAM_FEATURE_MODE, Constants.FM_DOCUMENT,
                        ExtractFeaturesStreamConnector.PARAM_ADD_INSTANCE_ID, true,
                        ExtractFeaturesStreamConnector.PARAM_FEATURE_FILTERS, new String[] {},
                        ExtractFeaturesStreamConnector.PARAM_IS_TESTING, false,
                        ExtractFeaturesStreamConnector.PARAM_USE_SPARSE_FEATURES, false,
                        ExtractFeaturesStreamConnector.PARAM_OUTCOMES, new String [] {"emotional", "neutral"},
                        ExtractFeaturesStreamConnector.PARAM_FEATURE_EXTRACTORS,
                        asList(createExternalResourceDescription(EmoticonRatio.class,
                                EmoticonRatio.PARAM_UNIQUE_EXTRACTOR_NAME, "123"),
                                createExternalResourceDescription(NumberOfHashTags.class,
                                        NumberOfHashTags.PARAM_UNIQUE_EXTRACTOR_NAME, "1234"))));
    }
}
