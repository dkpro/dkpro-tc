package de.tudarmstadt.ukp.dkpro.tc.experiments.twentynewsgroups;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription
import static org.uimafit.factory.CollectionReaderFactory.createDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.collection.CollectionReaderDescription
import org.apache.uima.resource.ResourceInitializationException

import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.functions.SMO
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import de.tudarmstadt.ukp.dkpro.lab.Lab
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy
import de.tudarmstadt.ukp.dkpro.tc.core.extractor.SingleLabelInstanceExtractor
import de.tudarmstadt.ukp.dkpro.tc.experiments.twentynewsgroups.io.TwentyNewsgroupsCorpusReader
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensFeatureExtractor
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.NGramMetaCollector
import de.tudarmstadt.ukp.dkpro.tc.weka.report.CVBatchReport
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCV
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter

/**
 * Groovy-Version of the TwentyNewsgroupsExperiment
 *
 * The TwentyNewsgroupsGroovyExperiment does the same as TwentyNewsgroupsGroovyExtendedExperiment,
 * but it uses the BatchTaskCV and BatchTaskTrainTest to automatically wire the standard tasks for
 * a basic CV and TrainTest setup. This is more convenient, but less flexible.
 *
 * If you need to define a more complex experiment setup, look at TwentyNewsgroupsGroovyExtendedExperiment
 *
 * @author Oliver Ferschke
 */
public class TwentyNewsgroupsGroovyExperiment {

	// === PARAMETERS===========================================================

	def corpusFilePathTrain = "src/main/resources/data/bydate-train";
	def corpusFilePathTest  ="src/main/resources/data/bydate-test";
    def languageCode = "en";

	// === DIMENSIONS===========================================================

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
                [NaiveBayes.class.name].toArray(),
                [SMO.class.name].toArray()
            ] as Object[]
     );

	def dimFeatureSets = Dimension.create(
        "featureSet",
        [
            [
                NrOfTokensFeatureExtractor.class.name,
                NGramFeatureExtractor.class.name
            ].toArray()
        ] as Object[]
    );



    // === Experiments =========================================================

    /**
     * Crossvalidation setting
     *
     * @throws Exception
     */
	protected void runCrossValidation() throws Exception
	{
		BatchTaskCV batchTask = [
			experimentName: "TwentyNewsgroups-CV-Groovy",
			type: "Evaluation-TwentyNewsgroups-CV-Groovy",
			reader:	getReaderDesc(corpusFilePathTrain, languageCode),
            instanceExtractor:  SingleLabelInstanceExtractor.class.name,
            metaCollectorClasses: [NGramMetaCollector.class],
            dataWriter:         WekaDataWriter.class.name,
			aggregate:	getPreprocessing(),
			parameterSpace : [dimFolds, dimTopNgramsK, dimToLowerCase, dimMultiLabel, dimClassificationArgs, dimFeatureSets, dimPipelineParameters],
			executionPolicy: ExecutionPolicy.RUN_AGAIN,
			reports:         [CVBatchReport]
		];

		Lab.getInstance().run(batchTask);
	}
	/**
	 * TrainTest Setting
	 *
	 * @throws Exception
	 */
	protected void runTrainTest() throws Exception
	{

		BatchTaskTrainTest batchTask = [
			experimentName: "TwentyNewsgroups-TrainTest-Groovy",
			type: "Evaluation-TwentyNewsgroups-TrainTest-Groovy",
			readerTrain:	getReaderDesc(corpusFilePathTrain, languageCode),
			readerTest:		getReaderDesc(corpusFilePathTest, languageCode),
            instanceExtractor:  SingleLabelInstanceExtractor.class.name,
            metaCollectorClasses: [NGramMetaCollector.class],
            dataWriter:         WekaDataWriter.class.name,
			aggregate:	getPreprocessing(),
			parameterSpace : [dimFolds, dimTopNgramsK, dimToLowerCase, dimMultiLabel, dimClassificationArgs, dimFeatureSets, dimPipelineParameters],
			executionPolicy: ExecutionPolicy.RUN_AGAIN,
			reports:         [CVBatchReport]
		];

		// Run
		Lab.getInstance().run(batchTask);
	}


	private CollectionReaderDescription getReaderDesc(String corpusFilePath, String language)
			throws ResourceInitializationException, IOException
	{
		return createDescription(
			TwentyNewsgroupsCorpusReader,
			TwentyNewsgroupsCorpusReader.PARAM_PATH, corpusFilePath,
            TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, language,
			TwentyNewsgroupsCorpusReader.PARAM_PATTERNS, [TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"]
		);
	}

	private AnalysisEngineDescription getPreprocessing()
			throws ResourceInitializationException
	{
		return createAggregateDescription(
			createPrimitiveDescription(BreakIteratorSegmenter),
			createPrimitiveDescription(OpenNlpPosTagger)
		);
	}

	public static void main(String[] args)
	{
		new TwentyNewsgroupsGroovyExperiment().runCrossValidation();
		new TwentyNewsgroupsGroovyExperiment().runTrainTest();
	}

}
