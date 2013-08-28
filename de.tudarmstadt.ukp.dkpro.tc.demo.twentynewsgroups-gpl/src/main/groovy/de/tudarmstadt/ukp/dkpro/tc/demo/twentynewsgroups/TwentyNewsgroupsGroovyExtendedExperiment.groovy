package de.tudarmstadt.ukp.dkpro.tc.demo.twentynewsgroups;

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.fit.factory.AnalysisEngineFactory
import org.apache.uima.resource.ResourceInitializationException

import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.functions.SMO
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import de.tudarmstadt.ukp.dkpro.lab.Lab
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask
import de.tudarmstadt.ukp.dkpro.tc.demo.twentynewsgroups.io.TwentyNewsgroupsCorpusReader
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensFeatureExtractor
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchOutcomeIDReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.CVBatchReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.CVReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.OutcomeIDReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.TrainTestReport
import de.tudarmstadt.ukp.dkpro.tc.weka.task.CrossValidationTask
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
 * @author Oliver Ferschke
 */
public class TwentyNewsgroupsGroovyExtendedExperiment {

    // === PARAMETERS===========================================================

    def corpusFilePathTrain = "src/main/resources/data/bydate-train";
    def corpusFilePathTest  ="src/main/resources/data/bydate-test";
    def languageCode = "en";

    // === DIMENSIONS===========================================================

    def dimReaderTest = Dimension.createBundle("readerTest", [
        readerTest: TwentyNewsgroupsCorpusReader.class,
        readerTestParams: [
            "sourceLocation",
            corpusFilePathTest,
            "language",
            languageCode,
            "patterns",
            TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"
        ]
    ]);

    def dimReaderTrain = Dimension.createBundle("readerTrain", [
        readerTrain: TwentyNewsgroupsCorpusReader.class,
        readerTrainParams: [
            "sourceLocation",
            corpusFilePathTrain,
            "language",
            languageCode,
            "patterns",
            TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"
        ]
    ]);

    def dimFolds = Dimension.create("folds", 2);
    def dimMultiLabel = Dimension.create("multiLabel", false);

    //UIMA parameters for FE configuration
    def dimPipelineParameters = Dimension.create(
    "pipelineParameters",
    [
        "TopK",
        "500",
        NGramFeatureExtractor.PARAM_NGRAM_MIN_N,
        1,
        NGramFeatureExtractor.PARAM_NGRAM_MAX_N,
        3
    ],
    [
        "TopK",
        "1000",
        NGramFeatureExtractor.PARAM_NGRAM_MIN_N,
        1,
        NGramFeatureExtractor.PARAM_NGRAM_MAX_N,
        3
    ]);


    def dimClassificationArgs =
    Dimension.create("classificationArguments",
    [NaiveBayes.class.name],
    [SMO.class.name]);

    def dimFeatureSets = Dimension.create(
    "featureSet",
    [
        NrOfTokensFeatureExtractor.class.name,
        NGramFeatureExtractor.class.name
    ]
    );

    // === Experiments =========================================================

    /**
     * Crossvalidation setting. Not adapted for the new CV setup yet.
     *
     * @throws Exception
     */
    @Deprecated
    protected void runCrossValidation() throws Exception
    {
        /*
         * Define (instantiate) tasks
         */

        PreprocessTask preprocessTask = [
            aggregate:getPreprocessing(),
            type: "Preprocessing-TwentyNewsgroupsCV"
        ];

        MetaInfoTask metaTask = [
            type: "MetaInfoTask-TwentyNewsgroupsCV",
        ];

        ExtractFeaturesTask featureExtractionTask = [
            addInstanceId: false,
            dataWriter:         WekaDataWriter.class.name,
            type: "FeatureExtraction-TwentyNewsgroupsCV",
        ];

        CrossValidationTask cvTask = [
            type: "CVTask-TwentyNewsgroupsCV",
            reports: [CVReport]];


        /*
         * Wire tasks
         */
        metaTask.addImport(preprocessTask, PreprocessTask.OUTPUT_KEY_TRAIN, MetaInfoTask.INPUT_KEY);
        featureExtractionTask.addImport(preprocessTask, PreprocessTask.OUTPUT_KEY_TRAIN, MetaInfoTask.INPUT_KEY);
        featureExtractionTask.addImport(metaTask, MetaInfoTask.META_KEY, MetaInfoTask.META_KEY);
        cvTask.addImport(metaTask, MetaInfoTask.META_KEY, MetaInfoTask.META_KEY);
        cvTask.addImport(featureExtractionTask, ExtractFeaturesTask.OUTPUT_KEY, CrossValidationTask.INPUT_KEY);


        /*
         *	Wrap wired tasks in batch task
         */

        BatchTask batchTask = [
            type: "Evaluation-TwentyNewsgroups-CV",
            parameterSpace : [
                dimReaderTrain,
                dimFolds,
                dimMultiLabel,
                dimClassificationArgs,
                dimFeatureSets,
                dimPipelineParameters
            ],
            tasks:           [
                preprocessTask,
                metaTask,
                featureExtractionTask,
                cvTask
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [CVBatchReport]];

        /*
         * Run
         */
        Lab.getInstance().run(batchTask);
    }

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
            aggregate:getPreprocessing(),
            type: "Preprocessing-TwentyNewsgroups-Train",
            isTesting: false
        ];

        PreprocessTask preprocessTaskTest = [
            aggregate:getPreprocessing(),
            type: "Preprocessing-TwentyNewsgroups-Test",
            isTesting: true
        ];

        MetaInfoTask metaTask = [
            type: "MetaInfoTask-TwentyNewsgroups-TrainTest",
        ];

        ExtractFeaturesTask featuresTrainTask = [
            addInstanceId: true,
            dataWriter:         WekaDataWriter.class.name,
            type: "FeatureExtraction-TwentyNewsgroups-Train",
            isTesting: false
        ];

        ExtractFeaturesTask featuresTestTask = [
            addInstanceId: true,
            dataWriter:         WekaDataWriter.class.name,
            type: "FeatureExtraction-TwentyNewsgroups-Test",
            isTesting: true
        ];

        TestTask testTask = [
            type:"TestTask.TwentyNewsgroups",
            reports: [
                TrainTestReport,
                OutcomeIDReport]
        ];


        /*
         * Wire tasks
         */
        metaTask.addImport(preprocessTaskTrain, PreprocessTask.OUTPUT_KEY_TRAIN, MetaInfoTask.INPUT_KEY);
        featuresTrainTask.addImport(preprocessTaskTrain, PreprocessTask.OUTPUT_KEY_TRAIN, ExtractFeaturesTask.INPUT_KEY);
        featuresTrainTask.addImport(metaTask, MetaInfoTask.META_KEY, MetaInfoTask.META_KEY);
        featuresTestTask.addImport(preprocessTaskTest, PreprocessTask.OUTPUT_KEY_TEST, ExtractFeaturesTask.INPUT_KEY);
        featuresTestTask.addImport(metaTask, MetaInfoTask.META_KEY, MetaInfoTask.META_KEY);
        testTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY, TestTask.INPUT_KEY_TRAIN);
        testTask.addImport(featuresTestTask, ExtractFeaturesTask.OUTPUT_KEY, TestTask.INPUT_KEY_TEST);

        /*
         *	Wrap wired tasks in batch task
         */

        BatchTask batchTask = [
            type: "Evaluation-TwentyNewsgroups-TrainTest",
            parameterSpace : [
                dimReaderTrain,
                dimReaderTest,
                dimFolds,
                dimMultiLabel,
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
        ];

        // Run
        Lab.getInstance().run(batchTask);
    }

    private AnalysisEngineDescription getPreprocessing()
    throws ResourceInitializationException
    {
        return AnalysisEngineFactory.createEngineDescription(
        AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter),
        AnalysisEngineFactory.createEngineDescription(OpenNlpPosTagger)
        );
    }

    public static void main(String[] args)
    {
        new TwentyNewsgroupsGroovyExtendedExperiment().runCrossValidation();
        new TwentyNewsgroupsGroovyExtendedExperiment().runTrainTest();
    }

}
