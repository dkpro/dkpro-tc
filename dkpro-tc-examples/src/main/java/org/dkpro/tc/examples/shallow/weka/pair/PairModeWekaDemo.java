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
package org.dkpro.tc.examples.shallow.weka.pair;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.shallow.annotators.SequenceOutcomeAnnotator;
import org.dkpro.tc.examples.shallow.filter.FilterCharNgramsByStartingLetter;
import org.dkpro.tc.examples.shallow.io.PairTwentyNewsgroupsReader;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.pair.core.length.DiffNrOfTokensPairFeatureExtractor;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.builder.ExperimentBuilder;
import org.dkpro.tc.ml.builder.ExperimentType;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.weka.WekaAdapter;

import weka.classifiers.bayes.NaiveBayes;

/**
 * PairTwentyNewsgroupsExperiment, using Java
 * 
 * The PairTwentyNewsgroupsExperiment takes pairs of news files and trains/tests a binary classifier
 * to learn if the files in the pair are from the same newsgroup. The pairs are listed in a tsv
 * file: see the files in src/main/resources/lists/ as examples.
 * <p>
 * PairTwentyNewsgroupsExperiment uses similar architecture as TwentyNewsgroupsGroovyExperiment (
 * {@link ExperimentTrainTest}) to automatically wire the standard tasks for a basic TrainTest
 * setup. To remind the user to be careful of information leak when training and testing on pairs of
 * data from similar sources, we do not provide a demo Cross Validation setup here. (Our sample
 * train and test datasets are from separate newsgroups.) Please see
 * TwentyNewsgroupsGroovyExperiment for a demo implementing a CV experiment.
 */
public class PairModeWekaDemo
    implements Constants
{

    public static final String experimentName = "PairTwentyNewsgroupsExperiment";
    public static final String languageCode = "en";
    public static final String listFilePathTrain = "src/main/resources/data/twentynewsgroups/pairs/pairslist.train";
    public static final String listFilePathTest = "src/main/resources/data/twentynewsgroups/pairs/pairslist.test";

    public static void main(String[] args) throws Exception
    {
        DemoUtils.setDkproHome(PairModeWekaDemo.class.getSimpleName());

        PairModeWekaDemo experiment = new PairModeWekaDemo();
        experiment.runTrainTest();
    }
    
    public CollectionReaderDescription getTrainReader() throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(PairTwentyNewsgroupsReader.class,
                PairTwentyNewsgroupsReader.PARAM_LISTFILE, listFilePathTrain,
                PairTwentyNewsgroupsReader.PARAM_LANGUAGE_CODE, languageCode);
    }

    public CollectionReaderDescription getTestReader() throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(PairTwentyNewsgroupsReader.class,
                PairTwentyNewsgroupsReader.PARAM_LISTFILE, listFilePathTest,
                PairTwentyNewsgroupsReader.PARAM_LANGUAGE_CODE, languageCode);
    }
    
    public TcFeatureSet getFeatureSet() {
        return new TcFeatureSet(
                TcFeatureFactory.create(DiffNrOfTokensPairFeatureExtractor.class));
    }

    // ##### TRAIN-TEST #####
    public void runTrainTest() throws Exception
    {
        ExperimentBuilder builder = new ExperimentBuilder();
        builder.experiment(ExperimentType.TRAIN_TEST, "trainTestExperiment")
        .dataReaderTrain(getTrainReader())
        .dataReaderTest(getTestReader())
        .preprocessing(AnalysisEngineFactory.createEngineDescription(SequenceOutcomeAnnotator.class))
        .reports(new ContextMemoryReport())
        .featureSets(getFeatureSet())
        .featureFilter(FilterCharNgramsByStartingLetter.class.getName())
        .learningMode(LearningMode.SINGLE_LABEL)
        .featureMode(FeatureMode.PAIR)
        .machineLearningBackend(new MLBackend(new WekaAdapter(), NaiveBayes.class.getName()))
        .run();
    }

}
