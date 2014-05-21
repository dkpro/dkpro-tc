package de.tudarmstadt.ukp.dkpro.tc.groovyexamples.single.pair

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.resource.ResourceInitializationException

import weka.classifiers.functions.SMO
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter
import de.tudarmstadt.ukp.dkpro.lab.Lab
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy
import de.tudarmstadt.ukp.dkpro.tc.core.Constants
import de.tudarmstadt.ukp.dkpro.tc.examples.io.PairTwentyNewsgroupsReader
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.length.DiffNrOfTokensPairFeatureExtractor
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchOutcomeIDReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.ClassificationReport
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter


/**
 * PairTwentyNewsgroupsExperiment, using Groovy
 *
 * The PairTwentyNewsgroupsExperiment takes pairs of news files and trains/tests
 * a binary classifier to learn if the files in the pair are from the same newsgroup.
 * The pairs are listed in a tsv file: see the files in src/main/resources/lists/ as
 * examples.
 * <p>
 * PairTwentyNewsgroupsExperiment uses similar architecture as TwentyNewsgroupsGroovyExperiment
 * ({@link BatchTaskTrainTest}) to automatically wire the standard tasks for
 * a basic TrainTest setup.  To remind the user to be careful of information leak when
 * training and testing on pairs of data from similar sources, we do not provide
 * a demo Cross Validation setup here.  (Our sample train and test datasets are from separate
 * newsgroups.)  Please see TwentyNewsgroupsGroovyExperiment for a demo implementing a CV experiment.
 *
 *
 * @author Emily Jamison
 */
class PairTwentyNewsgroupsDemo implements Constants {

    // === PARAMETERS===========================================================

    def experimentName = "PairTwentyNewsgroupsExperiment"
    def languageCode = "en"
    def listFilePathTrain = "src/main/resources/data/twentynewsgroups/pairs/pairslist.train"
    def listFilePathTest  ="src/main/resources/data/twentynewsgroups/pairs/pairslist.test"


    // === DIMENSIONS===========================================================

    def dimReaders = Dimension.createBundle("readers", [
        readerTest: PairTwentyNewsgroupsReader,
        readerTestParams: [
            PairTwentyNewsgroupsReader.PARAM_LISTFILE,
            listFilePathTest,
            PairTwentyNewsgroupsReader.PARAM_LANGUAGE_CODE,
            languageCode
        ],
        readerTrain: PairTwentyNewsgroupsReader,
        readerTrainParams: [
            PairTwentyNewsgroupsReader.PARAM_LISTFILE,
            listFilePathTrain,
            PairTwentyNewsgroupsReader.PARAM_LANGUAGE_CODE,
            languageCode
        ]
    ])

    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_PAIR)
    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL)
    def dimDataWriter = Dimension.create(DIM_DATA_WRITER, WekaDataWriter.name)

    def dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
    //	[NaiveBayes.name],
    [SMO.name])

    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET,
    [
        // This feature is sensible and fast, but gives bad results on the demo data
        DiffNrOfTokensPairFeatureExtractor.name,
        // Please review LuceneNGramPairFeatureExtractor's javadoc to understand
        // the parameters before using LuceneNGramPairFeatureExtractor.
        //      LuceneNGramPairFeatureExtractor.name
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

        BatchTaskTrainTest batchTask = [
            experimentName: experimentName + "-TrainTest-Groovy",
            // we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
            type: "Evaluation-"+ experimentName +"-TrainTest-Groovy",
            preprocessingPipeline:	getPreprocessing(),
            innerReports: [ClassificationReport],
            parameterSpace : [
                dimReaders,
                dimFeatureMode,
                dimLearningMode,
                dimDataWriter,
                dimClassificationArgs,
                dimFeatureSets
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
        createEngineDescription(StanfordSegmenter),
        createEngineDescription(StanfordNamedEntityRecognizer,
        StanfordNamedEntityRecognizer.PARAM_VARIANT, "all.3class.distsim.crf")
        )
    }

    public static void main(String[] args)
    {
        new PairTwentyNewsgroupsDemo().runTrainTest()
    }

}
