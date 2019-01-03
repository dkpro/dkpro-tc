/**
 * Copyright 2019
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
package org.dkpro.tc.examples.deeplearning.keras.document;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.examples.deeplearning.PythonLocator;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.io.FolderwiseDataReader;
import org.dkpro.tc.ml.experiment.deep.DeepLearningExperimentTrainTest;
import org.dkpro.tc.ml.keras.KerasAdapter;
import org.dkpro.tc.ml.report.TrainTestReport;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class KerasDocumentTest
    extends PythonLocator
    implements Constants
{

    public static final String corpusFilePathTrain = "src/main/resources/data/twentynewsgroups/bydate-train";
    public static final String corpusFilePathTest = "src/main/resources/data/twentynewsgroups/bydate-test";

    ContextMemoryReport contextReport;

    @Test
    public void runTest() throws Exception
    {

        contextReport = new ContextMemoryReport();

        boolean testConditon = true;
        String python3 = null;
        python3 = getEnvironment();

        if (python3 == null) {
            System.err.println("Failed to locate Python with Keras - will skip this test case");
            testConditon = false;
        }

        if (testConditon) {

            Map<String, Object> dimReaders = new HashMap<String, Object>();
            CollectionReaderDescription readerTrain = CollectionReaderFactory
                    .createReaderDescription(FolderwiseDataReader.class,
                            FolderwiseDataReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                            FolderwiseDataReader.PARAM_LANGUAGE, "en",
                            FolderwiseDataReader.PARAM_PATTERNS, "*/*.txt");
            dimReaders.put(DIM_READER_TRAIN, readerTrain);

            CollectionReaderDescription readerTest = CollectionReaderFactory
                    .createReaderDescription(FolderwiseDataReader.class,
                            FolderwiseDataReader.PARAM_SOURCE_LOCATION, corpusFilePathTest,
                            FolderwiseDataReader.PARAM_LANGUAGE, "en",
                            FolderwiseDataReader.PARAM_PATTERNS, "*/*.txt");
            dimReaders.put(DIM_READER_TEST, readerTest);

            Map<String, Object> config = new HashMap<>();
            config.put(DIM_CLASSIFICATION_ARGS, new Object[] { new KerasAdapter(),
                    "src/main/resources/kerasCode/singleLabel/imdb_cnn_lstm.py" });

            Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config);

            ParameterSpace pSpace = new ParameterSpace(
                    Dimension.createBundle("readers", dimReaders),
                    Dimension.create(DIM_FEATURE_MODE, Constants.FM_DOCUMENT),
                    Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
                    Dimension.create(DeepLearningConstants.DIM_PYTHON_INSTALLATION, python3),
                    Dimension.create(DeepLearningConstants.DIM_MAXIMUM_LENGTH, 100),
                    Dimension.create(DeepLearningConstants.DIM_VECTORIZE_TO_INTEGER, true),
                    Dimension.create(DeepLearningConstants.DIM_PRETRAINED_EMBEDDINGS,
                            "src/test/resources/wordvector/glove.6B.50d_250.txt"),
                    mlas);

            DeepLearningExperimentTrainTest experiment = new DeepLearningExperimentTrainTest(
                    "KerasTrainTest");
            experiment.setPreprocessing(getPreprocessing());
            experiment.setParameterSpace(pSpace);
            experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
            experiment.addReport(TrainTestReport.class);
            experiment.addReport(contextReport);

            Lab.getInstance().run(experiment);

            assertEquals(1, contextReport.id2outcomeFiles.size());

            List<String> lines = FileUtils.readLines(contextReport.id2outcomeFiles.get(0), "utf-8");
            assertEquals(11, lines.size());

            // line-wise compare
            assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
            assertEquals("#labels 0=alt.atheism 1=comp.graphics", lines.get(1));
            assertTrue(lines.get(3).matches("[0-9]+\\.txt=[0-9]+;[0-9]+;-1"));
            assertTrue(lines.get(4).matches("[0-9]+\\.txt=[0-9]+;[0-9]+;-1"));
            assertTrue(lines.get(5).matches("[0-9]+\\.txt=[0-9]+;[0-9]+;-1"));
            assertTrue(lines.get(6).matches("[0-9]+\\.txt=[0-9]+;[0-9]+;-1"));
            assertTrue(lines.get(7).matches("[0-9]+\\.txt=[0-9]+;[0-9]+;-1"));
            assertTrue(lines.get(8).matches("[0-9]+\\.txt=[0-9]+;[0-9]+;-1"));
            assertTrue(lines.get(9).matches("[0-9]+\\.txt=[0-9]+;[0-9]+;-1"));
            assertTrue(lines.get(10).matches("[0-9]+\\.txt=[0-9]+;[0-9]+;-1"));

        }
    }

    protected static AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }
}
