package de.tudarmstadt.ukp.dkpro.tc.experiments.twentynewsgroups;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createDescription;

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
import de.tudarmstadt.ukp.dkpro.tc.core.report.BatchOutcomeReport;
import de.tudarmstadt.ukp.dkpro.tc.core.report.BatchReport;
import de.tudarmstadt.ukp.dkpro.tc.core.report.CVBatchReport;
import de.tudarmstadt.ukp.dkpro.tc.core.task.BatchTaskCV;
import de.tudarmstadt.ukp.dkpro.tc.core.task.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.experiments.twentynewsgroups.io.TwentyNewsgroupsCorpusReader;

public class TwentyNewsgroupsExperiment
{

    private String languageCode;
    private String corpusFilePathTrain;
    private String corpusFilePathTest;

    public static void main(String[] args)
        throws Exception
    {
        TwentyNewsgroupsExperiment experiment = new TwentyNewsgroupsExperiment();
        ParameterSpace pSpace = experiment.setup();

        experiment.runCrossValidation(pSpace);
        experiment.runTrainTest(pSpace);
    }
    
    /**
     * Initialize Experiment
     *
     * @return ParameterSpace for the javaExperiment
     * @throws Exception
     */
    protected ParameterSpace setup() throws Exception{
        String jsonPath = FileUtils.readFileToString(new File("src/main/resources/config/train.json"));
        JSONObject json = (JSONObject) JSONSerializer.toJSON(jsonPath);

        languageCode = json.getString("languageCode");
        corpusFilePathTrain = json.getString("corpusFilePathTrain");
        corpusFilePathTest = json.getString("corpusFilePathTest");

        return ParameterSpaceParser.createParamSpaceFromJson(json);
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {

        BatchTaskCV batch = new BatchTaskCV(
                "TwentyNewsgroupsCV",
                getReaderDesc(corpusFilePathTrain, languageCode),
                getPreprocessing()
        );
        batch.setType("Evaluation-TwentyNewsgroups-CV");
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
        BatchTaskTrainTest batch = new BatchTaskTrainTest(
                "TwentyNewsgroupsTrainTest",
                getReaderDesc(corpusFilePathTrain, languageCode),
                getReaderDesc(corpusFilePathTest, languageCode),
                getPreprocessing()
        );
        batch.setType("Evaluation-TwentyNewsgroups-TrainTest");
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchReport.class);
        batch.addReport(BatchOutcomeReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    protected  CollectionReaderDescription getReaderDesc(String corpusFilePath, String languageCode)
        throws ResourceInitializationException, IOException
    {

        return createDescription(TwentyNewsgroupsCorpusReader.class,
                TwentyNewsgroupsCorpusReader.PARAM_PATH, corpusFilePath,
                TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, languageCode,
                TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
                new String[] { TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt" });
    }

    protected  AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {

        return createAggregateDescription(
                createPrimitiveDescription(BreakIteratorSegmenter.class),
                createPrimitiveDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE,
                        languageCode));
    }
}
