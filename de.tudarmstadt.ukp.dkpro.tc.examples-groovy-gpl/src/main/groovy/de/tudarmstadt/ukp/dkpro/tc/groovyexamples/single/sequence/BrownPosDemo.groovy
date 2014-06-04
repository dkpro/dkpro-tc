package de.tudarmstadt.ukp.dkpro.tc.groovyexamples.single.sequence

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.fit.component.NoOpAnnotator
import org.apache.uima.resource.ResourceInitializationException

import de.tudarmstadt.ukp.dkpro.lab.Lab
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy
import de.tudarmstadt.ukp.dkpro.tc.core.Constants
import de.tudarmstadt.ukp.dkpro.tc.examples.io.BrownCorpusReader
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensUFE
import de.tudarmstadt.ukp.dkpro.tc.mallet.report.BatchCrossValidationReport
import de.tudarmstadt.ukp.dkpro.tc.mallet.report.ClassificationReport
import de.tudarmstadt.ukp.dkpro.tc.mallet.task.BatchTaskCrossValidation
import de.tudarmstadt.ukp.dkpro.tc.mallet.writer.MalletDataWriter

/**
 * This a Groovy experiment setup of POS tagging as sequence tagging.
 */
class BrownPosDemo
implements Constants {

    def String LANGUAGE_CODE = "en"
    def NUM_FOLDS = 2
    def String corpusFilePathTrain = "src/main/resources/data/brown_tei/"
    def experimentName = "BrownPosDemo"

    def dimReaders = Dimension.createBundle("readers", [
        readerTrain: BrownCorpusReader,
        readerTrainParams: [
            BrownCorpusReader.PARAM_LANGUAGE,
            LANGUAGE_CODE,
            BrownCorpusReader.PARAM_SOURCE_LOCATION,
            corpusFilePathTrain,
            BrownCorpusReader.PARAM_PATTERNS,
            [
                INCLUDE_PREFIX + "*.xml",
                INCLUDE_PREFIX + "*.xml.gz"
            ]
        ]])
    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL)
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_SEQUENCE)
    def dimDataWriter = Dimension.create(DIM_DATA_WRITER, MalletDataWriter.name)
    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET, [
        NrOfTokensUFE.name
    ])

    // ##### CV #####
    protected void runCrossValidation()
    throws Exception
    {
        BatchTaskCrossValidation batchTask = [
            experimentName: experimentName + "-CV-Groovy",
            // we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
            type: "Evaluation-"+ experimentName +"-CV-Groovy",
            preprocessingPipeline:  getPreprocessing(),
            innerReports: [ClassificationReport],
            parameterSpace : [
                dimReaders,
                dimFeatureMode,
                dimLearningMode,
                dimDataWriter,
                dimFeatureSets
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [BatchCrossValidationReport],
            numFolds: NUM_FOLDS]

        // Run
        Lab.getInstance().run(batchTask)
    }

    protected AnalysisEngineDescription getPreprocessing()
    throws ResourceInitializationException
    {
        return createEngineDescription(NoOpAnnotator)
    }

    public static void main(String[] args)
    {
        new BrownPosDemo().runCrossValidation()
    }
}
