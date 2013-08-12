package de.tudarmstadt.ukp.dkpro.tc.demo.regression;

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
import de.tudarmstadt.ukp.dkpro.tc.demo.regression.io.STSReader;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

public class RegressionExperiment
{
    static String jsonPath;
    static JSONObject json;

    public static String languageCode;
    public static String inputFile;
    public static String goldFile;
    public static int numFolds;

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
        goldFile = json.getString("goldFile");
        inputFile = json.getString("inputFile");
        languageCode = json.getString("languageCode");
        numFolds = json.getInt("folds");

        return ParameterSpaceParser.createParamSpaceFromJson(json);
    }

    public static void main(String[] args)
        throws Exception
    {
        RegressionExperiment experiment = new RegressionExperiment();
        experiment.runCrossValidation(experiment.setup());
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskCrossValidation batch = new BatchTaskCrossValidation("RegressionExampleCV",
                getReaderDesc(inputFile, goldFile), getPreprocessing(),
                WekaDataWriter.class.getName(), numFolds);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        // TODO add report

        // Run
        Lab.getInstance().run(batch);
    }

    private static CollectionReaderDescription getReaderDesc(String inputFile, String goldFile)
        throws ResourceInitializationException, IOException
    {

        return createReaderDescription(STSReader.class, STSReader.PARAM_INPUT_FILE, inputFile,
                STSReader.PARAM_GOLD_FILE, goldFile);
    }

    public static AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {

        return createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE,
                        languageCode));
    }
}
