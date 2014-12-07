/**
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.examples.raw;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import de.tudarmstadt.ukp.dkpro.core.arktools.ArktweetTagger;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.task.uima.ExtractFeaturesConnector;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.LabeledTweetReader;
import de.tudarmstadt.ukp.dkpro.tc.features.twitter.EmoticonRatioDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.twitter.NumberOfHashTagsDFE;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

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
                createReaderDescription(
                        LabeledTweetReader.class,
                        LabeledTweetReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                        LabeledTweetReader.PARAM_LANGUAGE, "en"),
                // Preprocessing
                createEngineDescription(
                        ArktweetTagger.class, ArktweetTagger.PARAM_LANGUAGE, "en",
                        ArktweetTagger.PARAM_VARIANT, "default"),
                // Feature extraction
                createEngineDescription(
                        ExtractFeaturesConnector.class,
                        ExtractFeaturesConnector.PARAM_OUTPUT_DIRECTORY, outputPath,
                        ExtractFeaturesConnector.PARAM_DATA_WRITER_CLASS, WekaDataWriter.class,
                        ExtractFeaturesConnector.PARAM_LEARNING_MODE, Constants.LM_SINGLE_LABEL,
                        ExtractFeaturesConnector.PARAM_FEATURE_MODE, Constants.FM_DOCUMENT,
                        ExtractFeaturesConnector.PARAM_ADD_INSTANCE_ID, true,
                        ExtractFeaturesConnector.PARAM_FEATURE_EXTRACTORS, asList(
                                createExternalResourceDescription(EmoticonRatioDFE.class),
                                createExternalResourceDescription(NumberOfHashTagsDFE.class))));
    }
}
