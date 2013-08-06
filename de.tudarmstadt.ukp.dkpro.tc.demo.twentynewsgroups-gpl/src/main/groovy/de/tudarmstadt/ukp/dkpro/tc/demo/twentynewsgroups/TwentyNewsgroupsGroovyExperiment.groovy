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
import de.tudarmstadt.ukp.dkpro.tc.demo.twentynewsgroups.io.TwentyNewsgroupsCorpusReader
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensFeatureExtractor
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchOutcomeIDReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.CVBatchReport
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
public class TwentyNewsgroupsGroovyExperiment {

    // === PARAMETERS===========================================================

    def corpusFilePathTrain = "src/main/resources/data/bydate-train";
    def corpusFilePathTest  ="src/main/resources/data/bydate-test";
    def languageCode = "en";
    def numFolds = 2;

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
     * Crossvalidation setting
     *
     * @throws Exception
     */
    protected void runCrossValidation() throws Exception
    {
        BatchTaskCrossValidation batchTask = [
            experimentName: "TwentyNewsgroups-CV-Groovy",
            type: "Evaluation-TwentyNewsgroups-CV-Groovy",
            reader:	getReaderDesc(corpusFilePathTrain, languageCode),
            dataWriter:         WekaDataWriter.class.name,
            aggregate:	getPreprocessing(),
            innerReport: TrainTestReport.class,
            parameterSpace : [
                dimFeatureParameters,
                dimToLowerCase,
                dimMultiLabel,
                dimClassificationArgs,
                dimFeatureSets,
                dimPipelineParameters
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [CVBatchReport],
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
            experimentName: "TwentyNewsgroups-TrainTest-Groovy",
            type: "Evaluation-TwentyNewsgroups-TrainTest-Groovy",
            readerTrain:	getReaderDesc(corpusFilePathTrain, languageCode),
            readerTest:		getReaderDesc(corpusFilePathTest, languageCode),
            dataWriter:         WekaDataWriter.class.name,
            aggregate:	getPreprocessing(),
            parameterSpace : [
                dimFeatureParameters,
                dimToLowerCase,
                dimMultiLabel,
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
        new TwentyNewsgroupsGroovyExperiment().runTrainTest();
        new TwentyNewsgroupsGroovyExperiment().runCrossValidation();
    }

}
