package de.tudarmstadt.ukp.dkpro.tc.experiments.reuters;

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
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.MultiLabelInstanceExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.report.BatchReport;
import de.tudarmstadt.ukp.dkpro.tc.core.report.CVBatchReport;
import de.tudarmstadt.ukp.dkpro.tc.core.report.CVReport;
import de.tudarmstadt.ukp.dkpro.tc.core.report.TestReport;
import de.tudarmstadt.ukp.dkpro.tc.core.task.CrossValidationTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.TestTask;
import de.tudarmstadt.ukp.dkpro.tc.experiments.reuters.io.ReutersCorpusReader;

public class ReutersTextClassification
{
    public static String languageCode;
    public static String corpusFilePathTrain;
    public static String corpusFilePathTest;
    public static String goldLabelFilePath;

    public static void main(String[] args)
        throws Exception
    {
        ReutersTextClassification experiment= new ReutersTextClassification();
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
    private ParameterSpace setup() throws Exception{
        String jsonPath = FileUtils.readFileToString(new File(ClassLoader.getSystemResource(
                "config/train.json").getFile()));
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
        PreprocessTask preprocessTask = new PreprocessTask();
        preprocessTask.setReader(getReaderDesc(corpusFilePathTrain, languageCode));
        preprocessTask.setAggregate(getPreprocessing());
        preprocessTask.setType(preprocessTask.getType() + "-Reuters-CV");

        // get some meta data depending on the whole document collection that we need for training
        TaskBase metaTask = new MetaInfoTask();
        metaTask.setType(metaTask.getType() + "-Reuters-CV");
        metaTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY,
                preprocessTask.getType());

        // Define the base task which generates an arff instances file
        ExtractFeaturesTask extractFeaturesTask = new ExtractFeaturesTask();
        extractFeaturesTask.setAddInstanceId(false);
        extractFeaturesTask.setInstanceExtractor(MultiLabelInstanceExtractor.class);
        extractFeaturesTask.setType(extractFeaturesTask.getType() + "-Reuters-CV");
        extractFeaturesTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY,
                preprocessTask.getType());
        extractFeaturesTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY,
                metaTask.getType());

        // Define the cross-validation task which operates on the results of the the train task
        TaskBase cvTask = new CrossValidationTask();
        cvTask.setType(cvTask.getType() + "-Reuters-CV");
        cvTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());
        cvTask.addImportLatest(CrossValidationTask.INPUT_KEY, ExtractFeaturesTask.OUTPUT_KEY,
                extractFeaturesTask.getType());
        cvTask.addReport(CVReport.class);

        // Define the overall task scenario
        BatchTask batch = new BatchTask();
        batch.setType("Reuters_Batch_CV_Evaluation");
        batch.setParameterSpace(pSpace);
        batch.addTask(preprocessTask);
        batch.addTask(metaTask);
        batch.addTask(extractFeaturesTask);
        batch.addTask(cvTask);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(CVBatchReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    // ##### TRAIN-TEST #####
    protected void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {

        PreprocessTask preprocessTaskTrain = new PreprocessTask();
        preprocessTaskTrain.setReader(getReaderDesc(corpusFilePathTrain, languageCode));
        preprocessTaskTrain.setAggregate(getPreprocessing());
        preprocessTaskTrain.setType(preprocessTaskTrain.getType() + "-Reuters-Train");

        PreprocessTask preprocessTaskTest = new PreprocessTask();
        preprocessTaskTest.setReader(getReaderDesc(corpusFilePathTest, languageCode));
        preprocessTaskTest.setAggregate(getPreprocessing());
        preprocessTaskTest.setType(preprocessTaskTest.getType() + "-Reuters-Test");

        // get some meta data depending on the whole document collection that we need for training
        TaskBase metaTask = new MetaInfoTask();
        metaTask.setType(metaTask.getType() + "-Reuters");
        metaTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY,
                preprocessTaskTrain.getType());

        ExtractFeaturesTask featuresTrainTask = new ExtractFeaturesTask();
        featuresTrainTask.setAddInstanceId(true);
        featuresTrainTask.setInstanceExtractor(MultiLabelInstanceExtractor.class);
        featuresTrainTask.setType(featuresTrainTask.getType() + "-Reuters-Train");
        featuresTrainTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY,
                preprocessTaskTrain.getType());
        featuresTrainTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY,
                metaTask.getType());

        ExtractFeaturesTask featuresTestTask = new ExtractFeaturesTask();
        featuresTestTask.setAddInstanceId(true);
        featuresTestTask.setInstanceExtractor(MultiLabelInstanceExtractor.class);
        featuresTestTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY,
                preprocessTaskTest.getType());
        featuresTestTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY,
                metaTask.getType());

        // Define the test task which operates on the results of the the train task
        TaskBase testTask = new TestTask();
        testTask.setType(testTask.getType() + "-Reuters-Test");
        testTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());
        testTask.addImportLatest(TestTask.INPUT_KEY_TRAIN, ExtractFeaturesTask.OUTPUT_KEY,
                featuresTrainTask.getType());
        testTask.addImportLatest(TestTask.INPUT_KEY_TEST, ExtractFeaturesTask.OUTPUT_KEY,
                featuresTestTask.getType());
        testTask.addReport(TestReport.class);

        // Define the overall task scenario
        BatchTask batch = new BatchTask();
        batch.setType("Reuters_Batch_Test_Evaluation");
        batch.setParameterSpace(pSpace);
        batch.addTask(preprocessTaskTrain);
        batch.addTask(preprocessTaskTest);
        batch.addTask(metaTask);
        batch.addTask(featuresTrainTask);
        batch.addTask(featuresTestTask);
        batch.addTask(testTask);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    private static CollectionReaderDescription getReaderDesc(String filePath, String language)
        throws ResourceInitializationException, IOException
    {
        return createDescription(
                ReutersCorpusReader.class,
                ReutersCorpusReader.PARAM_PATH, filePath,
                ReutersCorpusReader.PARAM_LANGUAGE, language,
                ReutersCorpusReader.PARAM_GOLD_LABEL_FILE, goldLabelFilePath,
                ReutersCorpusReader.PARAM_PATTERNS, new String[] { ReutersCorpusReader.INCLUDE_PREFIX + "*.txt" });
    }

    public static AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createAggregateDescription(
                createPrimitiveDescription(BreakIteratorSegmenter.class),
                createPrimitiveDescription(OpenNlpPosTagger.class));
    }
}
