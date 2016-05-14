/**
 * Copyright 2016
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
package org.dkpro.tc.groovyexamples.single.document

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription

import org.apache.uima.fit.factory.CollectionReaderFactory
import org.dkpro.lab.Lab
import org.dkpro.lab.task.Dimension
import org.dkpro.tc.core.Constants
import org.dkpro.tc.examples.io.LabeledTweetReader
import org.dkpro.tc.examples.util.DemoUtils
import org.dkpro.tc.features.twitter.EmoticonRatioDFE
import org.dkpro.tc.features.twitter.NumberOfHashTagsDFE
import org.dkpro.tc.ml.ExperimentCrossValidation
import org.dkpro.tc.ml.ExperimentTrainTest
import org.dkpro.tc.ml.report.BatchCrossValidationReport
import org.dkpro.tc.ml.report.BatchTrainTestReport
import org.dkpro.tc.weka.WekaClassificationAdapter

import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.trees.RandomForest
import de.tudarmstadt.ukp.dkpro.core.arktools.ArktweetPosTagger
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import org.apache.uima.fit.factory.CollectionReaderFactory;
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
 */
public class TwitterSentimentDemo implements Constants {


    /**
     * Overall Experiment Setup (Cross-validation)
     *
     * @throws Exception
     */
    protected void runCrossValidation() throws Exception {
        
       def trainreader = CollectionReaderFactory.createReaderDescription(LabeledTweetReader.class,
             LabeledTweetReader.PARAM_SOURCE_LOCATION,  "src/main/resources/data/twitter/train/*/*.txt"
          );

        ExperimentCrossValidation batchTask = [
            experimentName: "Twitter-Sentiment-CV",
            type: "Evaluation-Twitter-Sentiment-CV",
            machineLearningAdapter: WekaClassificationAdapter,
            preprocessing: createEngineDescription(createEngineDescription(BreakIteratorSegmenter),createEngineDescription(
            ArktweetPosTagger, ArktweetPosTagger.PARAM_LANGUAGE, "en", ArktweetPosTagger.PARAM_VARIANT, "default")), // Preprocessing
            parameterSpace: [
                // parameters in the parameter space with several values in a list will be swept
                Dimension.createBundle("readers", [
                    readerTrain: trainreader,
                    ]),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_SET, [
                    EmoticonRatioDFE.name,
                    NumberOfHashTagsDFE.name
                ]),
                Dimension.create(DIM_CLASSIFICATION_ARGS,[NaiveBayes.name], [RandomForest.name])
            ],
            reports: [
                BatchCrossValidationReport
            ], // collects results from folds
            numFolds: 2]

        // Run
        Lab.getInstance().run(batchTask)

    }

    /**
     * Overall Experiment Setup (Train-Test)
     *
     * @throws Exception
     */
    protected void runTrainTest() throws Exception {
        
        def trainreader = CollectionReaderFactory.createReaderDescription(LabeledTweetReader.class,
            LabeledTweetReader.PARAM_SOURCE_LOCATION,  "src/main/resources/data/twitter/train/*/*.txt"
         );
     
         def testreader= CollectionReaderFactory.createReaderDescription(LabeledTweetReader.class,
         LabeledTweetReader.PARAM_SOURCE_LOCATION,  "src/main/resources/data/twitter/test/*/*.txt"
         );

        ExperimentTrainTest batchTask = [
            experimentName: "Twitter-Sentiment-TrainTest",
            type: "Evaluation-Twitter-Sentiment-TrainTest",
            machineLearningAdapter: WekaClassificationAdapter,
            preprocessing: createEngineDescription(createEngineDescription(BreakIteratorSegmenter),createEngineDescription(
            ArktweetPosTagger, ArktweetPosTagger.PARAM_LANGUAGE, "en", ArktweetPosTagger.PARAM_VARIANT, "default")), // Preprocessing
            parameterSpace: [
                // parameters in the parameter space with several values in a list will be swept
                Dimension.createBundle("readers", [
                    readerTrain: trainreader,
                    readerTest: testreader,
                ]),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_SET, [
                    EmoticonRatioDFE.name,
                    NumberOfHashTagsDFE.name
                ]),
                Dimension.create(DIM_CLASSIFICATION_ARGS, [NaiveBayes.name], [RandomForest.name])
            ],
            reports: [BatchTrainTestReport], // collects results from folds
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
		DemoUtils.setDkproHome(TwitterSentimentDemo.getSimpleName());
        new TwitterSentimentDemo().runCrossValidation()
        new TwitterSentimentDemo().runTrainTest() } }
