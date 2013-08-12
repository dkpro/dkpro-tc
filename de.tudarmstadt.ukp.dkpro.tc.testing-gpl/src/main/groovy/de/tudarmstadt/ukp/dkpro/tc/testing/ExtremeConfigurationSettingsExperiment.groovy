package de.tudarmstadt.ukp.dkpro.tc.testing;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.collection.CollectionReaderDescription
import org.apache.uima.resource.ResourceInitializationException

import weka.classifiers.bayes.NaiveBayes
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import de.tudarmstadt.ukp.dkpro.lab.Lab
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy
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
 * but it uses the BatchTaskCV and BatchTaskTrainTest to automatically wire the standard tasks for
 * a basic CV and TrainTest setup. This is more convenient, but less flexible.
 *
 * If you need to define a more complex experiment setup, look at TwentyNewsgroupsGroovyExtendedExperiment
 *
 * @author Oliver Ferschke
 */
public class ExtremeConfigurationSettingsExperiment {

    def experimentName = "ExtremeConfigurationSettingsTest";

    // === PARAMETERS===========================================================

    def corpusFilePathTrain = "src/main/resources/data/train";
    def corpusFilePathTest  ="src/main/resources/data/test";
    def languageCode = "en";
    def numFolds = 2;
    def manyFolds = 10;
    
    // === DIMENSIONS===========================================================

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

    //UIMA parameters for FE configuration
    def dimPipelineParametersEmpty = Dimension.create("pipelineParameters", []);

    def dimClassificationArgs =
    Dimension.create("classificationArguments",
    [
        [NaiveBayes.class.name].toArray()
    ] as Object[]
    );

    def dimClassificationArgsEmpty =
    Dimension.create("classificationArguments", [] as Object[]);

    def dimFeatureSets = Dimension.create(
    "featureSet",
    [
        [
            NrOfTokensFeatureExtractor.class.name,
            NGramFeatureExtractor.class.name
        ].toArray()
    ] as Object[]
    );

    def dimFeatureSetsEmpty = Dimension.create("featureSet", [] as Object[]);

    def dimFeatureParameters = Dimension.create(
    "featureParameters",
    [
        [
            "TopK",
            "500"
        ].toArray()
    ] as Object[]
    );

    def dimFeatureParametersEmpty = Dimension.create("featureParameters", [] as Object[]);


    // === Test =========================================================

    public void runEmptyPipelineParameters() throws Exception
    {

        BatchTaskCrossValidation batchTask = [
            experimentName: experimentName + "-CV-Groovy",
            type: "Evaluation-"+ experimentName +"-CV-Groovy",
            reader:	getReaderDesc(corpusFilePathTrain, languageCode),
            dataWriter: WekaDataWriter.class.name,
            aggregate: getPreprocessing(),
            innerReport: TrainTestReport.class,
            parameterSpace : [
                dimFeatureParameters,
                dimToLowerCase,
                dimMultiLabel,
                dimClassificationArgs,
                dimFeatureSets,
                dimPipelineParametersEmpty
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [BatchCrossValidationReport],
            numFolds: numFolds];

        Lab.getInstance().run(batchTask);

        BatchTaskTrainTest batchTaskTrainTest = [
            experimentName: experimentName + "-TrainTest-Groovy",
            type: "Evaluation-"+ experimentName +"-TrainTest-Groovy",
            readerTrain: getReaderDesc(corpusFilePathTrain, languageCode),
            readerTest: getReaderDesc(corpusFilePathTest, languageCode),
            dataWriter: WekaDataWriter.class.name,
            aggregate: getPreprocessing(),
            innerReport: TrainTestReport.class,
            parameterSpace : [
                dimFeatureParameters,
                dimToLowerCase,
                dimMultiLabel,
                dimClassificationArgs,
                dimFeatureSets,
                dimPipelineParametersEmpty
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [
                BatchTrainTestReport,
                BatchOutcomeIDReport]
        ];

        // Run
        Lab.getInstance().run(batchTaskTrainTest);
    }

    public void runEmptyFeatureExtractorSet() throws Exception
    {

        BatchTaskCrossValidation batchTask = [
            experimentName: experimentName + "-CV-Groovy",
            type: "Evaluation-"+ experimentName +"-CV-Groovy",
            reader: getReaderDesc(corpusFilePathTrain, languageCode),
            dataWriter: WekaDataWriter.class.name,
            aggregate: getPreprocessing(),
            innerReport: TrainTestReport.class,
            parameterSpace : [
                dimFeatureParameters,
                dimToLowerCase,
                dimMultiLabel,
                dimClassificationArgs,
                dimFeatureSetsEmpty,
                dimPipelineParameters
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [BatchCrossValidationReport],
            numFolds: numFolds];

        Lab.getInstance().run(batchTask);

        BatchTaskTrainTest batchTaskTrainTest = [
            experimentName: experimentName + "-TrainTest-Groovy",
            type: "Evaluation-"+ experimentName +"-TrainTest-Groovy",
            readerTrain: getReaderDesc(corpusFilePathTrain, languageCode),
            readerTest: getReaderDesc(corpusFilePathTest, languageCode),
            dataWriter: WekaDataWriter.class.name,
            aggregate: getPreprocessing(),
            innerReport: TrainTestReport.class,
            parameterSpace : [
                dimFeatureParameters,
                dimToLowerCase,
                dimMultiLabel,
                dimClassificationArgs,
                dimFeatureSetsEmpty,
                dimPipelineParameters
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [
                BatchTrainTestReport,
                BatchOutcomeIDReport]
        ];

        // Run
        Lab.getInstance().run(batchTaskTrainTest);
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
            createEngineDescription(BreakIteratorSegmenter.class),
            createEngineDescription(OpenNlpPosTagger.class)
        );
    }
}