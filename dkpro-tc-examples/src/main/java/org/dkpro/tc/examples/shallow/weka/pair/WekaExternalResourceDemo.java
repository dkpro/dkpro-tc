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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.shallow.io.PairTwentyNewsgroupsReader;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.pair.similarity.SimilarityPairFeatureExtractor;
import org.dkpro.tc.ml.builder.ExperimentBuilderV2;
import org.dkpro.tc.ml.builder.ExperimentType;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.report.ScatterplotReport;
import org.dkpro.tc.ml.weka.WekaAdapter;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import dkpro.similarity.algorithms.lexical.string.CosineSimilarity.NormalizationMode;
import dkpro.similarity.algorithms.lexical.uima.string.CosineSimilarityResource;
import weka.classifiers.functions.SMO;

/**
 * Demonstrates the usage of external resources within feature extractors, i.e. nested resources in
 * uimaFit. Resource is created with
 * {@link ExternalResourceFactory#createExternalResourceDescription} and then passed to the feature
 * extractor(s) via the parameter space.
 * 
 */
public class WekaExternalResourceDemo
    implements Constants
{

    public static final String experimentName = "PairTwentyNewsgroupsExperiment";
    public static final String languageCode = "en";
    public static final String listFilePathTrain = "src/main/resources/data/twentynewsgroups/pairs/pairslist.train";
    public static final String listFilePathTest = "src/main/resources/data/twentynewsgroups/pairs/pairslist.test";

    public static void main(String[] args) throws Exception
    {
        DemoUtils.setDkproHome(WekaExternalResourceDemo.class.getSimpleName());

        WekaExternalResourceDemo experiment = new WekaExternalResourceDemo();
        experiment.runTrainTest();
    }
    
    public CollectionReaderDescription getTrainReader() throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(
                PairTwentyNewsgroupsReader.class, PairTwentyNewsgroupsReader.PARAM_LISTFILE,
                listFilePathTrain, PairTwentyNewsgroupsReader.PARAM_LANGUAGE_CODE, languageCode);
    }

    public CollectionReaderDescription getTestReader() throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(
                PairTwentyNewsgroupsReader.class, PairTwentyNewsgroupsReader.PARAM_LISTFILE,
                listFilePathTest, PairTwentyNewsgroupsReader.PARAM_LANGUAGE_CODE, languageCode);
    }

    public TcFeatureSet getFeatureSet()
    {

        // Create the External Resource here:
        ExternalResourceDescription gstResource = ExternalResourceFactory
                .createExternalResourceDescription(CosineSimilarityResource.class,
                        CosineSimilarityResource.PARAM_NORMALIZATION,
                        NormalizationMode.L2.toString());

        return new TcFeatureSet(TcFeatureFactory.create(SimilarityPairFeatureExtractor.class,
                        SimilarityPairFeatureExtractor.PARAM_TEXT_SIMILARITY_RESOURCE,
                        gstResource));
    }

    public void runTrainTest() throws Exception
    {

        ExperimentBuilderV2 builder = new ExperimentBuilderV2();
        builder.experiment(ExperimentType.TRAIN_TEST, "trainTestExperiment")
        .dataReaderTrain(getTrainReader())
        .dataReaderTest(getTestReader())
        .experimentPreprocessing(getPreprocessing())
        .experimentReports(new ContextMemoryReport(), new ScatterplotReport())
        .featureSets(getFeatureSet())
        .learningMode(LearningMode.SINGLE_LABEL)
        .featureMode(FeatureMode.PAIR)
        .machineLearningBackend(new MLBackend(new WekaAdapter(), SMO.class.getName()))
        .run();
    }

    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }
}
