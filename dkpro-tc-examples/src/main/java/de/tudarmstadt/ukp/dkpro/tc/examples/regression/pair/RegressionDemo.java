/**
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.examples.regression.pair;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.functions.SMOreg;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.STSReader;
import de.tudarmstadt.ukp.dkpro.tc.examples.util.DemoUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.length.DiffNrOfTokensPairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.ml.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.ml.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaRegressionReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * A demo for pair classification with a regression outcome.
 * 
 * This uses the Semantic Text Similarity (STS) from the SemEval 2012 task. It computes text
 * similarity features between document pairs and <br>
 * then learns a regression model that predicts similarity of unseen document pairs.
 */
public class RegressionDemo
{
    public static final String LANGUAGE_CODE = "en";
    public static final int NUM_FOLDS = 2;
    public static final String inputFileTrain = "src/main/resources/data/sts2012/STS.input.MSRpar.txt";
    public static final String goldFileTrain = "src/main/resources/data/sts2012/STS.gs.MSRpar.txt";

    public static final String inputFileTest = "src/main/resources/data/sts2012/STS.input.MSRvid.txt";
    public static final String goldFileTest = "src/main/resources/data/sts2012/STS.gs.MSRvid.txt";


    public static void main(String[] args)
        throws Exception
    {

    	// This is used to ensure that the required DKPRO_HOME environment variable is set.
    	// Ensures that people can run the experiments even if they haven't read the setup instructions first :)
    	// Don't use this in real experiments! Read the documentation and set DKPRO_HOME as explained there.
    	DemoUtils.setDkproHome(RegressionDemo.class.getSimpleName());
    	
        RegressionDemo experiment = new RegressionDemo();
        experiment.runCrossValidation(setup());
        experiment.runTrainTest(setup());
    }
    
    public static ParameterSpace setup()
    {
        // configure training data reader dimension
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(Constants.DIM_READER_TRAIN, STSReader.class);
        dimReaders.put(
                Constants.DIM_READER_TRAIN_PARAMS,
                Arrays.asList(new Object[] { STSReader.PARAM_INPUT_FILE, inputFileTrain,
                        STSReader.PARAM_GOLD_FILE, goldFileTrain }));
        dimReaders.put(Constants.DIM_READER_TEST, STSReader.class);
        dimReaders.put(
                Constants.DIM_READER_TEST_PARAMS,
                Arrays.asList(new Object[] { STSReader.PARAM_INPUT_FILE, inputFileTest,
                        STSReader.PARAM_GOLD_FILE, goldFileTest }));

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimClassificationArgs = Dimension.create(
                Constants.DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { SMOreg.class.getName() }));

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimFeatureSets = Dimension.create(Constants.DIM_FEATURE_SET,
                // yields really bad results. To improve the performance, use a string similarity
                // based feature extractor
                Arrays.asList(new String[] { DiffNrOfTokensPairFeatureExtractor.class.getName() }));

        @SuppressWarnings("unchecked")
        ParameterSpace pSpace = new ParameterSpace(
                Dimension.createBundle("readerTrain", dimReaders), Dimension.create(
                        Constants.DIM_FEATURE_MODE, Constants.FM_PAIR), Dimension.create(
                        Constants.DIM_LEARNING_MODE, Constants.LM_REGRESSION), Dimension.create(
                        Constants.DIM_DATA_WRITER, WekaDataWriter.class.getName()), dimFeatureSets,
                dimClassificationArgs);
        return pSpace;
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskCrossValidation batch = new BatchTaskCrossValidation("RegressionExampleCV",
        		WekaAdapter.getInstance(),
                getPreprocessing(), NUM_FOLDS);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addInnerReport(WekaRegressionReport.class);
        batch.addReport(WekaBatchCrossValidationReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    // ##### TRAIN-TEST #####
    protected void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {

        BatchTaskTrainTest batch = new BatchTaskTrainTest("RegressionExampleTrainTest",
        		WekaAdapter.getInstance(),
                getPreprocessing());
        batch.addInnerReport(WekaRegressionReport.class);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(WekaBatchTrainTestReport.class);
        batch.addReport(WekaBatchOutcomeIDReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    public static AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {

        return createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class));
    }
}