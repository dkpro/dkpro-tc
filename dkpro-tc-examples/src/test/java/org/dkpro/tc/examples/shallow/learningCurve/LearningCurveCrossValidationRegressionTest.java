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
package org.dkpro.tc.examples.shallow.learningCurve;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
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
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.shallow.feature.LengthFeatureNominal;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.features.maxnormalization.SentenceRatioPerDocument;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.io.DelimiterSeparatedValuesReader;
import org.dkpro.tc.ml.experiment.ExperimentLearningCurve;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.report.LearningCurveReport;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.dkpro.tc.ml.xgboost.XgboostAdapter;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import weka.classifiers.functions.LinearRegression;

/**
 * This test just ensures that the experiment runs without throwing any exception.
 */
public class LearningCurveCrossValidationRegressionTest
    extends TestCaseSuperClass
    implements Constants
{
    ContextMemoryReport contextReport;

    @Test
    public void testJavaTrainTest() throws Exception
    {
        runExperimentTrainTest();
        evaluateResults(contextReport.evaluationFolder);
        
    }
    
    private void evaluateResults(File evaluationFolder) {
    	List<File> folders = new ArrayList<>();
    	for(File f : evaluationFolder.listFiles()) {
    		if(f.isDirectory()) {
    			folders.add(f);
    		}
    	}
    	assertEquals(4, folders.size());
    	
	}

    private ParameterSpace getParameterSpace() throws Exception
    {
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                DelimiterSeparatedValuesReader.class,
                DelimiterSeparatedValuesReader.PARAM_OUTCOME_INDEX, 0,
                DelimiterSeparatedValuesReader.PARAM_TEXT_INDEX, 1,
                DelimiterSeparatedValuesReader.PARAM_SOURCE_LOCATION,
                "src/main/resources/data/essays/train/essay_train.txt",
                DelimiterSeparatedValuesReader.PARAM_LANGUAGE, "en");
        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
                DelimiterSeparatedValuesReader.class,
                DelimiterSeparatedValuesReader.PARAM_OUTCOME_INDEX, 0,
                DelimiterSeparatedValuesReader.PARAM_TEXT_INDEX, 1,
                DelimiterSeparatedValuesReader.PARAM_SOURCE_LOCATION,
                "src/main/resources/data/essays/test/essay_test.txt",
                DelimiterSeparatedValuesReader.PARAM_LANGUAGE, "en");
        dimReaders.put(DIM_READER_TEST, readerTest);

        Map<String, Object> xgboostConfig = new HashMap<>();
        xgboostConfig.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new XgboostAdapter(), "booster=gbtree", "reg:linear" });
        xgboostConfig.put(DIM_DATA_WRITER, new XgboostAdapter().getDataWriterClass());
        xgboostConfig.put(DIM_FEATURE_USE_SPARSE, new XgboostAdapter().useSparseFeatures());

        Map<String, Object> liblinearConfig = new HashMap<>();
        liblinearConfig.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new LiblinearAdapter(), "-s", "6" });
        liblinearConfig.put(DIM_DATA_WRITER, new LiblinearAdapter().getDataWriterClass());
        liblinearConfig.put(DIM_FEATURE_USE_SPARSE, new LiblinearAdapter().useSparseFeatures());

        Map<String, Object> libsvmConfig = new HashMap<>();
        libsvmConfig.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new LibsvmAdapter(), "-s", "3", "-c", "10" });
        libsvmConfig.put(DIM_DATA_WRITER, new LibsvmAdapter().getDataWriterClass());
        libsvmConfig.put(DIM_FEATURE_USE_SPARSE, new LibsvmAdapter().useSparseFeatures());

        Map<String, Object> wekaConfig = new HashMap<>();
        wekaConfig.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new WekaAdapter(), LinearRegression.class.getName() });
        wekaConfig.put(DIM_DATA_WRITER, new WekaAdapter().getDataWriterClass());
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

    private void runExperimentTrainTest() throws Exception
    {
        contextReport = new ContextMemoryReport();

        ExperimentLearningCurve experiment = new ExperimentLearningCurve("trainTest", 2, 2);
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(getParameterSpace());
        experiment.addReport(LearningCurveReport.class);
        experiment.addReport(contextReport);
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

        Lab.getInstance().run(experiment);
    }

    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }

}
