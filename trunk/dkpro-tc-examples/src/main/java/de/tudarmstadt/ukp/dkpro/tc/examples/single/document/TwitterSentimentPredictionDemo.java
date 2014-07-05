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
package de.tudarmstadt.ukp.dkpro.tc.examples.single.document;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.RandomForest;
import de.tudarmstadt.ukp.dkpro.core.arktools.ArktweetTagger;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.LabeledTweetReader;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.UnlabeledTweetReader;
import de.tudarmstadt.ukp.dkpro.tc.features.twitter.EmoticonRatioDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.twitter.NumberOfHashTagsDFE;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskPrediction;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * This a pure Java-based experiment setup of the Twitter Sentiment experiment, as described in:
 * 
 * <pre>
 * Johannes Daxenberger and Oliver Ferschke and Iryna Gurevych and Torsten Zesch (2014).
 * DKPro TC: A Java-based Framework for Supervised Learning Experiments on Textual Data.
 * In: Proceedings of the 52nd Annual Meeting of the ACL.
 * </pre>
 * 
 * This simplistic demo show-cases how to setup an experiment to classify a set of unlabeled tweets
 * as either "emotional" or "neutral", using a small set of labeled tweets to train a model.
 * 
 * @see de.tudarmstadt.ukp.dkpro.tc.groovyexamples.single.document.TwitterSentimentDemo
 */
public class TwitterSentimentPredictionDemo
    implements Constants
{

    public static void main(String[] args)
        throws Exception
    {
        ParameterSpace pSpace = getParameterSpace();

        TwitterSentimentPredictionDemo experiment = new TwitterSentimentPredictionDemo();
        experiment.runPrediction(pSpace);
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace()
    {

        // training data to learn a model
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, LabeledTweetReader.class);
        dimReaders.put(
                DIM_READER_TRAIN_PARAMS,
                Arrays.asList(new Object[] { LabeledTweetReader.PARAM_SOURCE_LOCATION,
                        "src/main/resources/data/twitter/train",
                        LabeledTweetReader.PARAM_LANGUAGE,
                        "en", LabeledTweetReader.PARAM_PATTERNS,
                        LabeledTweetReader.INCLUDE_PREFIX + "*/*.txt" }));
        // unlabeled data which will be classified using the trained model
        dimReaders.put(DIM_READER_TEST, LabeledTweetReader.class);
        dimReaders.put(
                DIM_READER_TEST_PARAMS,
                Arrays.asList(new Object[] { UnlabeledTweetReader.PARAM_SOURCE_LOCATION,
                        "src/main/resources/data/twitter/unlabeled",
                        UnlabeledTweetReader.PARAM_LANGUAGE,
                        "en", UnlabeledTweetReader.PARAM_PATTERNS,
                        UnlabeledTweetReader.INCLUDE_PREFIX + "*.txt" }));

        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { NaiveBayes.class.getName() }),
                Arrays.asList(new String[] { RandomForest.class.getName() }));

        Dimension<List<String>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                Arrays.asList(new String[] { EmoticonRatioDFE.class.getName(),
                        NumberOfHashTagsDFE.class.getName() }));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_DATA_WRITER, WekaDataWriter.class.getName()),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL), Dimension.create(
                        DIM_FEATURE_MODE, FM_DOCUMENT), dimFeatureSets,
                dimClassificationArgs);

        return pSpace;
    }

    // ##### PREDICTION #####
    protected void runPrediction(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskPrediction batch = new BatchTaskPrediction("TwitterSentimentPrediction",
                getPreprocessing());
        batch.setParameterSpace(pSpace);

        // Run
        Lab.getInstance().run(batch);
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(
                ArktweetTagger.class, ArktweetTagger.PARAM_LANGUAGE, "en",
                ArktweetTagger.PARAM_VARIANT,
                "default");
    }
}
