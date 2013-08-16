package de.tudarmstadt.ukp.dkpro.tc.demo.twentynewsgroups;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.demo.twentynewsgroups.io.TwentyNewsgroupsCorpusReader;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.TrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * This a pure Java-based experiment setup of the TwentyNewsgroupsExperiment.
 * 
 * Defining the parameters directly in this class makes on-the-fly changes 
 * more difficult when the experiment is run on a server.
 * 
 * For these cases, the self-sufficient Groovy versions are more suitable, 
 * since their source code can be changed and then executed without
 * pre-compilation.
 */
public class TwentyNewsgroupsWithoutJsonExperiment
{
    public static final String LANGUAGE_CODE = "en";
          
    public static int NUM_FOLDS = 3;
    
    public static final String corpusFilePathTrain = "src/main/resources/data/bydate-train";
    public static final String corpusFilePathTest  = "src/main/resources/data/bydate-test";

    public static void main(String[] args)
        throws Exception
    {
        Dimension<String[]> dimClassificationArgs = Dimension.create(
                "classificationArguments",
                new String[][] {
                        new String[] { SMO.class.getName() },
                        new String[] { NaiveBayes.class.getName() }
                }
        );

        Dimension<String[]> dimFeatureParameters = Dimension.create(
                "featureParameters",
                new String[][] {
                        new String[] {"TopK", "500"},
                        new String[] {"TopK", "1000"}

                }
        );

        @SuppressWarnings("unchecked")
        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                "pipelineParameters",
                Arrays.asList(new Object[] {
                        NGramFeatureExtractor.PARAM_NGRAM_MIN_N, 1,
                        NGramFeatureExtractor.PARAM_NGRAM_MAX_N, 3
                })
        );
        
        Dimension<String[]> dimFeatureSets = Dimension.create(
            "featureSet",
            new String[][] {
                 new String[]{
                         NrOfTokensFeatureExtractor.class.getName(),
                         NGramFeatureExtractor.class.getName()
                 }
             }
        );
        TwentyNewsgroupsWithoutJsonExperiment experiment = new TwentyNewsgroupsWithoutJsonExperiment();
        ParameterSpace pSpace = new ParameterSpace(
                Dimension.create("multiLabel", false),
                Dimension.create("lowerCase", new Boolean[] { true }),
                dimPipelineParameters,
                dimFeatureSets,
                dimFeatureParameters,
                dimClassificationArgs
        );
        
        experiment.runCrossValidation(pSpace);
        experiment.runTrainTest(pSpace);
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {

        BatchTaskCrossValidation batch = new BatchTaskCrossValidation("TwentyNewsgroupsCV",
                getReaderDesc(corpusFilePathTrain, LANGUAGE_CODE), getPreprocessing(),
                WekaDataWriter.class.getName(), NUM_FOLDS);
        batch.setInnerReport(TrainTestReport.class);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchCrossValidationReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    // ##### TRAIN-TEST #####
    protected void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {

        BatchTaskTrainTest batch = new BatchTaskTrainTest("TwentyNewsgroupsTrainTest",
                getReaderDesc(corpusFilePathTrain, LANGUAGE_CODE), getReaderDesc(corpusFilePathTest,
                        LANGUAGE_CODE), getPreprocessing(),
                WekaDataWriter.class.getName());
        batch.setInnerReport(TrainTestReport.class);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchTrainTestReport.class);
        batch.addReport(BatchOutcomeIDReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    protected CollectionReaderDescription getReaderDesc(String corpusFilePath, String languageCode)
        throws ResourceInitializationException, IOException
    {

        return createReaderDescription(TwentyNewsgroupsCorpusReader.class,
                TwentyNewsgroupsCorpusReader.PARAM_PATH, corpusFilePath,
                TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, languageCode,
                TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
                new String[] { TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt" });
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {

        return createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE,
                        LANGUAGE_CODE));
    }
}
