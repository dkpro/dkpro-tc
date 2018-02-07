/**
 * Copyright 2018
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
package org.dkpro.tc.examples.regression.pair;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.io.STSReader;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.pair.core.length.DiffNrOfTokensPairFeatureExtractor;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.report.BatchCrossValidationReport;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.report.ScatterplotReport;
import org.dkpro.tc.ml.weka.WekaRegressionAdapter;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import weka.classifiers.functions.SMOreg;

/**
 * A demo for pair classification with a regression outcome.
 * 
 * This uses the Semantic Text Similarity (STS) from the SemEval 2012 task. It computes text
 * similarity features between document pairs and <br>
 * then learns a regression model that predicts similarity of unseen document pairs.
 */
public class SemanticTextSimilarityDemo
    implements Constants
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
        // Ensures that people can run the experiments even if they haven't read the setup
        // instructions first :)
        // Don't use this in real experiments! Read the documentation and set DKPRO_HOME as
        // explained there.
        DemoUtils.setDkproHome(SemanticTextSimilarityDemo.class.getSimpleName());

        SemanticTextSimilarityDemo experiment = new SemanticTextSimilarityDemo();
        // experiment.runCrossValidation(getParameterSpace());
        experiment.runTrainTest(getParameterSpace());
    }

    public static ParameterSpace getParameterSpace()
        throws ResourceInitializationException
    {
        // configure training data reader dimension
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                STSReader.class, STSReader.PARAM_INPUT_FILE, inputFileTrain,
                STSReader.PARAM_GOLD_FILE, goldFileTrain);
        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
                STSReader.class, STSReader.PARAM_INPUT_FILE, inputFileTest,
                STSReader.PARAM_GOLD_FILE, goldFileTest);
        dimReaders.put(DIM_READER_TEST, readerTest);

        @SuppressWarnings("unchecked")
        Dimension<List<Object>> dimClassificationArgs = Dimension.create(
                Constants.DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new Object[] { new WekaRegressionAdapter(), SMOreg.class.getName() }));

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                new TcFeatureSet(TcFeatureFactory.create(DiffNrOfTokensPairFeatureExtractor.class)));

        ParameterSpace pSpace = new ParameterSpace(
                Dimension.createBundle(Constants.DIM_READER_TRAIN, dimReaders),
                Dimension.create(Constants.DIM_FEATURE_MODE, Constants.FM_PAIR),
                Dimension.create(Constants.DIM_LEARNING_MODE, Constants.LM_REGRESSION),
                dimFeatureSets, dimClassificationArgs);
        return pSpace;
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {
        ExperimentCrossValidation batch = new ExperimentCrossValidation("RegressionExampleCV", NUM_FOLDS);
        batch.setPreprocessing(getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchCrossValidationReport.class);
        batch.addReport(ScatterplotReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    // ##### TRAIN-TEST #####
    protected void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {

        ExperimentTrainTest batch = new ExperimentTrainTest("RegressionExampleTrainTest");
        batch.setPreprocessing(getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchTrainTestReport.class);
        batch.addReport(ContextMemoryReport.class);
        batch.addReport(ScatterplotReport.class);

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