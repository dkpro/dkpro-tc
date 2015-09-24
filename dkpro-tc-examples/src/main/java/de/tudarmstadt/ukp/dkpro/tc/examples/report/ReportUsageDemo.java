/**
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.examples.report;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.bayes.NaiveBayes;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.initializer.TwentyNewsgroupsOutcomeAnnotator;
import de.tudarmstadt.ukp.dkpro.tc.examples.util.DemoUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.NGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.ml.ExperimentCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.ml.ExperimentTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.ml.report.BatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.ml.report.BatchOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.ml.report.BatchRuntimeReport;
import de.tudarmstadt.ukp.dkpro.tc.ml.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaClassificationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaFeatureValuesReport;

/**
 * This experiments the usage of various reports that can added as regular reports and inner reports
 */
public class ReportUsageDemo
    implements Constants
{
    public static final String LANGUAGE_CODE = "en";

    public static final int NUM_FOLDS = 3;

    public static final String corpusFilePathTrain = "src/main/resources/data/twentynewsgroups/bydate-train";
    public static final String corpusFilePathTest = "src/main/resources/data/twentynewsgroups/bydate-test";

    public static void main(String[] args)
        throws Exception
    {
    	
    	// This is used to ensure that the required DKPRO_HOME environment variable is set.
    	// Ensures that people can run the experiments even if they haven't read the setup instructions first :)
    	// Don't use this in real experiments! Read the documentation and set DKPRO_HOME as explained there.
    	DemoUtils.setDkproHome(ReportUsageDemo.class.getSimpleName());
    	
        ParameterSpace pSpace = getParameterSpace();

        ReportUsageDemo experiment = new ReportUsageDemo();
        experiment.runCrossValidation(pSpace);
        experiment.runTrainTest(pSpace);
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace()
    {
        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, TextReader.class);
        dimReaders.put(
                DIM_READER_TRAIN_PARAMS,
                Arrays.asList(TextReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                        TextReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                        TextReader.PARAM_PATTERNS, TextReader.INCLUDE_PREFIX  + "*/*.txt"));
        dimReaders.put(DIM_READER_TEST, TextReader.class);
        dimReaders.put(
                DIM_READER_TEST_PARAMS,
                Arrays.asList(TextReader.PARAM_SOURCE_LOCATION, corpusFilePathTest,
                		TextReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                		TextReader.PARAM_PATTERNS, TextReader.INCLUDE_PREFIX + "*/*.txt"));

        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { NaiveBayes.class.getName() })
              );

        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] {
                		NGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, 50,
                		NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 }));

        Dimension<List<String>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                Arrays.asList(new String[] {
                		NrOfTokensDFE.class.getName(),
                		LuceneNGramDFE.class.getName()
                }
        ));
        
        Dimension<List<String>> dimBaselineClassificationArgs = Dimension.create(DIM_BASELINE_CLASSIFICATION_ARGS,
        		Arrays.asList(new String[]{NaiveBayes.class.getName()}));
        
        Dimension<List<String>> dimBaselinePipelineParameters = Dimension.create(DIM_BASELINE_FEATURE_SET,
        		Arrays.asList(new String[]{NrOfTokensDFE.class.getName(),LuceneNGramDFE.class.getName()}));

        Dimension<List<Object>> dimBaselineFeatureSets = Dimension.create(DIM_BASELINE_PIPELINE_PARAMS,
        		Arrays.asList(new Object[]{
        				NGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, 50,
                		NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 2,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3}));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL), Dimension.create(
                        DIM_FEATURE_MODE, FM_DOCUMENT), dimPipelineParameters, dimFeatureSets,
                dimClassificationArgs, dimBaselineClassificationArgs, dimBaselineFeatureSets, dimBaselinePipelineParameters
                );

        return pSpace;
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {

        ExperimentCrossValidation batch = new ExperimentCrossValidation("ReportsCrossValidation", WekaClassificationAdapter.class,
                 NUM_FOLDS);
        // add a second report to TestTask which creates a report about average feature values for
        // each outcome label
        batch.setPreprocessing(getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        
        batch.addInnerReport(WekaClassificationReport.class);
        batch.addInnerReport(WekaFeatureValuesReport.class);
        batch.addReport(BatchCrossValidationReport.class);
        batch.addReport(BatchOutcomeIDReport.class);
        batch.addReport(BatchRuntimeReport.class);

        // Run
        Lab.getInstance().run(batch);
    }
    
    // ##### TRAIN-TEST #####
    protected void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {

        ExperimentTrainTest batch = new ExperimentTrainTest("ReportsTrainTest", WekaClassificationAdapter.class);
        // add a second report to TestTask which creates a report about average feature values for
        // each outcome label
        batch.setPreprocessing(getPreprocessing());
        batch.addInnerReport(WekaFeatureValuesReport.class);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchTrainTestReport.class);
        batch.addReport(BatchOutcomeIDReport.class);
        batch.addReport(BatchRuntimeReport.class);

        // Run
        Lab.getInstance().run(batch);
    }
    
    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {

        return createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class,
                		OpenNlpPosTagger.PARAM_LANGUAGE, LANGUAGE_CODE),
                createEngineDescription(TwentyNewsgroupsOutcomeAnnotator.class)
        );
    }
}
