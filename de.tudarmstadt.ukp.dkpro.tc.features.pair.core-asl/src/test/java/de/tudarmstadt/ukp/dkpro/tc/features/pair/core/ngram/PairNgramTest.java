package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.demo.pairtwentynewsgroups.io.PairTwentyNewsgroupsReader;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneBasedMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ne.SharedNEsFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.ClassificationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * This a pure Java-based experiment setup of the TwentyNewsgroupsExperiment.
 * 
 * Defining the parameters directly in this class makes on-the-fly changes more difficult when the
 * experiment is run on a server.
 * 
 * For these cases, the self-sufficient Groovy versions are more suitable, since their source code
 * can be changed and then executed without pre-compilation.
 */
public class PairNgramTest
    implements Constants
{
    public static final String LANGUAGE_CODE = "en";

    public static int NUM_FOLDS = 3;

    public static final String locationPrefix = "de.tudarmstadt.ukp.dkpro.tc.demo.pairtwentynewsgroups";
    public static final String corpusFilePathTrain = locationPrefix + "src/main/resources/data/bydate-train";
    public static final String corpusFilePathTest = locationPrefix + "src/main/resources/data/bydate-test";
    
    private static File tmpDir = new File("target/" + LuceneBasedMetaCollector.LUCENE_DIR);

    @Test
    public static void test()
        throws Exception
    {
        ParameterSpace pSpace = getParameterSpace();

        PairNgramTest experiment = new PairNgramTest();
        experiment.runTrainTest(pSpace);
    }

    public static ParameterSpace getParameterSpace()
    {
        // configure training and test data reader dimension
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, PairTwentyNewsgroupsReader.class);
        dimReaders.put(
                DIM_READER_TRAIN_PARAMS,
                Arrays.asList(new Object[] {
                        PairTwentyNewsgroupsReader.PARAM_LISTFILE, corpusFilePathTrain,
                        PairTwentyNewsgroupsReader.PARAM_LANGUAGE2, LANGUAGE_CODE}
        ));
        dimReaders.put(DIM_READER_TEST, PairTwentyNewsgroupsReader.class);
        dimReaders.put(
                DIM_READER_TEST_PARAMS,
                Arrays.asList(new Object[] {
                        PairTwentyNewsgroupsReader.PARAM_LISTFILE, corpusFilePathTest,
                        PairTwentyNewsgroupsReader.PARAM_LANGUAGE2, LANGUAGE_CODE}
        ));

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimClassificationArgs = Dimension.create(
                DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { SMO.class.getName() }),
                Arrays.asList(new String[] { NaiveBayes.class.getName() }));

        @SuppressWarnings("unchecked")
        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] {
                        LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR, tmpDir }),
                Arrays.asList(new Object[] {
                        LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR, tmpDir }));

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                Arrays.asList(new String[] {
                        SharedNEsFeatureExtractor.class.getName(),
                        LuceneNGramPairFeatureExtractor.class.getName()
                }));

        ParameterSpace pSpace = new ParameterSpace(
                Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_DATA_WRITER, WekaDataWriter.class.getName()),
                Dimension.create(DIM_MULTI_LABEL, false),
                dimPipelineParameters,
                dimFeatureSets,
                dimClassificationArgs
        );

        return pSpace;
    }


    // ##### TRAIN-TEST #####
    protected void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {

        BatchTaskTrainTest batch = new BatchTaskTrainTest("PairTwentyNewsgroupsTrainTest",
                getPreprocessing());
        batch.setInnerReport(ClassificationReport.class);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchTrainTestReport.class);
        batch.addReport(BatchOutcomeIDReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {

        return createEngineDescription(
                createEngineDescription(StanfordSegmenter.class),
                createEngineDescription(StanfordNamedEntityRecognizer.class,
                    StanfordNamedEntityRecognizer.PARAM_VARIANT, "all.3class.distsim.crf")
        );
    }
    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
