package de.tudarmstadt.ukp.dkpro.tc.demo.pairtwentynewsgroups

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.resource.ResourceInitializationException

import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.functions.SMO
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter
import de.tudarmstadt.ukp.dkpro.lab.Lab
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy
import de.tudarmstadt.ukp.dkpro.tc.core.Constants
import de.tudarmstadt.ukp.dkpro.tc.demo.pairtwentynewsgroups.io.PairTwentyNewsgroupsReader
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.LuceneNGramPairFeatureExtractor
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneBasedMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ne.SharedNEsFeatureExtractor
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.CombinedNGramPairFeatureExtractor
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.NGramPairFeatureExtractor
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchOutcomeIDReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.ClassificationReport
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter

/**
 * PairTwentyNewsgroupsExperiment, using Groovy
 *
 * The PairTwentyNewsgroupsExperiment takes pairs of news files and trains/tests 
 * a binary classifier to learn if the files in the pair are from the same newsgroup.  
 * The pairs are listed in a tsv file: see the files in src/main/resources/lists/ as 
 * examples.
 * <p>
 * PairTwentyNewsgroupsExperiment uses similar architecture as TwentyNewsgroupsGroovyExperiment 
 * ({@link BatchTaskTrainTest}) to automatically wire the standard tasks for
 * a basic TrainTest setup.  To remind the user to be careful of information leak when
 * training and testing on pairs of data from similar sources, we do not provide
 * a demo Cross Validation setup here.  (Our sample train and test datasets are from separate
 * newsgroups.)  Please see TwentyNewsgroupsGroovyExperiment for a demo implementing a CV experiment.
 *
 *
 * @author Emily Jamison
 */
class PairTwentyNewsgroupsExperiment implements Constants
{
	
	// === PARAMETERS===========================================================

	def experimentName = "PairTwentyNewsgroupsExperiment";
	def languageCode = "en";
    def listFilePathTrain = "src/main/resources/lists/pairslist.train";
    def listFilePathTest  ="src/main/resources/lists/pairslist.test";
	

	// === DIMENSIONS===========================================================

	def dimReaders = Dimension.createBundle("readers", [
		readerTest: PairTwentyNewsgroupsReader.class,
		readerTestParams: [
			PairTwentyNewsgroupsReader.PARAM_LISTFILE,
			listFilePathTest,
			PairTwentyNewsgroupsReader.PARAM_LANGUAGE2,
			languageCode
		],
		readerTrain: PairTwentyNewsgroupsReader.class,
		readerTrainParams: [
			PairTwentyNewsgroupsReader.PARAM_LISTFILE,
			listFilePathTrain,
			PairTwentyNewsgroupsReader.PARAM_LANGUAGE2,
			languageCode
		]
	]);

	def dimIsPairClassification = Dimension.create(DIM_IS_PAIR_CLASSIFICATION, true);
	def dimMultiLabel = Dimension.create(DIM_MULTI_LABEL, false);
	def dimDataWriter = Dimension.create(DIM_DATA_WRITER, WekaDataWriter.class.name);

	def dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
//	[NaiveBayes.class.name],
	[SMO.class.name]);

	def dimFeatureSets = Dimension.create(
	DIM_FEATURE_SET,
	[
		// This feature is sensible and fast, but gives bad results on the demo data
        SharedNEsFeatureExtractor.class.name, 
		// This feature is sensible but slow.
//        CombinedNGramPairFeatureExtractor.class.name
		//Experimental.
//		LuceneNGramPairFeatureExtractor.class.name
	]
	);

	File tmpDir = new File("target/" + LuceneBasedMetaCollector.LUCENE_DIR);

    def dimPipelineParameters = Dimension.create(
    DIM_PIPELINE_PARAMS,

	    [
//	        CombinedNGramPairFeatureExtractor.PARAM_NGRAM_USE_TOP_K,
//	        "500",
//	        CombinedNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N,
//	        1,
//	        CombinedNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N,
//	        1
//			
//			LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW1,
//			1,
//			LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_VIEW1,
//			2,
//			LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW2,
//			1,
//			LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_VIEW2,
//			2,
//			LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_COMBO,
//			2,
//			LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_COMBO,
//			3,
//			LuceneNGramPairFeatureExtractor.PARAM_NGRAM_USE_TOP_K_VIEW1,
//			"100",
//			LuceneNGramPairFeatureExtractor.PARAM_NGRAM_USE_TOP_K_VIEW2,
//			"100",
//			LuceneNGramPairFeatureExtractor.PARAM_NGRAM_USE_TOP_K_COMBO,
//			"500",
			LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR,
			tmpDir
	    ]
    );

	// === Experiments =========================================================


	/**
	 * TrainTest Setting
	 *
	 * @throws Exception
	 */
	protected void runTrainTest() throws Exception
	{
		
		BatchTaskTrainTest batchTask = [
			experimentName: experimentName + "-TrainTest-Groovy",
			// we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
			type: "Evaluation-"+ experimentName +"-TrainTest-Groovy",
			aggregate:	getPreprocessing(),
			innerReport: ClassificationReport.class,
			parameterSpace : [
				dimReaders,
				dimIsPairClassification,
				dimMultiLabel,
				dimDataWriter,
				dimClassificationArgs,
				dimFeatureSets,
                dimPipelineParameters
			],
			executionPolicy: ExecutionPolicy.RUN_AGAIN,
			reports:         [
				BatchTrainTestReport,
				BatchOutcomeIDReport]
		];

		// Run
		Lab.getInstance().run(batchTask);
	}

	private AnalysisEngineDescription getPreprocessing()
	throws ResourceInitializationException
	{
		return createEngineDescription(
		createEngineDescription(StanfordSegmenter.class),
		createEngineDescription(StanfordNamedEntityRecognizer.class,
			StanfordNamedEntityRecognizer.PARAM_VARIANT, "all.3class.distsim.crf")
		);
	}

	public static void main(String[] args)
	{
		new PairTwentyNewsgroupsExperiment().runTrainTest();
	}

}
