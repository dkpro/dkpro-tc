package de.tudarmstadt.ukp.dkpro.tc.examples.multi.document;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
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
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.ReutersCorpusReader;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.FrequencyDistributionNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.MekaDataWriter;

public class ComplexConfigurationMultiDemo
    implements Constants
{

    public static final String EXPERIMENT_NAME = "ReutersTextClassification";
    public static final String FILEPATH_TRAIN = "src/main/resources/data/reuters/training";
    public static final String FILEPATH_TEST = "src/main/resources/data/reuters/test";
    public static final String FILEPATH_GOLD_LABELS = "src/main/resources/data/reuters/cats.txt";
    public static final String LANGUAGE_CODE = "en";
    public static final int NUM_FOLDS = 3;
    public static final String BIPARTITION_THRESHOLD = "0.5";

    public static void main(String[] args)
        throws Exception
    {
        ParameterSpace pSpace = getParameterSpace();
        ComplexConfigurationMultiDemo experiment = new ComplexConfigurationMultiDemo();
        experiment.runCrossValidation(pSpace);
        experiment.runTrainTest(pSpace);
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace()
    {
        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, ReutersCorpusReader.class);
        dimReaders.put(DIM_READER_TRAIN_PARAMS,
                Arrays.asList(ReutersCorpusReader.PARAM_SOURCE_LOCATION,
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
                Arrays.asList(ReutersCorpusReader.PARAM_SOURCE_LOCATION,
                        FILEPATH_TEST,
                        ReutersCorpusReader.PARAM_GOLD_LABEL_FILE,
                        FILEPATH_GOLD_LABELS,
                        ReutersCorpusReader.PARAM_LANGUAGE,
                        LANGUAGE_CODE,
                        ReutersCorpusReader.PARAM_PATTERNS,
                        ReutersCorpusReader.INCLUDE_PREFIX + "*.txt"));

        Dimension<List<String>> dimClassificationArgs = Dimension
                .create(DIM_CLASSIFICATION_ARGS,
                        Arrays.asList(new String[] { BR.class.getName(), "-W",
                                NaiveBayes.class.getName() }),
	            		Arrays.asList(new String[] { CCq.class.getName(), "P", "0.9" }),
	            		Arrays.asList(new String[] { PSUpdateable.class.getName(),
	            				"B", "900", "S", "9" }));

        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] {
                        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K,
                        "600", FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 }));

        Dimension<List<String>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                Arrays.asList(new String[] { NrOfTokensDFE.class.getName(),
                        LuceneNGramDFE.class.getName() }),
                Arrays.asList(new String[] { LuceneNGramDFE.class.getName() }));

        Map<String, Object> dimFeatureSelection = new HashMap<String, Object>();
        dimFeatureSelection.put(DIM_LABEL_TRANSFORMATION_METHOD,
                "BinaryRelevanceAttributeEvaluator");
        dimFeatureSelection.put(DIM_ATTRIBUTE_EVALUATOR_ARGS,
                Arrays.asList(new String[] { InfoGainAttributeEval.class.getName() }));
        dimFeatureSelection.put(DIM_NUM_LABELS_TO_KEEP, 300);
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

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskCrossValidation batch = new BatchTaskCrossValidation(EXPERIMENT_NAME
                + "-CrossValidation",
                getPreprocessing(), NUM_FOLDS);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchCrossValidationReport.class);

        // Run
        Lab.getInstance().run(batch);
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

        return createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE,
                        LANGUAGE_CODE));
    }
}
