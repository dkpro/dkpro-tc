package de.tudarmstadt.ukp.dkpro.tc.demo.reuters;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.TrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.MekaDataWriter;

/**
 * This experiment showcases multi label classification. The experiment setup is done manually. If
 * you want to use automatically wired CV or TrainTest setups, have a look at the
 * TwentyNewsgroupsExample.
 */
public class ReutersTextClassification
{
    private int numFolds;

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
        numFolds = json.getInt("folds");

        return ParameterSpaceParser.createParamSpaceFromJson(json);
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {

        BatchTaskCrossValidation batch = new BatchTaskCrossValidation("ReutersCV",
                getPreprocessing(), MekaDataWriter.class.getName(), numFolds);
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
        BatchTaskTrainTest batch = new BatchTaskTrainTest("ReutersTrainTest",
                getPreprocessing(), MekaDataWriter.class.getName());
        batch.setInnerReport(TrainTestReport.class);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchOutcomeIDReport.class);
        batch.addReport(BatchTrainTestReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    private AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class));
    }
}
