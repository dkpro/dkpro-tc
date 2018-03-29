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
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.maxnormalization.SentenceRatioPerDocument;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.io.LinewiseTextOutcomeReader;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.report.BatchCrossValidationReport;
import org.dkpro.tc.ml.report.BatchRuntimeReport;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.dkpro.tc.ml.xgboost.XgboostAdapter;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import weka.classifiers.functions.LinearRegression;

public class MultiRegressionWekaLibsvmLiblinear
    implements Constants
{

    public static void main(String[] args) throws Exception
    {

        // This is used to ensure that the required DKPRO_HOME environment variable is
        // set.
        // Ensures that people can run the experiments even if they haven't read the
        // setup
        // instructions first :)
        // Don't use this in real experiments! Read the documentation and set DKPRO_HOME
        // as
        // explained there.
        DemoUtils.setDkproHome(MultiRegressionWekaLibsvmLiblinear.class.getSimpleName());

        ParameterSpace pSpace = getParameterSpace();

        MultiRegressionWekaLibsvmLiblinear experiment = new MultiRegressionWekaLibsvmLiblinear();
        // experiment.runTrainTest(pSpace);
        experiment.runCrossValidation(pSpace);
    }

    public static ParameterSpace getParameterSpace() throws ResourceInitializationException
    {
        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        // The reader is also responsible for setting the labels/outcome on all
        // documents/instances it creates.
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                LinewiseTextOutcomeReader.class, LinewiseTextOutcomeReader.PARAM_OUTCOME_INDEX, 0,
                LinewiseTextOutcomeReader.PARAM_TEXT_INDEX, 1,
                LinewiseTextOutcomeReader.PARAM_SOURCE_LOCATION,
                "src/main/resources/data/essays/train/essay_train.txt",
                LinewiseTextOutcomeReader.PARAM_LANGUAGE, "en");
        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
                LinewiseTextOutcomeReader.class, LinewiseTextOutcomeReader.PARAM_OUTCOME_INDEX, 0,
                LinewiseTextOutcomeReader.PARAM_TEXT_INDEX, 1,
                LinewiseTextOutcomeReader.PARAM_SOURCE_LOCATION,
                "src/main/resources/data/essays/test/essay_test.txt",
                LinewiseTextOutcomeReader.PARAM_LANGUAGE, "en");
        dimReaders.put(DIM_READER_TEST, readerTest);

        Map<String, Object> xgboostConfig = new HashMap<>();
        xgboostConfig.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new XgboostAdapter(), "booster=gbtree", "reg:linear" });
        xgboostConfig.put(DIM_DATA_WRITER, new XgboostAdapter().getDataWriterClass().getName());
        xgboostConfig.put(DIM_FEATURE_USE_SPARSE, new XgboostAdapter().useSparseFeatures());

        Map<String, Object> liblinearConfig = new HashMap<>();
        liblinearConfig.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new LiblinearAdapter(), "-s", "6" });
        liblinearConfig.put(DIM_DATA_WRITER, new LiblinearAdapter().getDataWriterClass().getName());
        liblinearConfig.put(DIM_FEATURE_USE_SPARSE, new LiblinearAdapter().useSparseFeatures());

        Map<String, Object> libsvmConfig = new HashMap<>();
        libsvmConfig.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new LibsvmAdapter(), "-s", "3", "-c", "10" });
        libsvmConfig.put(DIM_DATA_WRITER, new LibsvmAdapter().getDataWriterClass().getName());
        libsvmConfig.put(DIM_FEATURE_USE_SPARSE, new LibsvmAdapter().useSparseFeatures());

        Map<String, Object> wekaConfig = new HashMap<>();
        wekaConfig.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new WekaAdapter(), LinearRegression.class.getName() });
        wekaConfig.put(DIM_DATA_WRITER, new WekaAdapter().getDataWriterClass().getName());
        wekaConfig.put(DIM_FEATURE_USE_SPARSE, new WekaAdapter().useSparseFeatures());

        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", xgboostConfig,
                liblinearConfig, libsvmConfig, wekaConfig);

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                new TcFeatureSet(TcFeatureFactory.create(SentenceRatioPerDocument.class),
                        TcFeatureFactory.create(LengthFeatureNominal.class),
                        TcFeatureFactory.create(TokenRatioPerDocument.class)));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_REGRESSION),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), dimFeatureSets, mlas);

        return pSpace;
    }

    // ##### TRAIN-TEST #####
    public void runTrainTest(ParameterSpace pSpace) throws Exception
    {
        ExperimentTrainTest experiment = new ExperimentTrainTest("RegressionDemo");
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(pSpace);
        experiment.addReport(BatchTrainTestReport.class);
        experiment.addReport(ContextMemoryReport.class);
        experiment.addReport(BatchRuntimeReport.class);

        // Run
        Lab.getInstance().run(experiment);
    }

    public void runCrossValidation(ParameterSpace pSpace) throws Exception
    {
        ExperimentCrossValidation experiment = new ExperimentCrossValidation("RegressionDemo", 2);
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(pSpace);
        experiment.addReport(BatchCrossValidationReport.class);
        experiment.addReport(ContextMemoryReport.class);
        experiment.addReport(BatchRuntimeReport.class);

        // Run
        Lab.getInstance().run(experiment);
    }

    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }
}
