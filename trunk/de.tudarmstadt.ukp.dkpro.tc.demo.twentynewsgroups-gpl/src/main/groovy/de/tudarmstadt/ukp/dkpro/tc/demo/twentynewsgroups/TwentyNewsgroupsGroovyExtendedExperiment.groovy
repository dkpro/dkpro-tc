package de.tudarmstadt.ukp.dkpro.tc.demo.twentynewsgroups;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.collection.CollectionReaderDescription
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

    def dimFolds = Dimension.create("folds", 2);
    def dimToLowerCase = Dimension.create("toLowerCase", true);
    def dimMultiLabel = Dimension.create("multiLabel", false);

    //UIMA parameters for FE configuration
    def dimPipelineParameters = Dimension.create(
    "pipelineParameters",
    [
        NGramFeatureExtractor.PARAM_NGRAM_MIN_N,
        1,
        NGramFeatureExtractor.PARAM_NGRAM_MAX_N,
        3
    ]);


    def dimClassificationArgs =
    Dimension.create("classificationArguments",
    [
        [NaiveBayes.class.name].toArray(),
        [SMO.class.name].toArray()
    ] as Object[]
    );

    def dimFeatureSets = Dimension.create(
    "featureSet",
    [
        [
            NrOfTokensFeatureExtractor.class.name,
            NGramFeatureExtractor.class.name
        ].toArray()
    ] as Object[]
    );

    def dimFeatureParameters = Dimension.create(
    "featureParameters",
    [
        [
            "TopK",
            "500"
        ].toArray(),
        [
            "TopK",
            "1000"
        ].toArray()
    ] as Object[]
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
            reader:getReaderDesc(corpusFilePathTrain,languageCode),
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
        metaTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY_TRAIN, preprocessTask.getType());
        featureExtractionTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY_TRAIN, preprocessTask.getType());
        featureExtractionTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());
        cvTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());
        cvTask.addImportLatest(CrossValidationTask.INPUT_KEY, ExtractFeaturesTask.OUTPUT_KEY, featureExtractionTask.getType());


        /*
         *	Wrap wired tasks in batch task
         */

        BatchTask batchTask = [
            type: "Evaluation-TwentyNewsgroups-CV",
            parameterSpace : [
                dimFolds,
                dimFeatureParameters,
                dimToLowerCase,
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
            reader:getReaderDesc(corpusFilePathTrain, languageCode),
            aggregate:getPreprocessing(),
            type: "Preprocessing-TwentyNewsgroups-Train",
            isTesting: false
        ];

        PreprocessTask preprocessTaskTest = [
            reader:getReaderDesc(corpusFilePathTest, languageCode),
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
        metaTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY_TRAIN, preprocessTaskTrain.getType());
        featuresTrainTask.addImportLatest(ExtractFeaturesTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY_TRAIN, preprocessTaskTrain.getType());
        featuresTrainTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());
        featuresTestTask.addImportLatest(ExtractFeaturesTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY_TEST, preprocessTaskTest.getType());
        featuresTestTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());
        testTask.addImportLatest(TestTask.INPUT_KEY_TRAIN, ExtractFeaturesTask.OUTPUT_KEY, featuresTrainTask.getType());
        testTask.addImportLatest(TestTask.INPUT_KEY_TEST, ExtractFeaturesTask.OUTPUT_KEY, featuresTestTask.getType());

        /*
         *	Wrap wired tasks in batch task
         */

        BatchTask batchTask = [
            type: "Evaluation-TwentyNewsgroups-TrainTest",
            parameterSpace : [
                dimFolds,
                dimFeatureParameters,
                dimToLowerCase,
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


    private CollectionReaderDescription getReaderDesc(String corpusFilePath, String language)
            throws ResourceInitializationException, IOException
    {
        return createReaderDescription(
        TwentyNewsgroupsCorpusReader,
        TwentyNewsgroupsCorpusReader.PARAM_PATH, corpusFilePath,
        TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, language,
        TwentyNewsgroupsCorpusReader.PARAM_PATTERNS, [
            TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"]
        );
    }

    private AnalysisEngineDescription getPreprocessing()
    throws ResourceInitializationException
    {
        return createEngineDescription(
        createEngineDescription(BreakIteratorSegmenter),
        createEngineDescription(OpenNlpPosTagger)
        );
    }

    public static void main(String[] args)
    {
        new TwentyNewsgroupsGroovyExtendedExperiment().runCrossValidation();
        new TwentyNewsgroupsGroovyExtendedExperiment().runTrainTest();
    }

}
