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

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.TwentyNewsgroupsCorpusReader;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfCharsDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensPerSentenceDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.FrequencyDistributionNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * This demo is to show-case a somewhat more complex experiment setup for a single-label experiment,
 * including parameter sweeping (6 different combinations), (Weka) classifier configuration, and
 * Feature Selection.
 * 
 */
public class ComplexConfigurationSingleDemo
    implements Constants
{
    private static final String CORPUS_FILEPATH_TRAIN = "src/main/resources/data/twentynewsgroups/bydate-train";
    private static final String COPRUS_FILEPATH_TEST = "src/main/resources/data/twentynewsgroups/bydate-test";
    private static final String LANGUAGE_CODE = "en";
    private static final String EXPERIMENT_NAME = "TwentyNewsgroupsComplex";

    /**
     * Starts the experiment.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args)
        throws Exception
    {
        ParameterSpace pSpace = getParameterSpace();
        ComplexConfigurationSingleDemo experiment = new ComplexConfigurationSingleDemo();
        experiment.runTrainTest(pSpace);
    }

    @SuppressWarnings("unchecked")
    private static ParameterSpace getParameterSpace()
    {
        // configure training and test data reader dimension
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, TwentyNewsgroupsCorpusReader.class);
        dimReaders
                .put(
                        DIM_READER_TRAIN_PARAMS,
                        Arrays.asList(TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION,
                                CORPUS_FILEPATH_TRAIN,
                                TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                                TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
                                Arrays.asList(TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX
                                        + "*/*.txt")));
        dimReaders.put(DIM_READER_TEST, TwentyNewsgroupsCorpusReader.class);
        dimReaders.put(
                DIM_READER_TEST_PARAMS,
                Arrays.asList(TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION,
                        COPRUS_FILEPATH_TEST, TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE,
                        LANGUAGE_CODE, TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
                        TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"));

        // We configure 3 different classifiers, which will be swept, each with a special
        // configuration.
        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                // "-C": complexity, "-K": kernel
                asList(new String[] { SMO.class.getName(), "-C", "1.0",
                        "-K", PolyKernel.class.getName() + " " + "-C -1 -E 2" }),
                // "-I": number of trees
                asList(new String[] { RandomForest.class.getName(), "-I", "5" }),
                // "W": base classifier
                asList(new String[] { Bagging.class.getName(), "-I", "2",
                        "-W", J48.class.getName(), "--", "-C", "0.5", "-M", "2" }));

        // We configure 2 sets of feature extractors, one consisting of 3 extractors, and one with
        // only 1
        Dimension<List<String>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                asList(new String[] { NrOfTokensPerSentenceDFE.class.getName(), NrOfCharsDFE
                        .class.getName(),
                        LuceneNGramDFE.class.getName() }),
                asList(new String[] { LuceneNGramDFE.class.getName() }));

        // parameters to configure feature extractors
        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                DIM_PIPELINE_PARAMS,
                asList(new Object[] {
                        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K,
                        "500", FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 }));

        // single-label feature selection (Weka specific options), reduces the feature set to 10
        Map<String, Object> dimFeatureSelection = new HashMap<String, Object>();
        dimFeatureSelection.put(DIM_FEATURE_SEARCHER_ARGS,
                asList(new String[] { Ranker.class.getName(), "-N", "10" }));
        dimFeatureSelection.put(DIM_ATTRIBUTE_EVALUATOR_ARGS,
                asList(new String[] { InfoGainAttributeEval.class.getName() }));
        dimFeatureSelection.put(DIM_APPLY_FEATURE_SELECTION, true);

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_DATA_WRITER, WekaDataWriter.class.getName()),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL), Dimension.create(
                        DIM_FEATURE_MODE, FM_DOCUMENT), dimPipelineParameters, dimFeatureSets,
                dimClassificationArgs, Dimension.createBundle("featureSelection",
                        dimFeatureSelection));

        return pSpace;
    }

    // ##### TRAIN-TEST #####
    protected void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskTrainTest batch = new BatchTaskTrainTest(EXPERIMENT_NAME,
                getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.addReport(BatchTrainTestReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class,
                BreakIteratorSegmenter.PARAM_LANGUAGE, LANGUAGE_CODE));
    }
}
