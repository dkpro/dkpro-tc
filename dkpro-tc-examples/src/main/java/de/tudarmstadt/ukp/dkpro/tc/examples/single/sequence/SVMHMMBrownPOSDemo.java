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
package de.tudarmstadt.ukp.dkpro.tc.examples.single.sequence;

import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.util.DemoUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfCharsUFE;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SparseFeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.ml.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.ml.task.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.BrownCorpusReader;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.SVMHMMAdapter;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.random.RandomSVMHMMAdapter;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.task.SVMHMMTestTask;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.util.OriginalTextHolderFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.writer.SVMHMMDataWriter;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.fit.component.NoOpAnnotator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

/**
 * Tests SVMhmm on POS tagging of one file in Brown corpus
 *
 * @author Ivan Habernal
 */
public class SVMHMMBrownPOSDemo
{

    public static final String corpusFilePathTrain = "src/main/resources/data/brown_tei";
    private static final int NUM_FOLDS = 10;

    public static Map<String, Object> getDimReaders(boolean trainTest)
    {
        // configure training and test data reader dimension
        Map<String, Object> results = new HashMap<>();
        results.put(Constants.DIM_READER_TRAIN, BrownCorpusReader.class);
        results.put(Constants.DIM_READER_TEST, BrownCorpusReader.class);

        if (trainTest) {
            results.put(Constants.DIM_READER_TRAIN_PARAMS,
                    Arrays.asList(BrownCorpusReader.PARAM_LANGUAGE,
                            "en", BrownCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                            BrownCorpusReader.PARAM_PATTERNS, "a01.xml"));
            results.put(Constants.DIM_READER_TEST_PARAMS,
                    Arrays.asList(BrownCorpusReader.PARAM_LANGUAGE,
                            "en", BrownCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                            BrownCorpusReader.PARAM_PATTERNS, "a02.xml"));
        }
        else {
            results.put(Constants.DIM_READER_TRAIN_PARAMS,
                    Arrays.asList(BrownCorpusReader.PARAM_LANGUAGE,
                            "en", BrownCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                            BrownCorpusReader.PARAM_PATTERNS,
                            Arrays.asList(INCLUDE_PREFIX + "*.xml")));
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace(boolean trainTest)
    {
        // configure training and test data reader dimension
        Map<String, Object> dimReaders = getDimReaders(trainTest);

        // no parameters needed for now... see TwentyNewsgroupDemo for multiple parametrization
        // or pipeline
        Dimension<List<Object>> dimPipelineParameters = Dimension
                .create(Constants.DIM_PIPELINE_PARAMS, Arrays.asList());

        // try different parametrization of C
        Dimension<Double> dimClassificationArgsC = Dimension.create(
                SVMHMMTestTask.PARAM_C, 1.0, 5.0);
        //                SVMHMMTestTask.PARAM_C, 1.0, 5.0, 10.0);

        // various orders of dependencies of transitions in HMM (max 3)
        Dimension<Integer> dimClassificationArgsT = Dimension.create(
                SVMHMMTestTask.PARAM_ORDER_T, 1);
        //                SVMHMMTestTask.PARAM_ORDER_T, 1, 2, 3);

        // various orders of dependencies of emissions in HMM (max 1)
        Dimension<Integer> dimClassificationArgsE = Dimension.create(
                SVMHMMTestTask.PARAM_ORDER_E, 0);
        //                SVMHMMTestTask.PARAM_ORDER_E, 0, 1);

        // feature extractors
        Dimension<List<String>> dimFeatureSets = Dimension.create(Constants.DIM_FEATURE_SET,
                Arrays.asList(new String[] { NrOfCharsUFE.class.getName(),
                        OriginalTextHolderFeatureExtractor.class.getName() }));

        return new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(Constants.DIM_DATA_WRITER, SVMHMMDataWriter.class.getName()),
                Dimension.create(Constants.DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
                Dimension.create(Constants.DIM_FEATURE_MODE, Constants.FM_SEQUENCE),
                Dimension.create(Constants.DIM_FEATURE_STORE, SparseFeatureStore.class.getName()),
                dimPipelineParameters, dimFeatureSets,
                dimClassificationArgsC,
                dimClassificationArgsT,
                dimClassificationArgsE
        );
    }

    protected void runCrossValidation(ParameterSpace pSpace,
            TCMachineLearningAdapter machineLearningAdapter)
            throws Exception
    {
        final BatchTaskCrossValidation batch = new BatchTaskCrossValidation("BrownCVBatchTask",
                machineLearningAdapter, createEngineDescription(NoOpAnnotator.class),
                NUM_FOLDS);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(BatchTask.ExecutionPolicy.RUN_AGAIN);

        // Run
        Lab.getInstance().run(batch);
    }

    protected void runTrainTest(ParameterSpace pSpace,
            TCMachineLearningAdapter machineLearningAdapter)
            throws Exception
    {
        final BatchTaskTrainTest batch = new BatchTaskTrainTest("BrownTrainTestBatchTask",
                machineLearningAdapter, createEngineDescription(NoOpAnnotator.class));
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(BatchTask.ExecutionPolicy.RUN_AGAIN);

        // Run
        Lab.getInstance().run(batch);
    }

    public static void main(String[] args)
    {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);

        // This is used to ensure that the required DKPRO_HOME environment variable is set.
        // Ensures that people can run the experiments even if they haven't read the setup instructions first :)
        // Don't use this in real experiments! Read the documentation and set DKPRO_HOME as explained there.
        DemoUtils.setDkproHome(SVMHMMBrownPOSDemo.class.getSimpleName());

        // run cross-validation first
        try {
            ParameterSpace pSpace = getParameterSpace(false);

            SVMHMMBrownPOSDemo experiment = new SVMHMMBrownPOSDemo();
            // run with a random labeler
            experiment.runCrossValidation(pSpace, new RandomSVMHMMAdapter());
            // run with an actual SVMHMM implementation
            experiment.runCrossValidation(pSpace, new SVMHMMAdapter());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // run train test
        try {
            ParameterSpace pSpace = getParameterSpace(true);

            SVMHMMBrownPOSDemo experiment = new SVMHMMBrownPOSDemo();
            // run with a random labeler
            experiment.runTrainTest(pSpace, new RandomSVMHMMAdapter());
            // run with an actual SVMHMM implementation
            experiment.runTrainTest(pSpace, new SVMHMMAdapter());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
