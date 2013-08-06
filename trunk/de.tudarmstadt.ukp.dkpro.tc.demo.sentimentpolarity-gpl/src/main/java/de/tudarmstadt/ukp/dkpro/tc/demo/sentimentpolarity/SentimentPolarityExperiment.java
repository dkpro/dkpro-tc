package de.tudarmstadt.ukp.dkpro.tc.demo.sentimentpolarity;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.File;
import java.io.IOException;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.demo.sentimentpolarity.io.MovieReviewCorpusReader;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.CVBatchReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.CrossValidationBatchReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.TrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCV;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * This Java-based experiment setup of the SentimentPolarityExperiment loads all its configurations
 * from a json file using the ParameterSpaceParser. Alternatively, the parameters could be defined
 * directly in this class, which makes on-the-fly changes more difficult when the experiment is run
 * on a server.
 */
public class SentimentPolarityExperiment
{

    private String languageCode;
    private String corpusFilePathTrain;
    private String corpusFilePathTest;
    private int folds;

    public static void main(String[] args)
        throws Exception
    {
        SentimentPolarityExperiment experiment = new SentimentPolarityExperiment();
        ParameterSpace pSpace = experiment.setup();

        experiment.runTrainTest(pSpace);
        experiment.runCrossvalidation(pSpace);
        experiment.runOldCrossvalidation(pSpace);
    }

    /**
     * Initialize Experiment
     * 
     * @return ParameterSpace for the javaExperiment
     * @throws Exception
     */
    protected ParameterSpace setup()
        throws Exception
    {
        String jsonPath = FileUtils.readFileToString(new File(
                "src/main/resources/config/train.json"));
        JSONObject json = (JSONObject) JSONSerializer.toJSON(jsonPath);

        languageCode = json.getString("languageCode");
        corpusFilePathTrain = json.getString("corpusFilePathTrain");
        corpusFilePathTest = json.getString("corpusFilePathTest");
        folds = json.getInt("folds");

        return ParameterSpaceParser.createParamSpaceFromJson(json);
    }

    // ##### CROSSVALIDATION #####
    protected void runCrossvalidation(ParameterSpace pSpace)
        throws Exception
    {

        BatchTaskCrossValidation batch = new
                BatchTaskCrossValidation("SentimentPolarityTrainTest",
                        getReaderDesc(corpusFilePathTest, languageCode),
                        getPreprocessing(),
                        WekaDataWriter.class.getName(), folds);
        batch.setType("Evaluation-SentimentPolarity-CV");
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.setInnerReport(TrainTestReport.class);
        batch.addReport(CrossValidationBatchReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    /**
     * Only for comparison. Will be deleted soon.
     * 
     * @param pSpace
     * @throws Exception
     */
    protected void runOldCrossvalidation(ParameterSpace pSpace)
        throws Exception
    {

        BatchTaskCV batch = new
                BatchTaskCV("SentimentPolarityTrainTest",
                        // BatchTaskCV batch = new BatchTaskCV("SentimentPolarityTrainTest",
                        getReaderDesc(corpusFilePathTest, languageCode),
                        getPreprocessing(),
                        WekaDataWriter.class.getName());
        batch.setType("Evaluation-SentimentPolarity-CV-Old");
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(CVBatchReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    // ##### TRAIN-TEST #####
    protected void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {

        BatchTaskTrainTest batch = new BatchTaskTrainTest("SentimentPolarityTrainTest",
                getReaderDesc(corpusFilePathTrain, languageCode),
                getReaderDesc(corpusFilePathTest, languageCode),
                getPreprocessing(),
                WekaDataWriter.class.getName());
        batch.setType("Evaluation-SentimentPolarity-TrainTest");
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchTrainTestReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    protected CollectionReaderDescription getReaderDesc(String corpusFilePath, String languageCode)
        throws ResourceInitializationException, IOException
    {

        return createReaderDescription(MovieReviewCorpusReader.class,
                MovieReviewCorpusReader.PARAM_PATH, corpusFilePath,
                MovieReviewCorpusReader.PARAM_LANGUAGE, languageCode,
                MovieReviewCorpusReader.PARAM_PATTERNS,
                new String[] { MovieReviewCorpusReader.INCLUDE_PREFIX + "*/*.txt" });
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {

        return createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE,
                        languageCode));
    }
}
