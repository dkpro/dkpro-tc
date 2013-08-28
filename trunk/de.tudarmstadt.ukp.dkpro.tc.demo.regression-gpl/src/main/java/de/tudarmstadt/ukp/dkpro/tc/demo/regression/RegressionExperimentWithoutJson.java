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
import de.tudarmstadt.ukp.dkpro.tc.demo.regression.io.STSReader;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.similarity.GreedyStringTilingFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

public class RegressionExperimentWithoutJson
{
    public static final String LANGUAGE_CODE = "en";
    public static int NUM_FOLDS = 2;
    public static final String inputFile = "src/main/resources/sts2012/STS.input.MSRpar.txt";
    public static final String goldFile = "src/main/resources/sts2012/STS.gs.MSRpar.txt";

    public static void main(String[] args)
        throws Exception
    {

        // configure training data reader dimension
        Map<String, Object> dimReaderTrain = new HashMap<String, Object>();
        dimReaderTrain.put("readerTrain", STSReader.class);
        dimReaderTrain.put("readerTrainParams", Arrays.asList(new Object[] {
                STSReader.PARAM_INPUT_FILE, inputFile,
                STSReader.PARAM_GOLD_FILE, goldFile
        }));

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimClassificationArgs = Dimension.create(
                "classificationArguments",
                Arrays.asList(new String[] { SMOreg.class.getName() })
                );

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimFeatureSets = Dimension.create(
                "featureSet",
                Arrays.asList(new String[] {
                        NrOfTokensFeatureExtractor.class.getName(),
                        GreedyStringTilingFeatureExtractor.class.getName()
                })
                );

        ParameterSpace pSpace = new ParameterSpace(
                Dimension.createBundle("readerTrain", dimReaderTrain),
                Dimension.create("multiLabel", false),
                // this dimension is important
                // TODO should that be a dimension or rather a pipeline parameter?
                Dimension.create("isRegressionExperiment", true),
                dimFeatureSets,
                dimClassificationArgs
                );

        RegressionExperimentWithoutJson experiment = new RegressionExperimentWithoutJson();
        experiment.runCrossValidation(pSpace);
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskCrossValidation batch = new BatchTaskCrossValidation("RegressionExampleCV",
                getPreprocessing(),
                WekaDataWriter.class.getName(),
                NUM_FOLDS
                );
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        // TODO add report

        // Run
        Lab.getInstance().run(batch);
    }

    public static AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {

        return createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(
                OpenNlpPosTagger.class
                ));
    }
}