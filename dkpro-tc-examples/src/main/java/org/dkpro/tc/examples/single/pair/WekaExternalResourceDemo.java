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
package org.dkpro.tc.examples.single.pair;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcFeature;
import org.dkpro.tc.core.util.TcFeatureFactory;
import org.dkpro.tc.examples.io.PairTwentyNewsgroupsReader;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.pair.similarity.SimilarityPairFeatureExtractor;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.weka.WekaClassificationAdapter;

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

    public static void main(String[] args)
        throws Exception
    {
        // This is used to ensure that the required DKPRO_HOME environment variable is set.
        // Ensures that people can run the experiments even if they haven't read the setup
        // instructions first :)
        // Don't use this in real experiments! Read the documentation and set DKPRO_HOME as
        // explained there.
        DemoUtils.setDkproHome(WekaExternalResourceDemo.class.getSimpleName());

        ParameterSpace pSpace = getParameterSpace();
        WekaExternalResourceDemo experiment = new WekaExternalResourceDemo();
        experiment.runTrainTest(pSpace);
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace() throws ResourceInitializationException
    {
        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                PairTwentyNewsgroupsReader.class, PairTwentyNewsgroupsReader.PARAM_LISTFILE,
                listFilePathTrain, PairTwentyNewsgroupsReader.PARAM_LANGUAGE_CODE, languageCode);
        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
                PairTwentyNewsgroupsReader.class, PairTwentyNewsgroupsReader.PARAM_LISTFILE,
                listFilePathTest, PairTwentyNewsgroupsReader.PARAM_LANGUAGE_CODE, languageCode);
        dimReaders.put(DIM_READER_TEST, readerTest);

        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { SMO.class.getName() }));

        // Create the External Resource here:
        ExternalResourceDescription gstResource = ExternalResourceFactory
                .createExternalResourceDescription(CosineSimilarityResource.class,
                        CosineSimilarityResource.PARAM_NORMALIZATION,
                        NormalizationMode.L2.toString());

        Dimension<List<TcFeature<ExternalResourceDescription>>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                Arrays.asList(TcFeatureFactory.create(SimilarityPairFeatureExtractor.class,SimilarityPairFeatureExtractor.PARAM_TEXT_SIMILARITY_RESOURCE,
                        gstResource)));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_PAIR), dimFeatureSets,
                dimClassificationArgs);

        return pSpace;
    }

    // ##### TRAIN-TEST #####
    protected void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {

        ExperimentTrainTest batch = new ExperimentTrainTest("TwentyNewsgroupsTrainTest",
                WekaClassificationAdapter.class);
        batch.setPreprocessing(getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

        // Run
        Lab.getInstance().run(batch);
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }
}
