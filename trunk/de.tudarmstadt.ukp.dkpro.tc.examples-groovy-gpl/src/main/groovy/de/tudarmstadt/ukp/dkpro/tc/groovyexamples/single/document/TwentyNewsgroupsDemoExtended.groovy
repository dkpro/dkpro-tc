package de.tudarmstadt.ukp.dkpro.tc.groovyexamples.single.document

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.resource.ResourceInitializationException

import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.functions.SMO
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import de.tudarmstadt.ukp.dkpro.lab.Lab
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy
import de.tudarmstadt.ukp.dkpro.tc.core.Constants
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask
import de.tudarmstadt.ukp.dkpro.tc.examples.io.TwentyNewsgroupsCorpusReader
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.FrequencyDistributionNGramFeatureExtractorBase
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchOutcomeIDReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.ClassificationReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.OutcomeIDReport
import de.tudarmstadt.ukp.dkpro.tc.weka.task.TestTask
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter

/**
 * Groovy-Version of the TwentyNewsgroupsExperiment
 *
 * The TwentyNewsgroupsGroovyExtendedExperiment does the same as TwentyNewsgroupsGroovyExperiment,
 * but it manually sets up the sub-tasks and builds a generic batch task.
 *
 * In TwentyNewsgroupsGroovyExperiment, this is done automatically in the BatchTaskCV and BatchTaskTrainTest,
 * which is more convenient, but less flexible.
 *
 * Currently only supports train-test setup.
 *
 * @author Oliver Ferschke
 * @author daxenberger
 */
public class TwentyNewsgroupsDemoExtended implements Constants{

    // === PARAMETERS===========================================================

    def corpusFilePathTrain = "src/main/resources/data/twentynewsgroups/bydate-train"
    def corpusFilePathTest  ="src/main/resources/data/twentynewsgroups/bydate-test"
    def languageCode = "en"

    // === DIMENSIONS===========================================================

    def dimReaders = Dimension.createBundle("readers", [
        readerTest: TwentyNewsgroupsCorpusReader.class,
        readerTestParams: [
            TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION,
            corpusFilePathTest,
            TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE,
            languageCode,
            TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
            TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"
        ],
        readerTrain: TwentyNewsgroupsCorpusReader.class,
        readerTrainParams: [
            TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION,
            corpusFilePathTrain,
            TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE,
            languageCode,
            TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
            TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"
        ]
    ])

    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL)
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT)
    def dimDataWriter = Dimension.create(DIM_DATA_WRITER, WekaDataWriter.class.name)

    //UIMA parameters for FE configuration
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
    ])


    def dimClassificationArgs =
    Dimension.create(DIM_CLASSIFICATION_ARGS,
    [NaiveBayes.class.name],
    [SMO.class.name])

    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET,
    [
        NrOfTokensDFE.class.name,
        LuceneNGramDFE.class.name
    ]
    )

    // === Experiments =========================================================

    /**
     * TrainTest Setting
     *
     * @throws Exception
     */
    protected void runTrainTest() throws Exception
    {
        /*
         * Define (instantiate) tasks
         */

        PreprocessTask preprocessTaskTrain = [
            preprocessingPipeline:getPreprocessing(),
            type: "Preprocessing-TwentyNewsgroups-Train",
            isTesting: false
        ]

        PreprocessTask preprocessTaskTest = [
            preprocessingPipeline:getPreprocessing(),
            type: "Preprocessing-TwentyNewsgroups-Test",
            isTesting: true
        ]

        MetaInfoTask metaTask = [
            type: "MetaInfoTask-TwentyNewsgroups-TrainTest",
        ]

        ExtractFeaturesTask featuresTrainTask = [
            type: "FeatureExtraction-TwentyNewsgroups-Train",
            isTesting: false
        ]

        ExtractFeaturesTask featuresTestTask = [
            type: "FeatureExtraction-TwentyNewsgroups-Test",
            isTesting: true
        ]

        TestTask testTask = [
            type:"TestTask.TwentyNewsgroups",
            reports: [
                ClassificationReport,
                OutcomeIDReport]
        ]


        /*
         * Wire tasks
         */
        metaTask.addImport(preprocessTaskTrain, PreprocessTask.OUTPUT_KEY_TRAIN, MetaInfoTask.INPUT_KEY)
        featuresTrainTask.addImport(preprocessTaskTrain, PreprocessTask.OUTPUT_KEY_TRAIN, ExtractFeaturesTask.INPUT_KEY)
        featuresTrainTask.addImport(metaTask, MetaInfoTask.META_KEY, MetaInfoTask.META_KEY)
        featuresTestTask.addImport(preprocessTaskTest, PreprocessTask.OUTPUT_KEY_TEST, ExtractFeaturesTask.INPUT_KEY)
        featuresTestTask.addImport(metaTask, MetaInfoTask.META_KEY, MetaInfoTask.META_KEY)
        testTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY, Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA)
        testTask.addImport(featuresTestTask, ExtractFeaturesTask.OUTPUT_KEY, Constants.TEST_TASK_INPUT_KEY_TEST_DATA)

        /*
         *	Wrap wired tasks in batch task
         */

        BatchTask batchTask = [
            type: "Evaluation-TwentyNewsgroups-TrainTest",
            parameterSpace : [
                dimReaders,
                dimLearningMode,
                dimFeatureMode,
                dimDataWriter,
                dimClassificationArgs,
                dimFeatureSets,
                dimPipelineParameters
            ],
            tasks:           [
                preprocessTaskTrain,
                preprocessTaskTest,
                metaTask,
                featuresTrainTask,
                featuresTestTask,
                testTask
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [
                BatchTrainTestReport,
                BatchOutcomeIDReport]
        ]

        // Run
        Lab.getInstance().run(batchTask)
    }

    private AnalysisEngineDescription getPreprocessing()
    throws ResourceInitializationException
    {
        return createEngineDescription(
        createEngineDescription(BreakIteratorSegmenter),
        createEngineDescription(OpenNlpPosTagger)
        )
    }

    public static void main(String[] args)
    {
        new TwentyNewsgroupsDemoExtended().runTrainTest()
    }

}
