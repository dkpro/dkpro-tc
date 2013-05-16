package de.tudarmstadt.ukp.dkpro.tc.experiments.regression;

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
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.TaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.SingleLabelInstanceExtractorPair;
import de.tudarmstadt.ukp.dkpro.tc.core.report.CVBatchReport;
import de.tudarmstadt.ukp.dkpro.tc.core.report.CVRegressionReport;
import de.tudarmstadt.ukp.dkpro.tc.core.task.CrossValidationTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask;
import de.tudarmstadt.ukp.dkpro.tc.experiments.regression.io.STSReader;

public class RegressionExperiment
{

    private static final String nameCV = "-RegressionExampleCV";

    static String jsonPath;
    static JSONObject json;

    public static String languageCode;
    public static String inputFile;
    public static String goldFile;

    public static void main(String[] args)
        throws Exception
    {

        jsonPath = FileUtils.readFileToString(new File("src/main/resources/config/train.json"));
        json = (JSONObject) JSONSerializer.toJSON(jsonPath);

        languageCode = json.getString("languageCode");
        inputFile = json.getString("inputFile");
        goldFile = json.getString("goldFile");

        runCrossValidation(ParameterSpaceParser.createParamSpaceFromJson(json));

        // runTrainTest(ParameterSpaceParser.createParamSpaceFromJson(json));
    }

    // ##### CV #####
    private static void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {
        PreprocessTask preprocessTask = new PreprocessTask();
        preprocessTask.setReader(getReaderDesc(inputFile, goldFile));
        preprocessTask.setAggregate(getPreprocessing());
        preprocessTask.setType(preprocessTask.getType() + "-RegressionExampleCV");

        // get some meta data depending on the whole document collection that we need for training
        MetaInfoTask metaTask = new MetaInfoTask();
        metaTask.setType(metaTask.getType() + nameCV);
        metaTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY,
                preprocessTask.getType());

        // Define the base task which generates an arff instances file
        ExtractFeaturesTask trainTask = new ExtractFeaturesTask();
        trainTask.setInstanceExtractor(SingleLabelInstanceExtractorPair.class);
        trainTask.setType(trainTask.getType() + nameCV);
        trainTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY,
                preprocessTask.getType());
        trainTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());

        // Define the cross-validation task which operates on the results of the the train task
        TaskBase cvTask = new CrossValidationTask();
        cvTask.setType(cvTask.getType() + nameCV);
        cvTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());
        cvTask.addImportLatest(CrossValidationTask.INPUT_KEY, ExtractFeaturesTask.OUTPUT_KEY,
                trainTask.getType());
        cvTask.addReport(CVRegressionReport.class);

        // Define the overall task scenario
        BatchTask batch = new BatchTask();
        batch.setType("Evaluation" + nameCV);
        batch.setParameterSpace(pSpace);
        batch.addTask(preprocessTask);
        batch.addTask(metaTask);
        batch.addTask(trainTask);
        batch.addTask(cvTask);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(CVBatchReport.class);

        // Run
        Lab.newInstance("/lab/debug_context.xml").run(batch);
    }

    // // ##### TRAIN-TEST #####
    // private static void runTrainTest(ParameterSpace pSpace, Object... additionalParameters)
    // throws Exception
    // {
    //
    // boolean addInstanceId = true;
    //
    // TaskBase preprocessTaskTrain = new PreprocessTask(getReaderDesc(inputFile),
    // getPreprocessing());
    // preprocessTaskTrain.setType(preprocessTaskTrain.getType() + "-TwentyNewsgroups-Train");
    //
    // TaskBase preprocessTaskTest = new PreprocessTask(getReaderDesc(goldFile),
    // getPreprocessing());
    // preprocessTaskTrain.setType(preprocessTaskTest.getType() + "-TwentyNewsgroups-Test");
    //
    // // get some meta data depending on the whole document collection that we need for training
    // TaskBase metaTask = new MetaInfoTask(additionalParameters);
    // metaTask.setType(metaTask.getType() + "-TwentyNewsgroups");
    // metaTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY,
    // preprocessTaskTrain.getType());
    //
    // TaskBase featuresTrainTask = new ExtractFeaturesTask(languageCode, addInstanceId,
    // additionalParameters);
    // featuresTrainTask.setType(featuresTrainTask.getType() + "-TwentyNewsgroups-Train");
    // featuresTrainTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY,
    // preprocessTaskTrain.getType());
    // featuresTrainTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY,
    // metaTask.getType());
    //
    // TaskBase featuresTestTask = new ExtractFeaturesTask(languageCode, addInstanceId,
    // additionalParameters);
    // featuresTestTask.setType(featuresTestTask.getType() + "TwentyNewsgroups-Test");
    // featuresTestTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY,
    // preprocessTaskTest.getType());
    // featuresTestTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY,
    // metaTask.getType());
    //
    // // Define the test task which operates on the results of the the train task
    // TaskBase testTask = new TestTask();
    // testTask.setType(testTask.getType() + "-TwentyNewsgroups");
    // testTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());
    // testTask.addImportLatest(TestTask.INPUT_KEY_TRAIN, ExtractFeaturesTask.OUTPUT_KEY,
    // featuresTrainTask.getType());
    // testTask.addImportLatest(TestTask.INPUT_KEY_TEST, ExtractFeaturesTask.OUTPUT_KEY,
    // featuresTestTask.getType());
    // testTask.addReport(TestReport.class);
    // testTask.addReport(OutcomeReport.class);
    //
    // // Define the overall task scenario
    // BatchTask batch = new BatchTask();
    // batch.setType("Evaluation-TwentyNewsgroups-TrainTest");
    // batch.setParameterSpace(pSpace);
    // batch.addTask(preprocessTaskTrain);
    // batch.addTask(preprocessTaskTest);
    // batch.addTask(metaTask);
    // batch.addTask(featuresTrainTask);
    // batch.addTask(featuresTestTask);
    // batch.addTask(testTask);
    // batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
    // batch.addReport(BatchReport.class);
    // batch.addReport(BatchOutcomeReport.class);
    //
    // // Run
    // Lab.newInstance("/lab/debug_context.xml").run(batch);
    // }

    private static CollectionReaderDescription getReaderDesc(String inputFile, String goldFile)
        throws ResourceInitializationException, IOException
    {

        return createDescription(STSReader.class, STSReader.PARAM_INPUT_FILE, inputFile,
                STSReader.PARAM_GOLD_FILE, goldFile);
    }

    public static AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {

        return createAggregateDescription(
                createPrimitiveDescription(BreakIteratorSegmenter.class),
                createPrimitiveDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE,
                        languageCode));
    }
}
