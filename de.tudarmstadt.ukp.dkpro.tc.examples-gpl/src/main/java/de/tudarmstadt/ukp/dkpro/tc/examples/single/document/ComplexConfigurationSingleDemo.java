package de.tudarmstadt.ukp.dkpro.tc.examples.single.document;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.core.OptionHandler;
import weka.core.Utils;
import de.tudarmstadt.ukp.dkpro.core.arktools.ArktweetTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.TwentyNewsgroupsCorpusReader;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.FrequencyDistributionNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.style.IsSurroundedByCharsUFE;
import de.tudarmstadt.ukp.dkpro.tc.features.twitter.EmoticonRatioDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.twitter.NumberOfHashTagsDFE;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * This a pure Java-based experiment setup of the Twitter Sentiment experiment, as described in:
 * 
 * <pre>
 * Johannes Daxenberger and Oliver Ferschke and Iryna Gurevych and Torsten Zesch (2014).
 * DKPro TC: A Java-based Framework for Supervised Learning Experiments on Textual Data.
 * In: Proceedings of the 52nd Annual Meeting of the ACL.
 * </pre>
 * 
 * This demo show-cases how to do a complex configuration setup to classify a set of tweets 
 * as either "emotional" or "neutral".
 * 
 * @see de.tudarmstadt.ukp.dkpro.tc.groovyexamples.single.document.TwitterSentimentDemo
 */
public class ComplexConfigurationSingleDemo
    implements Constants
{

    public static void main(String[] args)
        throws Exception
    {
        ParameterSpace pSpace = getParameterSpace();

        ComplexConfigurationSingleDemo experiment = new ComplexConfigurationSingleDemo();
        experiment.runCrossValidation(pSpace);
        experiment.runTrainTest(pSpace);
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace()
    {
        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, TwentyNewsgroupsCorpusReader.class);
        dimReaders.put(
                DIM_READER_TRAIN_PARAMS,
                Arrays.asList(new Object[] { TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION,
                        "src/main/resources/data/twitter/train",
                        TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE,
                        "en", TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
                        TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt" }));
        dimReaders.put(DIM_READER_TEST, TwentyNewsgroupsCorpusReader.class);
        dimReaders.put(
                DIM_READER_TEST_PARAMS,
                Arrays.asList(new Object[] { TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION,
                        "src/main/resources/data/twitter/test",
                        TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE,
                        "en", TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
                        TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt" }));

        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
	    		Arrays.asList(new String[] { SMO.class.getName(), "-C", "1.0", 
	            		"-K", PolyKernel.class.getName() + " " + "-C -1 -E 2" }),
        		Arrays.asList(new String[] { RandomForest.class.getName(), "-I", "5" }),
        		Arrays.asList(new String[] { Bagging.class.getName(), "-I", "2",
        				"-W", J48.class.getName(), "--", "-C", "0.5", "-M", "2" }));
 
        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] {
                        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K,
                        "500", FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 }));
               
        Dimension<List<String>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                Arrays.asList(new String[] { NumberOfHashTagsDFE.class.getName(), 
                		EmoticonRatioDFE.class.getName(),
                		LuceneNGramDFE.class.getName() }),
        		Arrays.asList(new String[] { LuceneNGramDFE.class.getName() }));
        
        Map<String, Object> dimFeatureSelection = new HashMap<String, Object>();
        dimFeatureSelection.put(DIM_FEATURE_SEARCHER_ARGS,
        		Arrays.asList(new String[] { Ranker.class.getName(), "-N", "100" }));
        dimFeatureSelection.put(DIM_ATTRIBUTE_EVALUATOR_ARGS,
                Arrays.asList(new String[] { InfoGainAttributeEval.class.getName() }));
        dimFeatureSelection.put(DIM_APPLY_FEATURE_SELECTION, true);

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_DATA_WRITER, WekaDataWriter.class.getName()),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL), Dimension.create(
                        DIM_FEATURE_MODE, FM_DOCUMENT), dimPipelineParameters, dimFeatureSets,
                dimClassificationArgs, Dimension.createBundle("featureSelection",
                        dimFeatureSelection));

        return pSpace;
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskCrossValidation batch = new BatchTaskCrossValidation("TwitterSentimentCV",
                getPreprocessing(), 10);
        batch.setParameterSpace(pSpace);
        batch.addReport(BatchCrossValidationReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    // ##### TRAIN-TEST #####
    protected void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskTrainTest batch = new BatchTaskTrainTest("TwitterSentimentTrainTest",
                getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.addReport(BatchTrainTestReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(
        		createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE,
                        "en"),
                createEngineDescription(ArktweetTagger.class, ArktweetTagger.PARAM_LANGUAGE, "en",
                ArktweetTagger.PARAM_VARIANT,
                "default"));
    }
}
