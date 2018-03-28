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
package org.dkpro.tc.examples.shallow.xgboost.document;

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
import org.dkpro.tc.features.maxnormalization.AvgTokenRatioPerDocument;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.io.FolderwiseDataReader;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.xgboost.XgboostAdapter;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class XgboostDocumentPlain
    implements Constants
{
    public static final String LANGUAGE_CODE = "en";

    public static final String corpusFilePathTrain = "src/main/resources/data/twentynewsgroups/bydate-train";
    public static final String corpusFilePathTest = "src/main/resources/data/twentynewsgroups/bydate-test";

    public static void main(String[] args) throws Exception
    {

        DemoUtils.setDkproHome("target/");

        ParameterSpace pSpace = getParameterSpace();

        XgboostDocumentPlain experiment = new XgboostDocumentPlain();
        experiment.runTrainTest(pSpace);
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

        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
                FolderwiseDataReader.class, FolderwiseDataReader.PARAM_SOURCE_LOCATION,
                corpusFilePathTest, FolderwiseDataReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                FolderwiseDataReader.PARAM_PATTERNS, "*/*.txt");
        dimReaders.put(DIM_READER_TEST, readerTest);

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                new TcFeatureSet("DummyFeatureSet",
                        TcFeatureFactory.create(AvgTokenRatioPerDocument.class),
                        TcFeatureFactory.create(WordNGram.class, WordNGram.PARAM_NGRAM_USE_TOP_K,
                                20, WordNGram.PARAM_NGRAM_MIN_N, 1, WordNGram.PARAM_NGRAM_MAX_N,
                                3)));

        Map<String, Object> xgboostConfig = new HashMap<>();
        xgboostConfig.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new XgboostAdapter(), "objective=multi:softmax" });
        xgboostConfig.put(DIM_DATA_WRITER, new XgboostAdapter().getDataWriterClass().getName());
        xgboostConfig.put(DIM_FEATURE_USE_SPARSE, new XgboostAdapter().useSparseFeatures());

        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config",
                xgboostConfig);

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), dimFeatureSets, mlas);

        return pSpace;
    }

    // ##### TRAIN-TEST #####
    public void runTrainTest(ParameterSpace pSpace) throws Exception
    {

        ExperimentTrainTest experiment = new ExperimentTrainTest("XgboostDocumentTest");
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(pSpace);
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
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
