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
package de.tudarmstadt.ukp.dkpro.tc.examples.multi.document;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.CCq;
import meka.classifiers.multilabel.PSUpdateable;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.classifiers.bayes.NaiveBayes;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.ReutersCorpusReader;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.FrequencyDistributionNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.MekaDataWriter;

/**
 * This demo is to show-case a somewhat more complex experiment setup for a multi-label experiment,
 * including parameter sweeping (6 different combinations), (Meka) classifier configuration, and
 * Feature Selection.
 * 
 */
public class ComplexConfigurationMultiDemo
    implements Constants
{

    private static final String EXPERIMENT_NAME = "ReutersTextClassificationComplex";
    private static final String FILEPATH_TRAIN = "src/main/resources/data/reuters/training";
    private static final String FILEPATH_TEST = "src/main/resources/data/reuters/test";
    private static final String FILEPATH_GOLD_LABELS = "src/main/resources/data/reuters/cats.txt";
    private static final String LANGUAGE_CODE = "en";
    private static final String BIPARTITION_THRESHOLD = "0.5";

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
        ComplexConfigurationMultiDemo experiment = new ComplexConfigurationMultiDemo();
        experiment.runTrainTest(pSpace);
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    private static ParameterSpace getParameterSpace()
    {
        // configure training and test data reader dimension
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, ReutersCorpusReader.class);
        dimReaders.put(DIM_READER_TRAIN_PARAMS,
                asList(ReutersCorpusReader.PARAM_SOURCE_LOCATION,
                        FILEPATH_TRAIN,
                        ReutersCorpusReader.PARAM_GOLD_LABEL_FILE,
                        FILEPATH_GOLD_LABELS,
                        ReutersCorpusReader.PARAM_LANGUAGE,
                        LANGUAGE_CODE,
                        ReutersCorpusReader.PARAM_PATTERNS,
                        ReutersCorpusReader.INCLUDE_PREFIX + "*.txt"));
        dimReaders.put(DIM_READER_TEST, ReutersCorpusReader.class);
        dimReaders.put(
                DIM_READER_TEST_PARAMS,
                asList(ReutersCorpusReader.PARAM_SOURCE_LOCATION,
                        FILEPATH_TEST,
                        ReutersCorpusReader.PARAM_GOLD_LABEL_FILE,
                        FILEPATH_GOLD_LABELS,
                        ReutersCorpusReader.PARAM_LANGUAGE,
                        LANGUAGE_CODE,
                        ReutersCorpusReader.PARAM_PATTERNS,
                        ReutersCorpusReader.INCLUDE_PREFIX + "*.txt"));

        // We configure 3 different classifiers, which will be swept, each with a special
        // configuration.
        Dimension<List<String>> dimClassificationArgs = Dimension
                .create(DIM_CLASSIFICATION_ARGS,
                        // Config1: "-W" is used to set a base classifer
                        asList(new String[] { BR.class.getName(), "-W",
                                NaiveBayes.class.getName() }),
                        // Config2: "-P" sets the downsampling ratio
                        asList(new String[] { CCq.class.getName(), "-P", "0.9" }),
                        // Config3: "-B": buffer size, "-S": max. num. of combs.
                        asList(new String[] { PSUpdateable.class.getName(),
                                "-B", "900", "-S", "9" }));

        // We configure 2 sets of feature extractors, one consisting of 2 extractors, and one with
        // only one
        Dimension<List<String>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                asList(new String[] { NrOfTokensDFE.class.getName(),
                        LuceneNGramDFE.class.getName() }),
                asList(new String[] { LuceneNGramDFE.class.getName() }));

        // parameters to configure feature extractors
        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                DIM_PIPELINE_PARAMS,
                asList(new Object[] {
                        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K,
                        "600", FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 }));

        // multi-label feature selection (Mulan specific options), reduces the feature set to 10
        Map<String, Object> dimFeatureSelection = new HashMap<String, Object>();
        dimFeatureSelection.put(DIM_LABEL_TRANSFORMATION_METHOD,
                "BinaryRelevanceAttributeEvaluator");
        dimFeatureSelection.put(DIM_ATTRIBUTE_EVALUATOR_ARGS,
                asList(new String[] { InfoGainAttributeEval.class.getName() }));
        dimFeatureSelection.put(DIM_NUM_LABELS_TO_KEEP, 10);
        dimFeatureSelection.put(DIM_APPLY_FEATURE_SELECTION, true);

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_DATA_WRITER, MekaDataWriter.class.getName()),
                Dimension.create(DIM_LEARNING_MODE, LM_MULTI_LABEL), Dimension.create(
                        DIM_FEATURE_MODE, FM_DOCUMENT), Dimension.create(
                        DIM_BIPARTITION_THRESHOLD, BIPARTITION_THRESHOLD), dimPipelineParameters,
                dimFeatureSets, dimClassificationArgs, Dimension.createBundle("featureSelection",
                        dimFeatureSelection));

        return pSpace;
    }

    // ##### TRAIN-TEST #####
    protected void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskTrainTest batch = new BatchTaskTrainTest(EXPERIMENT_NAME + "-TrainTest",
                getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchTrainTestReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {

        return createEngineDescription(createEngineDescription(OpenNlpSegmenter.class,
                OpenNlpSegmenter.PARAM_LANGUAGE, LANGUAGE_CODE));
    }
}
