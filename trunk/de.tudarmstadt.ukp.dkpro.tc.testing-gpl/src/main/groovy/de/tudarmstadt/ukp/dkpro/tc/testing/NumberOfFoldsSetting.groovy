package de.tudarmstadt.ukp.dkpro.tc.testing;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.resource.ResourceInitializationException

import weka.classifiers.bayes.NaiveBayes
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import de.tudarmstadt.ukp.dkpro.lab.Lab
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy
import de.tudarmstadt.ukp.dkpro.tc.core.Constants
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE
import de.tudarmstadt.ukp.dkpro.tc.testing.io.LineInstanceReader
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchCrossValidationReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.ClassificationReport
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter

/**
 * Testing the check in the CV batch task that the number of folds needs to be at least 2.
 *
 */
public class NumberOfFoldsSetting implements Constants {

    def experimentName = "BatchCvFoldTest";

    // === PARAMETERS===========================================================

    def corpusFilePathTrain = "classpath:/data/smalltexts/smallInstances.txt.gz";
    def languageCode = "en";
    def numFolds = 1;

    // === DIMENSIONS===========================================================

    def dimReaders = Dimension.createBundle("readers", [
        readerTrain: LineInstanceReader.class,
        readerTrainParams: [
            LineInstanceReader.PARAM_SOURCE_LOCATION,
            corpusFilePathTrain]
    ]);

    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL);
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT);
    def dimDataWriter = Dimension.create(DIM_DATA_WRITER, WekaDataWriter.class.name);


    def dimClassificationArgs = Dimension.create(
    DIM_CLASSIFICATION_ARGS,
    [NaiveBayes.class.name]);

    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET,
    [
        NrOfTokensDFE.class.name
    ]);

    // === Test =========================================================

    public void run() throws Exception
    {

        BatchTaskCrossValidation batchTask = [
            experimentName: experimentName + "-CV-Groovy",
            type: "Evaluation-"+ experimentName +"-CV-Groovy",
            aggregate: getPreprocessing(),
            innerReport: ClassificationReport.class,
            parameterSpace : [
                dimReaders,
                dimFeatureMode,
                dimLearningMode,
                dimDataWriter,
                dimClassificationArgs,
                dimFeatureSets
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [BatchCrossValidationReport],
            numFolds: numFolds];

        Lab.getInstance().run(batchTask);
    }

    private AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(
            createEngineDescription(BreakIteratorSegmenter.class)
        );
    }

    public static void main(String[] args)
    {
        new NumberOfFoldsSetting().run();
    }
}