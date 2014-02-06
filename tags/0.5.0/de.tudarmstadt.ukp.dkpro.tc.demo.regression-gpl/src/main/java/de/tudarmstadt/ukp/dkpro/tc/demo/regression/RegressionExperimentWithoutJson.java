package de.tudarmstadt.ukp.dkpro.tc.demo.regression;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.functions.SMOreg;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.demo.regression.io.STSReader;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.similarity.GreedyStringTilingFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.RegressionReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

public class RegressionExperimentWithoutJson
{
    public static final String LANGUAGE_CODE = "en";
    public static int NUM_FOLDS = 2;
    public static final String inputFileTrain = "src/main/resources/sts2012/STS.input.MSRpar.txt";
    public static final String goldFileTrain = "src/main/resources/sts2012/STS.gs.MSRpar.txt";

    public static final String inputFileTest = "src/main/resources/sts2012/STS.input.MSRvid.txt";
    public static final String goldFileTest = "src/main/resources/sts2012/STS.gs.MSRvid.txt";

    public static ParameterSpace setup()
    {
        // configure training data reader dimension
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(Constants.DIM_READER_TRAIN, STSReader.class);
        dimReaders.put(
                Constants.DIM_READER_TRAIN_PARAMS,
                Arrays.asList(new Object[] {
                        STSReader.PARAM_INPUT_FILE, inputFileTrain,
                        STSReader.PARAM_GOLD_FILE, goldFileTrain }));
        dimReaders.put(Constants.DIM_READER_TEST, STSReader.class);
        dimReaders.put(
                Constants.DIM_READER_TEST_PARAMS,
                Arrays.asList(new Object[] {
                        STSReader.PARAM_INPUT_FILE, inputFileTest,
                        STSReader.PARAM_GOLD_FILE, goldFileTest }));
        
        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimClassificationArgs = Dimension.create(
                Constants.DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { SMOreg.class.getName() }));

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimFeatureSets = Dimension.create(
                Constants.DIM_FEATURE_SET,
                Arrays.asList(new String[] { NrOfTokensFeatureExtractor.class.getName(),
                        GreedyStringTilingFeatureExtractor.class.getName() }));

        @SuppressWarnings("unchecked")
        ParameterSpace pSpace = new ParameterSpace(
                Dimension.createBundle("readerTrain", dimReaders),
                Dimension.create(Constants.DIM_MULTI_LABEL, false),
                Dimension.create(Constants.DIM_IS_REGRESSION, true),
                Dimension.create(Constants.DIM_DATA_WRITER, WekaDataWriter.class.getName()),
                dimFeatureSets,
                dimClassificationArgs);
        return pSpace;
    }

    public static void main(String[] args)
        throws Exception
    {

        RegressionExperimentWithoutJson experiment = new RegressionExperimentWithoutJson();
        experiment.runCrossValidation(setup());
        experiment.runTrainTest(setup());
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskCrossValidation batch = new BatchTaskCrossValidation(
                "RegressionExampleCV",
                getPreprocessing(),
                NUM_FOLDS
        );
        batch.setAddInstanceId(true);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.setInnerReport(RegressionReport.class);
        batch.addReport(BatchCrossValidationReport.class);

        // Run
        Lab.getInstance().run(batch);
    }
    
    // ##### TRAIN-TEST #####
    protected void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {

        BatchTaskTrainTest batch = new BatchTaskTrainTest(
                "RegressionExampleTrainTest",
                getPreprocessing()
        );
        batch.setInnerReport(RegressionReport.class);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchTrainTestReport.class);
        batch.addReport(BatchOutcomeIDReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    public static AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {

        return createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class)
        );
    }
}