package de.tudarmstadt.ukp.dkpro.tc.demo.reuters;

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
import de.tudarmstadt.ukp.dkpro.tc.demo.reuters.io.ReutersCorpusReader;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.CVBatchReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCV;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.MekaDataWriter;

/**
 * This experiment showcases multi label classification. The experiment setup is done manually. If
 * you want to use automatically wired CV or TrainTest setups, have a look at the
 * TwentyNewsgroupsExample.
 */
public class ReutersTextClassification
{
    private String languageCode;
    private String corpusFilePathTrain;
    private String corpusFilePathTest;
    private String goldLabelFilePath;

    public static void main(String[] args)
        throws Exception
    {
        ReutersTextClassification experiment = new ReutersTextClassification();
        ParameterSpace pSpace = experiment.setup();
        experiment.runCrossValidation(pSpace);
        experiment.runTrainTest(pSpace);
    }

    /**
     * Initialize Experiment
     * 
     * @return ParameterSpace for the experiment
     * @throws Exception
     */
    protected ParameterSpace setup()
        throws Exception
    {
        String jsonPath = FileUtils.readFileToString(new File(
                "src/main/resources/config/train.json"));
        JSONObject json = (JSONObject) JSONSerializer.toJSON(jsonPath);

        goldLabelFilePath = json.getString("goldLabelFilePath");
        corpusFilePathTrain = json.getString("corpusFilePathTrain");
        corpusFilePathTest = json.getString("corpusFilePathTest");
        languageCode = json.getString("languageCode");

        return ParameterSpaceParser.createParamSpaceFromJson(json);
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {

        BatchTaskCV batch = new BatchTaskCV("ReutersCV", getReaderDesc(corpusFilePathTrain,
                languageCode), getPreprocessing(), MekaDataWriter.class.getName());
        batch.setType("Evaluation-Reuters-CV");
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
        BatchTaskTrainTest batch = new BatchTaskTrainTest("ReutersTrainTest", getReaderDesc(
                corpusFilePathTrain, languageCode),
                getReaderDesc(corpusFilePathTest, languageCode), getPreprocessing(),
                MekaDataWriter.class.getName());
        batch.setType("Evaluation-Reuters-TrainTest");
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchTrainTestReport.class);
        batch.addReport(BatchOutcomeIDReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    private CollectionReaderDescription getReaderDesc(String filePath, String language)
        throws ResourceInitializationException, IOException
    {
        return createReaderDescription(ReutersCorpusReader.class, ReutersCorpusReader.PARAM_PATH,
                filePath, ReutersCorpusReader.PARAM_LANGUAGE, language,
                ReutersCorpusReader.PARAM_GOLD_LABEL_FILE, goldLabelFilePath,
                ReutersCorpusReader.PARAM_PATTERNS,
                new String[] { ReutersCorpusReader.INCLUDE_PREFIX + "*.txt" });
    }

    private AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class)
        );
    }
}
