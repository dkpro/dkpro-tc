/*
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfCharsUFE;
import de.tudarmstadt.ukp.dkpro.tc.ml.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.BrownCorpusReader;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.SVMHMMAdapter;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.task.SVMHMMTestTask;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.util.OriginalTokenHolderFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.writer.SVMHMMDataWriter;
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
public class SVMHMMBrownPOSTest
{

    public static final String corpusFilePathTrain = "src/main/resources/brown_tei";
    private static final int NUM_FOLDS = 10;

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace(String featureMode, String learningMode)
    {
        // configure training and test data reader dimension
        Map<String, Object> dimReaders = new HashMap<>();
        dimReaders.put(Constants.DIM_READER_TRAIN, BrownCorpusReader.class);
        dimReaders.put(Constants.DIM_READER_TRAIN_PARAMS,
                Arrays.asList(BrownCorpusReader.PARAM_LANGUAGE,
                        "en", BrownCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                        BrownCorpusReader.PARAM_PATTERNS,
                        Arrays.asList(INCLUDE_PREFIX + "b15.xml")));

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
                        OriginalTokenHolderFeatureExtractor.class.getName() }));

        return new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(Constants.DIM_DATA_WRITER, SVMHMMDataWriter.class.getName()),
                Dimension.create(Constants.DIM_LEARNING_MODE, learningMode), Dimension.create(
                Constants.DIM_FEATURE_MODE, featureMode),
                dimPipelineParameters, dimFeatureSets,
                dimClassificationArgsC,
                dimClassificationArgsT,
                dimClassificationArgsE
        );
    }

    protected void runCrossValidation(ParameterSpace pSpace)
            throws Exception
    {

        BatchTaskCrossValidation batch = new BatchTaskCrossValidation("BrownCVBatchTask",
                new SVMHMMAdapter(), createEngineDescription(NoOpAnnotator.class),
                NUM_FOLDS);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(BatchTask.ExecutionPolicy.RUN_AGAIN);

        // Run
        Lab.getInstance().run(batch);
    }

    public static void main(String[] args)
    {
        //        Logger.getRootLogger();
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");

        try {
            ParameterSpace pSpace = getParameterSpace(Constants.FM_SEQUENCE,
                    Constants.LM_SINGLE_LABEL);

            SVMHMMBrownPOSTest experiment = new SVMHMMBrownPOSTest();
            experiment.runCrossValidation(pSpace);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
