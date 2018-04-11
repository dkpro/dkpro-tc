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
package org.dkpro.tc.examples.shallow.multi;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.features.style.InitialCharacterUpperCase;
import org.dkpro.tc.io.DelimiterSeparatedSequenceValuesReader;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.crfsuite.CrfSuiteAdapter;
import org.dkpro.tc.ml.report.BatchCrossValidationReport;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.svmhmm.SvmHmmAdapter;

/**
 * Example for NER as sequence classification.
 */
public class SequenceDemo
    implements Constants
{

    public static final String LANGUAGE_CODE = "de";
    public static final int NUM_FOLDS = 2;
    public static final String corpusFilePathTrain = "src/main/resources/data/germ_eval2014_ner/train";
    public static final String corpusFilePathTest = "src/main/resources/data/germ_eval2014_ner/test";

    public static File outputFolder = null;

    public static void main(String[] args) throws Exception
    {
        // Suppress mallet logging output
        System.setProperty("java.util.logging.config.file",
                "src/main/resources/logging.properties");

        // This is used to ensure that the required DKPRO_HOME environment variable is set.
        // Ensures that people can run the experiments even if they haven't read the setup
        // instructions first :)
        // Don't use this in real experiments! Read the documentation and set DKPRO_HOME as
        // explained there.
        DemoUtils.setDkproHome(SequenceDemo.class.getSimpleName());

        SequenceDemo demo = new SequenceDemo();
        // demo.runCrossValidation(getParameterSpace());
        demo.runTrainTest(getParameterSpace());
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace) throws Exception
    {
        ExperimentCrossValidation experiment = new ExperimentCrossValidation("NamedEntitySequenceDemoCV",
                NUM_FOLDS);
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(pSpace);
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        experiment.addReport(BatchCrossValidationReport.class);

        // Run
        Lab.getInstance().run(experiment);
    }

    // ##### Train Test #####
    public void runTrainTest(ParameterSpace pSpace) throws Exception
    {
        ExperimentTrainTest experiment = new ExperimentTrainTest(
                "NamedEntitySequenceDemoTrainTest");
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(pSpace);
        experiment.addReport(BatchTrainTestReport.class);
        experiment.addReport(ContextMemoryReport.class);
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

        // Run
        Lab.getInstance().run(experiment);
    }

    public static ParameterSpace getParameterSpace() throws ResourceInitializationException
    {

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                DelimiterSeparatedSequenceValuesReader.class, DelimiterSeparatedSequenceValuesReader.PARAM_LANGUAGE, "de",
                DelimiterSeparatedSequenceValuesReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                DelimiterSeparatedSequenceValuesReader.PARAM_TOKEN_INDEX, 1,
                DelimiterSeparatedSequenceValuesReader.PARAM_OUTCOME_INDEX, 2,
                DelimiterSeparatedSequenceValuesReader.PARAM_SKIP_LINES_START_WITH_STRING, "#",
                DelimiterSeparatedSequenceValuesReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*.txt");

        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
                DelimiterSeparatedSequenceValuesReader.class, DelimiterSeparatedSequenceValuesReader.PARAM_LANGUAGE, "de",
                DelimiterSeparatedSequenceValuesReader.PARAM_SOURCE_LOCATION, corpusFilePathTest,
                DelimiterSeparatedSequenceValuesReader.PARAM_TOKEN_INDEX, 1,
                DelimiterSeparatedSequenceValuesReader.PARAM_OUTCOME_INDEX, 2,
                DelimiterSeparatedSequenceValuesReader.PARAM_SKIP_LINES_START_WITH_STRING, "#",
                DelimiterSeparatedSequenceValuesReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*.txt");

        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, readerTrain);
        dimReaders.put(DIM_READER_TEST, readerTest);

        Map<String, Object> crfsuite = new HashMap<>();
        crfsuite.put(DIM_CLASSIFICATION_ARGS, new Object[] { new CrfSuiteAdapter(),
                CrfSuiteAdapter.ALGORITHM_LBFGS, "max_iterations=5" });
        crfsuite.put(DIM_DATA_WRITER, new CrfSuiteAdapter().getDataWriterClass().getName());
        crfsuite.put(DIM_FEATURE_USE_SPARSE, new CrfSuiteAdapter().useSparseFeatures());
        
        
        Map<String, Object> svmHmm = new HashMap<>();
        svmHmm.put(DIM_CLASSIFICATION_ARGS, new Object[] { new SvmHmmAdapter(),
                "-c", "1000" });
        svmHmm.put(DIM_DATA_WRITER, new SvmHmmAdapter().getDataWriterClass().getName());
        svmHmm.put(DIM_FEATURE_USE_SPARSE, new SvmHmmAdapter().useSparseFeatures());
        
        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", crfsuite, svmHmm);

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                new TcFeatureSet(TcFeatureFactory.create(TokenRatioPerDocument.class),
                        TcFeatureFactory.create(InitialCharacterUpperCase.class)));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, Constants.FM_SEQUENCE), dimFeatureSets, mlas);

        return pSpace;
    }

    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(NoOpAnnotator.class);
    }

}
