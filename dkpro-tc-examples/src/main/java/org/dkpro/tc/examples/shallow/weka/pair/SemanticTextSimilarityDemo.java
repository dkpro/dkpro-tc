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
package org.dkpro.tc.examples.shallow.weka.pair;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.shallow.io.STSReader;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.pair.core.length.DiffNrOfTokensPairFeatureExtractor;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.builder.ExperimentBuilder;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.report.BatchCrossValidationReport;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.report.ScatterplotReport;
import org.dkpro.tc.ml.weka.WekaAdapter;

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

    public static void main(String[] args) throws Exception
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

    public static ParameterSpace getParameterSpace() throws ResourceInitializationException
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

        TcFeatureSet tcFeatureSet = new TcFeatureSet(
                TcFeatureFactory.create(DiffNrOfTokensPairFeatureExtractor.class));
        
        ExperimentBuilder builder = new ExperimentBuilder();
        builder.addFeatureSet(tcFeatureSet);
        builder.setLearningMode(LearningMode.REGRESSION);
        builder.setFeatureMode(FeatureMode.PAIR);
        builder.addAdapterConfiguration( new WekaAdapter(), SMOreg.class.getName());
        builder.setReaders(dimReaders);
        ParameterSpace pSpace = builder.buildParameterSpace();

        return pSpace;
    }

    // ##### CV #####
    public void runCrossValidation(ParameterSpace pSpace) throws Exception
    {
        ExperimentCrossValidation experiment = new ExperimentCrossValidation("RegressionExampleCV",
                NUM_FOLDS);
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(pSpace);
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        experiment.addReport(BatchCrossValidationReport.class);
        experiment.addReport(ScatterplotReport.class);

        // Run
        Lab.getInstance().run(experiment);
    }

    // ##### TRAIN-TEST #####
    public void runTrainTest(ParameterSpace pSpace) throws Exception
    {

        ExperimentTrainTest experiment = new ExperimentTrainTest("RegressionExampleTrainTest");
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(pSpace);
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        experiment.addReport(BatchTrainTestReport.class);
        experiment.addReport(ContextMemoryReport.class);
        experiment.addReport(ScatterplotReport.class);

        // Run
        Lab.getInstance().run(experiment);
    }

    public static AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {

        return createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class));
    }
}