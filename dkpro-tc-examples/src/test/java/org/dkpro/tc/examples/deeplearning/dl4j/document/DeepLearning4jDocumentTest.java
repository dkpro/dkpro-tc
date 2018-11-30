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
package org.dkpro.tc.examples.deeplearning.dl4j.document;

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
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.io.DelimiterSeparatedValuesReader;
import org.dkpro.tc.ml.deeplearning4j.Deeplearning4jAdapter;
import org.dkpro.tc.ml.experiment.deep.DeepLearningExperimentTrainTest;
import org.dkpro.tc.ml.report.TrainTestReport;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class DeepLearning4jDocumentTest
    extends TestCaseSuperClass implements Constants
{
    ContextMemoryReport contextReport;
    public static final String corpusFilePathTrain = "src/main/resources/data/LabelledNews/train";
    public static final String corpusFilePathTest = "src/main/resources/data/LabelledNews/test";
    
    @Test
    public void runDocumentTest() throws Exception
    {
        contextReport = new ContextMemoryReport();
        
        Map<String, Object> config = new HashMap<>();
        config.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new Deeplearning4jAdapter(), new Dl4jDocumentUserCode() });

        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config);

        Map<String, Object> dimReaders = new HashMap<String, Object>();
        //
        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                DelimiterSeparatedValuesReader.class, 
                DelimiterSeparatedValuesReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain, 
                DelimiterSeparatedValuesReader.PARAM_OUTCOME_INDEX, 0, 
                DelimiterSeparatedValuesReader.PARAM_TEXT_INDEX, 1, 
                DelimiterSeparatedValuesReader.PARAM_LANGUAGE, "en",
                DelimiterSeparatedValuesReader.PARAM_PATTERNS, "/**/*.txt");
        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
                DelimiterSeparatedValuesReader.class, 
                DelimiterSeparatedValuesReader.PARAM_SOURCE_LOCATION, corpusFilePathTest,
                DelimiterSeparatedValuesReader.PARAM_OUTCOME_INDEX, 0, 
                DelimiterSeparatedValuesReader.PARAM_TEXT_INDEX, 1, 
                DelimiterSeparatedValuesReader.PARAM_LANGUAGE, "en",
                DelimiterSeparatedValuesReader.PARAM_PATTERNS, "/**/*.txt");
        dimReaders.put(DIM_READER_TEST, readerTest);
        
        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_FEATURE_MODE, Constants.FM_DOCUMENT),
                Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
                Dimension.create(DeepLearningConstants.DIM_MAXIMUM_LENGTH, 15),
                Dimension.create(DeepLearningConstants.DIM_VECTORIZE_TO_INTEGER, true),
                Dimension.create(DeepLearningConstants.DIM_PRETRAINED_EMBEDDINGS,
                        "src/test/resources/wordvector/glove.6B.50d_250.txt"),
                mlas);

        DeepLearningExperimentTrainTest experiment = new DeepLearningExperimentTrainTest(
                "dl4jSeq2Seq");
        experiment.setParameterSpace(pSpace);
        experiment.setPreprocessing(getPreprocessing());
        experiment.addReport(contextReport);
        experiment.addReport(new TrainTestReport());
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

        Lab.getInstance().run(experiment);

        List<String> lines = FileUtils.readLines(contextReport.id2outcomeFiles.get(0),
                "utf-8");
        assertEquals(203, lines.size());
        // line-wise compare
        assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
        assertEquals("#labels 0=bollywood 1=business 2=crime 3=politics", lines.get(1));
        assertTrue(lines.get(3).matches("0=[0-9]+;1;-1"));
        assertTrue(lines.get(4).matches("1=[0-9]+;1;-1"));
    }
    
    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }
}
