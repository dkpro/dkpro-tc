package org.dkpro.tc.demo.sentimentpolarity;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.resource.ResourceInitializationException

import weka.classifiers.functions.SMO
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import org.dkpro.lab.Lab
import org.dkpro.lab.task.Dimension
import org.dkpro.lab.task.impl.DefaultBatchTask
import org.dkpro.lab.task.BatchTask.ExecutionPolicy
import org.dkpro.tc.api.features.TcFeatureFactory
import org.dkpro.tc.api.features.TcFeatureSet
import org.dkpro.tc.core.Constants
import org.dkpro.tc.core.GroovyExperiment
import org.dkpro.tc.examples.io.MovieReviewCorpusReader
import org.dkpro.tc.features.length.NrOfTokens
import org.dkpro.tc.features.ngram.LuceneNGram
import org.dkpro.tc.features.syntax.QuestionsRatioFeatureExtractor
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.dkpro.tc.ml.weka.writer.WekaDataWriter
import org.dkpro.tc.ml.ExperimentCrossValidation
import org.dkpro.tc.ml.ExperimentTrainTest
import org.dkpro.tc.ml.report.BatchCrossValidationReport;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
/**
 * Groovy-Version of the SentimentPolarityExperiment
 */
public class SentimentPolarityDemo implements GroovyExperiment, Constants {

    // === PARAMETERS===========================================================

    def experimentName = "SentimentPolarity";
    def corpusFilePathTrain = "src/main/resources/data/sentiment/train-small";
    def corpusFilePathTest  ="src/main/resources/data/sentiment/test-small";
    def languageCode = "en";
    def numFolds = 2;

    // === DIMENSIONS===========================================================

    def testreader = createReaderDescription(MovieReviewCorpusReader.class,
        MovieReviewCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTest,
        MovieReviewCorpusReader.PARAM_LANGUAGE, languageCode,
        MovieReviewCorpusReader.PARAM_PATTERNS, MovieReviewCorpusReader.INCLUDE_PREFIX + "*/*.txt"
        );
    
    def trainreader = createReaderDescription(MovieReviewCorpusReader.class,
        MovieReviewCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
        MovieReviewCorpusReader.PARAM_LANGUAGE, languageCode,
        MovieReviewCorpusReader.PARAM_PATTERNS, MovieReviewCorpusReader.INCLUDE_PREFIX + "*/*.txt"
        );
    
    def dimReaders = Dimension.createBundle("readers", [
        readerTest: testreader,
        readerTrain: trainreader,
    ]);

    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL);
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT);

    def dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
    [	new WekaAdapter(),
        SMO.class.name,
        "-C",
        "10"
    ]);

    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET,
    new TcFeatureSet(
        TcFeatureFactory.create(QuestionsRatioFeatureExtractor.class),
        TcFeatureFactory.create(NrOfTokens.class),
        TcFeatureFactory.create(LuceneNGram.class, LuceneNGram.PARAM_NGRAM_USE_TOP_K, 500, LuceneNGram.PARAM_NGRAM_MIN_N, 1,LuceneNGram.PARAM_NGRAM_MAX_N, 3)
    ),
    new TcFeatureSet(
        TcFeatureFactory.create(QuestionsRatioFeatureExtractor.class),
        TcFeatureFactory.create(NrOfTokens.class),
        TcFeatureFactory.create(LuceneNGram.class, LuceneNGram.PARAM_NGRAM_USE_TOP_K, 5000 ,LuceneNGram.PARAM_NGRAM_MIN_N, 1,LuceneNGram.PARAM_NGRAM_MAX_N, 3)
    )
    );

    // === Experiments =========================================================

    /**
     * Crossvalidation setting
     *
     * @throws Exception
     */
    protected void runCrossValidation() throws Exception
    {

        ExperimentCrossValidation batchTask = [
            experimentName: experimentName + "-CV-Groovy",
            // we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
            type: "Evaluation-"+ experimentName +"-CV-Groovy",
            preprocessing: getPreprocessing(),
            parameterSpace : [
                dimReaders,
                dimLearningMode,
                dimFeatureMode,
                dimClassificationArgs,
                dimFeatureSets
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [BatchCrossValidationReport.newInstance()],
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

        ExperimentTrainTest batchTask = [
            experimentName: experimentName + "-TrainTest-Groovy",
            // we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
            type: "Evaluation-"+ experimentName +"-TrainTest-Groovy",
            preprocessing:	getPreprocessing(),
            parameterSpace : [
                dimReaders,
                dimLearningMode,
                dimFeatureMode,
                dimClassificationArgs,
                dimFeatureSets
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [
                BatchTrainTestReport.newInstance()
                ]
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

    public void run()
    {
        new SentimentPolarityDemo().runTrainTest();
        new SentimentPolarityDemo().runCrossValidation();
    }

}
