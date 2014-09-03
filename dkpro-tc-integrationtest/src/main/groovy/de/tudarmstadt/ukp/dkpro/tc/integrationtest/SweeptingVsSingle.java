/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.tc.integrationtest;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.functions.SMO;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.TwentyNewsgroupsCorpusReader;
import de.tudarmstadt.ukp.dkpro.tc.examples.util.DemoUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.NGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.ml.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchRuntimeReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaClassificationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaFeatureValuesReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * Test case for issue 100.
 * 
 */
public class SweeptingVsSingle
    implements Constants
{
    public static final String LANGUAGE_CODE = "en";

    public static final int NUM_FOLDS = 3;

    // public static final String corpusFilePathTrain =
    // "src/main/resources/data/twentynewsgroups/train";
    // public static final String corpusFilePathTest =
    // "src/main/resources/data/twentynewsgroups/test";
    public static final String corpusFilePathTrain = "/Users/zesch/Downloads/20news-bydate-train";
    public static final String corpusFilePathTest = "/Users/zesch/Downloads/20news-bydate-test";

    @SuppressWarnings("unchecked")
    public static void main(String[] args)
        throws Exception
    {

        // This is used to ensure that the required DKPRO_HOME environment variable is set.
        // Ensures that people can run the experiments even if they haven't read the setup
        // instructions first :)
        // Don't use this in real experiments! Read the documentation and set DKPRO_HOME as
        // explained there.
        DemoUtils.setDkproHome(SweeptingVsSingle.class.getSimpleName());

        // for sweeping
        Dimension<List<Object>> dimPipelineParametersAll = Dimension.create(
                DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] {
                        NGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, 500,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 }),
                Arrays.asList(new Object[] {
                        NGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, 1000,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 }),
                Arrays.asList(new Object[] {
                        NGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, 5000,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 }),
                Arrays.asList(new Object[] {
                        NGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, 10000,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 })

                );

        Dimension<List<Object>> dimPipelineParameters500 = Dimension.create(
                DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] {
                        NGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, 500,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 })
                );

        Dimension<List<Object>> dimPipelineParameters1000 = Dimension.create(
                DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] {
                        NGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, 1000,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 })
                );

        Dimension<List<Object>> dimPipelineParameters5000 = Dimension.create(
                DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] {
                        NGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, 5000,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 })
                );

        Dimension<List<Object>> dimPipelineParameters10000 = Dimension.create(
                DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] {
                        NGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, 10000,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 })
                );

        ParameterSpace pSpaceAll = getParameterSpace(dimPipelineParametersAll);
        ParameterSpace pSpace500 = getParameterSpace(dimPipelineParameters500);
        ParameterSpace pSpace1000 = getParameterSpace(dimPipelineParameters1000);
        ParameterSpace pSpace5000 = getParameterSpace(dimPipelineParameters5000);
        ParameterSpace pSpace10000 = getParameterSpace(dimPipelineParameters10000);

        // with sweeping
        SweeptingVsSingle experimentAll = new SweeptingVsSingle();
        experimentAll.runTrainTest(pSpaceAll);

        // single experiments
        SweeptingVsSingle experiment500 = new SweeptingVsSingle();
        experiment500.runTrainTest(pSpace500);
        SweeptingVsSingle experiment1000 = new SweeptingVsSingle();
        experiment1000.runTrainTest(pSpace1000);
        SweeptingVsSingle experiment5000 = new SweeptingVsSingle();
        experiment5000.runTrainTest(pSpace5000);
        SweeptingVsSingle experiment10000 = new SweeptingVsSingle();
        experiment10000.runTrainTest(pSpace10000);
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace(Dimension<List<Object>> dimPipelineParameters)
    {
        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, TwentyNewsgroupsCorpusReader.class);
        dimReaders
                .put(
                        DIM_READER_TRAIN_PARAMS,
                        Arrays.asList(TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION,
                                corpusFilePathTrain,
                                TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                                TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
                                Arrays.asList(TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX
                                        + "*/*.txt")));
        dimReaders.put(DIM_READER_TEST, TwentyNewsgroupsCorpusReader.class);
        dimReaders.put(
                DIM_READER_TEST_PARAMS,
                Arrays.asList(TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION,
                        corpusFilePathTest, TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE,
                        LANGUAGE_CODE, TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
                        TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"));

        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { SMO.class.getName() }));

        Dimension<List<String>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                Arrays.asList(new String[] { NrOfTokensDFE.class.getName(),
                        LuceneNGramDFE.class.getName() }));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_DATA_WRITER, WekaDataWriter.class.getName()),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL), Dimension.create(
                        DIM_FEATURE_MODE, FM_DOCUMENT), dimPipelineParameters, dimFeatureSets,
                dimClassificationArgs);

        return pSpace;
    }

    // ##### TRAIN-TEST #####
    protected void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {

        BatchTaskTrainTest batch = new BatchTaskTrainTest("TwentyNewsgroupsTrainTest",
                WekaAdapter.getInstance(),
                getPreprocessing());
        batch.addInnerReport(WekaClassificationReport.class);
        // add a second report to TestTask which creates a report about average feature values for
        // each outcome label
        batch.addInnerReport(WekaFeatureValuesReport.class);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(WekaBatchTrainTestReport.class);
        batch.addReport(WekaBatchOutcomeIDReport.class);
        batch.addReport(WekaBatchRuntimeReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {

        return createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE,
                        LANGUAGE_CODE));
    }
}
