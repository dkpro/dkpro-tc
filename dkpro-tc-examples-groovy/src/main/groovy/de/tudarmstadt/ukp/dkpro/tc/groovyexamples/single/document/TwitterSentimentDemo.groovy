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
package de.tudarmstadt.ukp.dkpro.tc.groovyexamples.single.document

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.trees.RandomForest
import de.tudarmstadt.ukp.dkpro.core.arktools.ArktweetTagger
import de.tudarmstadt.ukp.dkpro.lab.Lab
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension
import de.tudarmstadt.ukp.dkpro.tc.core.Constants
import de.tudarmstadt.ukp.dkpro.tc.examples.io.LabeledTweetReader
import de.tudarmstadt.ukp.dkpro.tc.features.twitter.EmoticonRatioDFE
import de.tudarmstadt.ukp.dkpro.tc.features.twitter.NumberOfHashTagsDFE
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchCrossValidationReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchTrainTestReport
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter

/**
 * Running example as described in the paper:
 *
 * <pre>
 * Johannes Daxenberger and Oliver Ferschke and Iryna Gurevych and Torsten Zesch (2014).
 * DKPro TC: A Java-based Framework for Supervised Learning Experiments on Textual Data.
 * In: Proceedings of the 52nd Annual Meeting of the ACL.
 * </pre>
 *
 * This simplistic demo show-cases how to setup an experiment to classify a set of tweets as either "emotional" or "neutral".
 *
 * @author Johannes Daxenberger
 */
public class TwitterSentimentDemo implements Constants {


    /**
     * Overall Experiment Setup (Cross-validation)
     *
     * @throws Exception
     */
    protected void runCrossValidation() throws Exception {

        BatchTaskCrossValidation batchTask = [
            experimentName: "Twitter-Sentiment-CV",
            type: "Evaluation-Twitter-Sentiment-CV",
            preprocessingPipeline: createEngineDescription(
            ArktweetTagger, ArktweetTagger.PARAM_LANGUAGE, "en", ArktweetTagger.PARAM_VARIANT, "default"), // Preprocessing
            parameterSpace: [
                // parameters in the parameter space with several values in a list will be swept
                Dimension.createBundle("readers", [
                    readerTrain: LabeledTweetReader,
                    readerTrainParams: [
                        LabeledTweetReader.PARAM_SOURCE_LOCATION,
                        "src/main/resources/data/twitter/train/*/*.txt"
                    ]]),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_SET, [
                    EmoticonRatioDFE.name,
                    NumberOfHashTagsDFE.name
                ]),
                Dimension.create(DIM_DATA_WRITER, WekaDataWriter.name),
                Dimension.create(DIM_CLASSIFICATION_ARGS,[NaiveBayes.name], [RandomForest.name])
            ],
            reports: [WekaBatchCrossValidationReport], // collects results from folds
            numFolds: 10]

        // Run
        Lab.getInstance().run(batchTask)

    }

    /**
     * Overall Experiment Setup (Train-Test)
     *
     * @throws Exception
     */
    protected void runTrainTest() throws Exception {

        BatchTaskTrainTest batchTask = [
            experimentName: "Twitter-Sentiment-TrainTest",
            type: "Evaluation-Twitter-Sentiment-TrainTest",
            preprocessingPipeline: createEngineDescription(
            ArktweetTagger, ArktweetTagger.PARAM_LANGUAGE, "en", ArktweetTagger.PARAM_VARIANT, "default"), // Preprocessing
            parameterSpace: [
                // parameters in the parameter space with several values in a list will be swept
                Dimension.createBundle("readers", [
                    readerTrain: LabeledTweetReader,
                    readerTrainParams: [
                        LabeledTweetReader.PARAM_SOURCE_LOCATION,
                        "src/main/resources/data/twitter/train/*/*.txt"
                    ],
                    readerTest: LabeledTweetReader,
                    readerTestParams: [
                        LabeledTweetReader.PARAM_SOURCE_LOCATION,
                        "src/main/resources/data/twitter/test/*/*.txt"
                    ],
                ]),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_SET, [
                    EmoticonRatioDFE.name,
                    NumberOfHashTagsDFE.name
                ]),
                Dimension.create(DIM_DATA_WRITER, WekaDataWriter.name),
                Dimension.create(DIM_CLASSIFICATION_ARGS, [NaiveBayes.name], [RandomForest.name])
            ],
            reports: [WekaBatchTrainTestReport], // collects results from folds
        ]

        // Run
        Lab.getInstance().run(batchTask)

    }

    /**
     * For testing.
     *
     * @param args
     */
    public static void main(String[] args) {
        new TwitterSentimentDemo().runCrossValidation()
        new TwitterSentimentDemo().runTrainTest() } }
