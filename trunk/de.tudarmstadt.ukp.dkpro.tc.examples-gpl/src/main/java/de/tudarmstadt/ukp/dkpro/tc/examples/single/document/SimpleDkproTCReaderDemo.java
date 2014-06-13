package de.tudarmstadt.ukp.dkpro.tc.examples.single.document;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.bayes.NaiveBayes;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.SimpleDkproTCReader;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.FrequencyDistributionNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchRuntimeReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.ClassificationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * This demo uses the {@link SimpleDkproTCReader}.
 */

public class SimpleDkproTCReaderDemo
    implements Constants
{
    public static final String LANGUAGE_CODE = "en";
    public static final int NUM_FOLDS = 2;
    public static final String FILEPATH_TRAIN = "src/main/resources/data/simple_reader/train";
    public static final String FILEPATH_GOLD_LABELS = "src/main/resources/data/simple_reader/gold_labels.txt";

    public static void main(String[] args)
        throws Exception
    {
        SimpleDkproTCReaderDemo demo = new SimpleDkproTCReaderDemo();
        demo.runCrossValidation(getParameterSpace());
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskCrossValidation batch = new BatchTaskCrossValidation(
                "SimpleReaderDemoCV", getPreprocessing(), NUM_FOLDS);
        batch.addInnerReport(ClassificationReport.class);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchCrossValidationReport.class);
        batch.addReport(BatchRuntimeReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    public static ParameterSpace getParameterSpace()
    {
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, SimpleDkproTCReader.class);
        dimReaders.put(
                DIM_READER_TRAIN_PARAMS,
                Arrays.asList(new Object[] {
                        SimpleDkproTCReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                        SimpleDkproTCReader.PARAM_GOLD_LABEL_FILE,
                        FILEPATH_GOLD_LABELS,
                        SimpleDkproTCReader.PARAM_SOURCE_LOCATION,
                        FILEPATH_TRAIN, SimpleDkproTCReader.PARAM_PATTERNS,
                        INCLUDE_PREFIX + "*.txt" }));

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimClassificationArgs = Dimension.create(
                DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { NaiveBayes.class.getName() }));

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                asList(new String[] { LuceneNGramDFE.class.getName() }));

        // parameters to configure feature extractors
        @SuppressWarnings("unchecked")
        Dimension<List<Object>> dimPipelineParameters = Dimension
                .create(DIM_PIPELINE_PARAMS,
                        asList(new Object[] {
                                FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K,
                                "100",
                                FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MIN_N,
                                1,
                                FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MAX_N,
                                3 }));

        @SuppressWarnings("unchecked")
        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle(
                "readers", dimReaders), Dimension.create(DIM_DATA_WRITER,
                WekaDataWriter.class.getName()), Dimension.create(
                DIM_LEARNING_MODE, LM_SINGLE_LABEL), Dimension.create(
                DIM_FEATURE_MODE, FM_DOCUMENT), dimPipelineParameters,
                dimFeatureSets, dimClassificationArgs);

        return pSpace;
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(createEngineDescription(
                BreakIteratorSegmenter.class,
                BreakIteratorSegmenter.PARAM_LANGUAGE, LANGUAGE_CODE));
    }
}
