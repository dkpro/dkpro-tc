package de.tudarmstadt.ukp.dkpro.tc.groovyexamples.multi.document;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.resource.ResourceInitializationException

import weka.attributeSelection.InfoGainAttributeEval
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.multilabel.BR
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import de.tudarmstadt.ukp.dkpro.lab.Lab
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy
import de.tudarmstadt.ukp.dkpro.tc.core.Constants
import de.tudarmstadt.ukp.dkpro.tc.examples.io.ReutersCorpusReader
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.FrequencyDistributionNGramDFE
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.FrequencyDistributionNGramFeatureExtractorBase
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchCrossValidationReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchOutcomeIDReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.ClassificationReport
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.MekaDataWriter

/**
 * Groovy-Version of the ReutersTextClassificationExperiment
 *
 * Base class for running for Train-Test and CrossValidation experiments using the pre-configured BatchTask setups.
 *
 * @author Oliver Ferschke
 * @author daxenberger
 *
 */
public class ReutersDemo implements Constants {

    // === PARAMETERS===========================================================

    def experimentName = "ReutersTextClassification";
    def corpusFilePathTrain = "src/main/resources/data/reuters/training";
    def corpusFilePathTest = "src/main/resources/data/reuters/test";
    def goldLabelFilePath = "src/main/resources/data/reuters/cats.txt";
    def languageCode = "en";
    def numFolds = 2;
    def threshold = "0.5";

    // === DIMENSIONS===========================================================

    def dimReaders = Dimension.createBundle("readers", [
        readerTest: ReutersCorpusReader.class,
        readerTestParams: [
            ReutersCorpusReader.PARAM_SOURCE_LOCATION,
            corpusFilePathTest,
            ReutersCorpusReader.PARAM_GOLD_LABEL_FILE,
            goldLabelFilePath,
            ReutersCorpusReader.PARAM_LANGUAGE,
            languageCode,
            ReutersCorpusReader.PARAM_PATTERNS,
            ReutersCorpusReader.INCLUDE_PREFIX + "*.txt"
        ],
        readerTrain: ReutersCorpusReader.class,
        readerTrainParams: [
            ReutersCorpusReader.PARAM_SOURCE_LOCATION,
            corpusFilePathTrain,
            ReutersCorpusReader.PARAM_GOLD_LABEL_FILE,
            goldLabelFilePath,
            ReutersCorpusReader.PARAM_LANGUAGE,
            languageCode,
            ReutersCorpusReader.PARAM_PATTERNS,
            ReutersCorpusReader.INCLUDE_PREFIX + "*.txt"
        ]
    ]);

    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_MULTI_LABEL);
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT);
    def dimThreshold = Dimension.create(DIM_BIPARTITION_THRESHOLD, threshold);
    def dimDataWriter = Dimension.create(DIM_DATA_WRITER, MekaDataWriter.class.name);

    def dimClassificationArgs =
    Dimension.create(
    DIM_CLASSIFICATION_ARGS,
    [
        BR.class.name,
        "-W",
        NaiveBayes.class.name
    ]);

    def dimFeatureSelection = Dimension.createBundle("featureSelection", [
        labelTransformationMethod: "BinaryRelevanceAttributeEvaluator",
        attributeEvaluator: [
            InfoGainAttributeEval.class.name
        ],
        numLabelsToKeep: 100,
        applySelection: true
    ]);


    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET,
    [
        NrOfTokensDFE.class.name,
        FrequencyDistributionNGramDFE.class.name
    ]
    );

    def dimPipelineParameters = Dimension.create(
    DIM_PIPELINE_PARAMS,
    [
        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K,
        "500",
        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MIN_N,
        1,
        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MAX_N,
        3
    ],
    [
        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K,
        "1000",
        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MIN_N,
        1,
        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MAX_N,
        3
    ]
    );

    // === Experiments =========================================================

    /**
     * Crossvalidation setting
     *
     * @throws Exception
     */
    protected void runCrossValidation() throws Exception
    {

        BatchTaskCrossValidation batchTask = [
            experimentName: experimentName + "-CV-Groovy",
            // we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
            type: "Evaluation-"+ experimentName +"-CV-Groovy",
            preprocessingPipeline:	getPreprocessing(),
            innerReport: ClassificationReport.class,
            parameterSpace : [
                dimReaders,
                dimFeatureMode,
                dimLearningMode,
                dimDataWriter,
                dimThreshold,
                dimClassificationArgs,
                dimFeatureSelection,
                dimFeatureSets,
                dimPipelineParameters
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [BatchCrossValidationReport],
            numFolds: numFolds];

        Lab.getInstance().run(batchTask);
    }
    /**
     * TrainTest Setting
     *
     * @throws Exception
     */
    protected void runTrainTest() throws Exception
    {

        BatchTaskTrainTest batchTask = [
            experimentName: experimentName + "-TrainTest-Groovy",
            // we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
            type: "Evaluation-"+ experimentName +"-TrainTest-Groovy",
            preprocessingPipeline:	getPreprocessing(),
            innerReport: ClassificationReport.class,
            parameterSpace : [
                dimReaders,
                dimLearningMode,
                dimFeatureMode,
                dimDataWriter,
                dimThreshold,
                dimClassificationArgs,
                dimFeatureSelection,
                dimFeatureSets,
                dimPipelineParameters
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [
                BatchTrainTestReport,
                BatchOutcomeIDReport]
        ];

        // Run
        Lab.getInstance().run(batchTask);
    }

    private AnalysisEngineDescription getPreprocessing()
    throws ResourceInitializationException
    {
        return createEngineDescription(
        createEngineDescription(BreakIteratorSegmenter.class),
        createEngineDescription(OpenNlpPosTagger.class)
        );
    }

    public static void main(String[] args)
    {
        new ReutersDemo().runTrainTest();
        new ReutersDemo().runCrossValidation();
    }

}
