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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.HashMap;
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
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.io.FolderwiseDataReader;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.report.BatchCrossValidationReport;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.dkpro.tc.ml.xgboost.XgboostAdapter;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;

public class MultiDocument
    implements Constants
{
    public static final String LANGUAGE_CODE = "en";

    public static final int NUM_FOLDS = 2;

    public static final String corpusFilePathTrain = "src/main/resources/data/twentynewsgroups/bydate-train";
    public static final String corpusFilePathTest = "src/main/resources/data/twentynewsgroups/bydate-test";

    public static void main(String[] args) throws Exception
    {

        DemoUtils.setDkproHome("target/");

        ParameterSpace pSpace = getParameterSpace();

        MultiDocument experiment = new MultiDocument();
        experiment.runTrainTest(pSpace);
        // experiment.runCrossValidation(pSpace);
    }

    public static ParameterSpace getParameterSpace() throws ResourceInitializationException
    {
        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the
        // train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                FolderwiseDataReader.class, FolderwiseDataReader.PARAM_SOURCE_LOCATION,
                corpusFilePathTrain, FolderwiseDataReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                FolderwiseDataReader.PARAM_PATTERNS, "*/*.txt");
        dimReaders.put(DIM_READER_TRAIN, readerTrain);
        //
        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
                FolderwiseDataReader.class, FolderwiseDataReader.PARAM_SOURCE_LOCATION,
                corpusFilePathTest, FolderwiseDataReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                FolderwiseDataReader.PARAM_PATTERNS, "*/*.txt");
        dimReaders.put(DIM_READER_TEST, readerTest);

        Map<String, Object> config = new HashMap<>();
        config.put(DIM_CLASSIFICATION_ARGS, new Object[] { new WekaAdapter(), SMO.class.getName(),
                "-C", "1.0", "-K", PolyKernel.class.getName() + " " + "-C -1 -E 2" });
        config.put(DIM_DATA_WRITER, new WekaAdapter().getDataWriterClass());
        config.put(DIM_FEATURE_USE_SPARSE, new WekaAdapter().useSparseFeatures());

        Map<String, Object> config2 = new HashMap<>();
        config2.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new LiblinearAdapter(), "-s", "4", "-c", "100" });
        config2.put(DIM_DATA_WRITER, new LiblinearAdapter().getDataWriterClass());
        config2.put(DIM_FEATURE_USE_SPARSE, new LiblinearAdapter().useSparseFeatures());

        Map<String, Object> config3 = new HashMap<>();
        config3.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new LibsvmAdapter(), "-s", "1", "-c", "1000", "-t", "3" });
        config3.put(DIM_DATA_WRITER, new LibsvmAdapter().getDataWriterClass());
        config3.put(DIM_FEATURE_USE_SPARSE, new LibsvmAdapter().useSparseFeatures());
        
        Map<String, Object> config4 = new HashMap<>();
        config4.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new XgboostAdapter(), "objective=multi:softmax" });
        config4.put(DIM_DATA_WRITER, new XgboostAdapter().getDataWriterClass());
        config4.put(DIM_FEATURE_USE_SPARSE, new XgboostAdapter().useSparseFeatures());

        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config, config2,
                config3, config4);

        Dimension<String> dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL);
        Dimension<String> dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT);
        Dimension<TcFeatureSet> dimFeatureSet = Dimension.create(DIM_FEATURE_SET, getFeatureSet());

        ParameterSpace ps = new ParameterSpace(dimLearningMode, dimFeatureMode, dimFeatureMode,
                dimFeatureSet, mlas, Dimension.createBundle(DIM_READERS, dimReaders));

        return ps;
    }

    private static TcFeatureSet getFeatureSet()
    {
        return new TcFeatureSet("DummyFeatureSet",
                TcFeatureFactory.create(TokenRatioPerDocument.class),
                TcFeatureFactory.create(WordNGram.class, WordNGram.PARAM_NGRAM_USE_TOP_K, 500,
                        WordNGram.PARAM_NGRAM_MIN_N, 1, WordNGram.PARAM_NGRAM_MAX_N, 3));
    }

    // ##### CV #####
    public void runCrossValidation(ParameterSpace pSpace) throws Exception
    {

        ExperimentCrossValidation experiment = new ExperimentCrossValidation("TwentyNewsgroupsCV",
                NUM_FOLDS);
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(pSpace);
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        experiment.addReport(BatchCrossValidationReport.class);
        experiment.addReport(new ContextMemoryReport());

        // Run
        Lab.getInstance().run(experiment);
    }

    // ##### TRAIN-TEST #####
    public void runTrainTest(ParameterSpace pSpace) throws Exception
    {

        ExperimentTrainTest experiment = new ExperimentTrainTest("SvmDemo");
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(pSpace);
        experiment.setExecutionPolicy(ExecutionPolicy.USE_EXISTING);
        experiment.addReport(BatchTrainTestReport.class);
        experiment.addReport(new ContextMemoryReport());

        // Run
        Lab.getInstance().run(experiment);
    }

    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }
}
