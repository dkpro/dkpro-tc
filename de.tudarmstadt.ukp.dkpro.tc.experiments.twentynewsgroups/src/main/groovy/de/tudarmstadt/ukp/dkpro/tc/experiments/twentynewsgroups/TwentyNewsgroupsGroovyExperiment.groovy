import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription
import static org.uimafit.factory.CollectionReaderFactory.createDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.collection.CollectionReaderDescription
import org.apache.uima.resource.ResourceInitializationException

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import de.tudarmstadt.ukp.dkpro.lab.Lab
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.SingleLabelInstanceExtractor
import de.tudarmstadt.ukp.dkpro.tc.core.report.BatchOutcomeReport
import de.tudarmstadt.ukp.dkpro.tc.core.report.BatchReport
import de.tudarmstadt.ukp.dkpro.tc.core.report.CVBatchReport
import de.tudarmstadt.ukp.dkpro.tc.core.report.CVReport
import de.tudarmstadt.ukp.dkpro.tc.core.report.OutcomeReport
import de.tudarmstadt.ukp.dkpro.tc.core.report.TestReport
import de.tudarmstadt.ukp.dkpro.tc.core.task.CrossValidationTask
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask
import de.tudarmstadt.ukp.dkpro.tc.core.task.TestTask
import de.tudarmstadt.ukp.dkpro.tc.experiments.twentynewsgroups.io.TwentyNewsgroupCorpusReader
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor



/**
 * Groovy-Version of the TwentyNewsgroupsExperiment
 *
 * @author Oliver Ferschke
 */
public class TwentyNewsgroupsGroovyExperiment {

	// === PARAMETERS===========================================================

	//TODO should also be dimensions (but, we're passing readers, not datasets)

	def corpusFilePathTrain = "src/main/resources/data/bydate-train";
	def corpusFilePathTest  ="src/main/resources/data/bydate-test";

	// === DIMENSIONS===========================================================

	def languageCode = "en";
	def dimLanguageCode = Dimension.create("languageCode", languageCode);
    def dimFolds = Dimension.create("folds" , 2);
    def dimTopNgramsK = Dimension.create("topNgramsK" , [500, 1000] as int[] );
    def dimToLowerCase = Dimension.create("toLowerCase", true);
    def dimMultiLabel = Dimension.create("multiLabel", false);

	//UIMA parameters for FE configuration
	def dimPipelineParameters = Dimension.create(
        "pipelineParameters",
        [
            NGramFeatureExtractor.PARAM_NGRAM_MIN_N, 1,
            NGramFeatureExtractor.PARAM_NGRAM_MAX_N, 3
        ]);


	def dimClassificationArgs =
        Dimension.create("classificationArguments",
            [
                ["weka.classifiers.bayes.NaiveBayes"].toArray(),
                ["weka.classifiers.functions.SMO"].toArray()
            ] as Object[]
     );

	def dimFeatureSets = Dimension.create(
        "featureSet",
        [
            [
                "de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensFeatureExtractor",
                "de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor"
            ].toArray()
        ] as Object[]
    );



    // === Experiments =========================================================

    /**
     * Crossvalidation setting
     *
     * @throws Exception
     */
	void runCrossValidation() throws Exception
	{
		/*
		 * Define (instantiate) tasks
		 */

		PreprocessTask preprocessTask = [
			reader:getReaderDesc(corpusFilePathTrain),
			aggregate:getPreprocessing(languageCode),
			type: "Preprocessing-TwentyNewsgroupsCV"
		];

		MetaInfoTask metaTask = [type: "MetaInfoTask-TwentyNewsgroupsCV"];

		ExtractFeaturesTask featureExtractionTask = [
			addInstanceId: false,
			instanceExtractor:SingleLabelInstanceExtractor.class,
			type: "FeatureExtraction-TwentyNewsgroupsCV"
		];

		CrossValidationTask cvTask = [
			type: "CVTask-TwentyNewsgroupsCV",
			reports: [CVReport]];


		/*
		 * Wire tasks
		 */
		metaTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY, preprocessTask.getType());
		featureExtractionTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY, preprocessTask.getType());
		featureExtractionTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());
		cvTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());
		cvTask.addImportLatest(CrossValidationTask.INPUT_KEY, ExtractFeaturesTask.OUTPUT_KEY, featureExtractionTask.getType());


		/*
		 *	Wrap wired tasks in batch task
		 */

		BatchTask batchTask = [
			type: "Evaluation-TwentyNewsgroups-CV",
			parameterSpace : [dimLanguageCode, dimFolds, dimTopNgramsK, dimToLowerCase, dimMultiLabel, dimClassificationArgs, dimFeatureSets, dimPipelineParameters],
			tasks:           [preprocessTask, metaTask, featureExtractionTask, cvTask],
			executionPolicy: ExecutionPolicy.RUN_AGAIN,
			reports:         [CVBatchReport]
		];

		/*
		 * Run
		 */
		Lab.newInstance("/lab/debug_context.xml").run(batchTask);
	}

	/**
	 * TrainTest Setting
	 *
	 * @throws Exception
	 */
	void runTrainTest() throws Exception
	{
		/*
		 * Define (instantiate) tasks
		 */

		PreprocessTask preprocessTaskTrain = [
			reader:getReaderDesc(corpusFilePathTrain),
			aggregate:getPreprocessing(languageCode),
			type: "Preprocessing-TwentyNewsgroups-Train"
		];

		PreprocessTask preprocessTaskTest = [
			reader:getReaderDesc(corpusFilePathTest),
			aggregate:getPreprocessing(languageCode),
			type: "Preprocessing-TwentyNewsgroups-Test"
		];

		MetaInfoTask metaTask = [type: "MetaInfoTask-TwentyNewsgroups-TrainTest"];

		ExtractFeaturesTask featuresTrainTask = [
			addInstanceId: true,
			instanceExtractor:SingleLabelInstanceExtractor.class,
			type: "FeatureExtraction-TwentyNewsgroups-Train"
		];

		ExtractFeaturesTask featuresTestTask = [
			addInstanceId: true,
			instanceExtractor:SingleLabelInstanceExtractor.class,
			type: "FeatureExtraction-TwentyNewsgroups-Test"
		];

		TestTask testTask = [
			type:"TestTask.TwentyNewsgroups",
			reports: [ TestReport, OutcomeReport]
		];


		/*
		 * Wire tasks
		 */
		metaTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY, preprocessTaskTrain.getType());
		featuresTrainTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY, preprocessTaskTrain.getType());
		featuresTrainTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());
		featuresTestTask.addImportLatest(MetaInfoTask.INPUT_KEY, PreprocessTask.OUTPUT_KEY, preprocessTaskTest.getType());
		featuresTestTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());
		testTask.addImportLatest(MetaInfoTask.META_KEY, MetaInfoTask.META_KEY, metaTask.getType());
		testTask.addImportLatest(TestTask.INPUT_KEY_TRAIN, ExtractFeaturesTask.OUTPUT_KEY, featuresTrainTask.getType());
		testTask.addImportLatest(TestTask.INPUT_KEY_TEST, ExtractFeaturesTask.OUTPUT_KEY, featuresTestTask.getType());


		/*
		 *	Wrap wired tasks in batch task
		 */

		BatchTask batchTask = [
			type: "Evaluation-TwentyNewsgroups-TrainTest",
			parameterSpace : [ dimLanguageCode, dimFolds, dimTopNgramsK, dimToLowerCase, dimMultiLabel, dimClassificationArgs, dimFeatureSets, dimPipelineParameters],
			tasks:           [ preprocessTaskTrain, preprocessTaskTest, metaTask, featuresTrainTask, featuresTestTask, testTask],
			executionPolicy: ExecutionPolicy.RUN_AGAIN,
			reports:         [ BatchReport, BatchOutcomeReport]
		];

		// Run
		Lab.newInstance("/lab/debug_context.xml").run(batchTask);
	}


	private CollectionReaderDescription getReaderDesc(String corpusFilePath)
			throws ResourceInitializationException, IOException
	{
		return createDescription(
			TwentyNewsgroupCorpusReader.class,
			TwentyNewsgroupCorpusReader.PARAM_PATH, corpusFilePath,
			TwentyNewsgroupCorpusReader.PARAM_PATTERNS, [TwentyNewsgroupCorpusReader.INCLUDE_PREFIX + "*/*.txt"]
		);
	}

	private AnalysisEngineDescription getPreprocessing(String languageCode)
			throws ResourceInitializationException
	{
		return createAggregateDescription(
			createPrimitiveDescription(
				BreakIteratorSegmenter.class),
			createPrimitiveDescription(
				OpenNlpPosTagger.class,
				OpenNlpPosTagger.PARAM_LANGUAGE, languageCode)
		);
	}

	public static void main(String[] args)
	{
		new TwentyNewsgroupsGroovyExperiment().runCrossValidation();
		new TwentyNewsgroupsGroovyExperiment().runTrainTest();
	}

}
