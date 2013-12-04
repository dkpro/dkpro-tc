package de.tudarmstadt.ukp.dkpro.tc.demo.twentynewsgroups;

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
import de.tudarmstadt.ukp.dkpro.tc.demo.twentynewsgroups.io.TwentyNewsgroupsCorpusReader
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensFeatureExtractor
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchCrossValidationReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchOutcomeIDReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.TrainTestReport
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter

/**
 * Groovy-Version of the TwentyNewsgroupsExperiment
 *
 * The TwentyNewsgroupsGroovyExperiment does the same as TwentyNewsgroupsGroovyExtendedExperiment,
 * but it uses the {@link BatchTaskCrossValidation} and {@link BatchTaskTrainTest} to automatically wire the standard tasks for
 * a basic CV and TrainTest setup. This is more convenient, but less flexible.
 *
 * If you need to define a more complex experiment setup, look at TwentyNewsgroupsGroovyExtendedExperiment
 *
 * @author Oliver Ferschke
 */
public class TwentyNewsgroupsGroovyExperiment implements Constants {

    // === PARAMETERS===========================================================

    def experimentName = "TwentyNewsgroups";
    def corpusFilePathTrain = "src/main/resources/data/bydate-train";
    def corpusFilePathTest  ="src/main/resources/data/bydate-test";
    def languageCode = "en";
    def numFolds = 2;

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
            corpusFilePathTest,
            TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE,
            languageCode,
            TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
            TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"
        ]
    ]);

    def dimMultiLabel = Dimension.create(DIM_MULTI_LABEL, false);
    def dimDataWriter = Dimension.create(DIM_DATA_WRITER, WekaDataWriter.class.name);

    def dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
    [NaiveBayes.class.name],
    [SMO.class.name]);

    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET,
    [
        NrOfTokensFeatureExtractor.class.name,
        NGramFeatureExtractor.class.name
    ]
    );

    def dimPipelineParameters = Dimension.create(
    DIM_PIPELINE_PARAMS,
    [
        NGramFeatureExtractor.PARAM_NGRAM_USE_TOP_K,
        "500",
        NGramFeatureExtractor.PARAM_NGRAM_MIN_N,
        1,
        NGramFeatureExtractor.PARAM_NGRAM_MAX_N,
        3
    ],
    [
        NGramFeatureExtractor.PARAM_NGRAM_USE_TOP_K,
        "1000",
        NGramFeatureExtractor.PARAM_NGRAM_MIN_N,
        1,
        NGramFeatureExtractor.PARAM_NGRAM_MAX_N,
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
            aggregate:	getPreprocessing(),
            innerReport: TrainTestReport.class,
            parameterSpace : [
                dimReaders,
                dimMultiLabel,
                dimDataWriter,
                dimClassificationArgs,
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
            aggregate:	getPreprocessing(),
            innerReport: TrainTestReport.class,
            parameterSpace : [
                dimReaders,
                dimMultiLabel,
                dimDataWriter,
                dimClassificationArgs,
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
        new TwentyNewsgroupsGroovyExperiment().runTrainTest();
        new TwentyNewsgroupsGroovyExperiment().runCrossValidation();
    }

}
