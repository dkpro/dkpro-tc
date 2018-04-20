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
package org.dkpro.tc.examples.shallow;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.io.FolderwiseDataReader;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.report.BatchCrossValidationReport;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.report.util.Tc2LtlabEvalConverter;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.dkpro.tc.ml.xgboost.XgboostAdapter;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.Accuracy;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;

/**
 * This test just ensures that the experiment runs without throwing any exception.
 */
public class MultiDocumentTest
    extends TestCaseSuperClass implements Constants
{
    
    public static final String corpusFilePathTrain = "src/main/resources/data/twentynewsgroups/bydate-train";
    public static final String corpusFilePathTest = "src/main/resources/data/twentynewsgroups/bydate-test";

    @Test
    public void testJavaTrainTest() throws Exception
    {
        runExperimentTrainTest();

        assertEquals(getSumOfExpectedTasksForTrainTest().intValue(),
                ContextMemoryReport.allIds.size());

        assertEquals(getSumOfMachineLearningAdapterTasks().intValue(),
                ContextMemoryReport.id2outcomeFiles.size());

        assertEquals(0.5, getAccuracy(ContextMemoryReport.id2outcomeFiles, "Weka"), 0.1);
        assertEquals(0.5, getAccuracy(
                ContextMemoryReport.id2outcomeFiles, "Xgboost"), 0.5);
        assertEquals(0.625, getAccuracy(ContextMemoryReport.id2outcomeFiles, "Libsvm"), 0.1);
        assertEquals(0.625, getAccuracy(ContextMemoryReport.id2outcomeFiles, "Liblinear"), 0.1);
        
        verifyId2OutcomeReport(getId2outcomeFile(ContextMemoryReport.id2outcomeFiles, "Weka"));
        verifyId2OutcomeReport(getId2outcomeFile(ContextMemoryReport.id2outcomeFiles, "Xgboost"));
        verifyId2OutcomeReport(getId2outcomeFile(ContextMemoryReport.id2outcomeFiles, "Libsvm"));
        verifyId2OutcomeReport(getId2outcomeFile(ContextMemoryReport.id2outcomeFiles, "Liblinear"));
    }
    
    private ParameterSpace getParameterSpace() throws ResourceInitializationException {
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                FolderwiseDataReader.class, FolderwiseDataReader.PARAM_SOURCE_LOCATION,
                corpusFilePathTrain, FolderwiseDataReader.PARAM_LANGUAGE, "en",
                FolderwiseDataReader.PARAM_PATTERNS, "*/*.txt");
        dimReaders.put(DIM_READER_TRAIN, readerTrain);
        //
        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
                FolderwiseDataReader.class, FolderwiseDataReader.PARAM_SOURCE_LOCATION,
                corpusFilePathTest, FolderwiseDataReader.PARAM_LANGUAGE, "en",
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

    private void runExperimentTrainTest() throws Exception
    {
        ExperimentTrainTest experiment = new ExperimentTrainTest(
                "NamedEntitySequenceDemoTrainTest");
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(getParameterSpace());
        experiment.addReport(BatchTrainTestReport.class);
        experiment.addReport(ContextMemoryReport.class);
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        
        Lab.getInstance().run(experiment);        
    }

    private void verifyId2OutcomeReport(File wekaId2outcomeFile) throws IOException
    {
        List<String> lines = FileUtils.readLines(ContextMemoryReport.id2outcomeFiles.get(0),
                "utf-8");
        assertEquals(11, lines.size());

        // line-wise compare
        assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
        assertEquals("#labels 0=alt.atheism 1=comp.graphics", lines.get(1));
        // line 2 is a time-stamp
        assertTrue(lines.get(3).matches("alt.atheism/53068.txt=[0-1];[0-1];-1"));
        assertTrue(lines.get(4).matches("alt.atheism/53257.txt=[0-1];[0-1];-1"));
        assertTrue(lines.get(5).matches("alt.atheism/53260.txt=[0-1];[0-1];-1"));
        assertTrue(lines.get(6).matches("alt.atheism/53261.txt=[0-1];[0-1];-1"));
        assertTrue(lines.get(7).matches("comp.graphics/38758.txt=[0-1];[0-1];-1"));
        assertTrue(lines.get(8).matches("comp.graphics/38761.txt=[0-1];[0-1];-1"));
        assertTrue(lines.get(9).matches("comp.graphics/38762.txt=[0-1];[0-1];-1"));
        assertTrue(lines.get(10).matches("comp.graphics/38763.txt=[0-1];[0-1];-1"));
    }

    private File getId2outcomeFile(List<File> id2outcomeFiles, String k)
    {
        for (File f : id2outcomeFiles) {
            if (f.getAbsolutePath().toLowerCase().contains(k.toLowerCase())) {
                return f;
            }
        }
        return null;
    }

    @Test
    public void testCrossValidation() throws Exception
    {
        runExperimentCrossValidation();

        assertEquals(getSumOfExpectedTasksForCrossValidation().intValue(),
                ContextMemoryReport.allIds.size());

        assertEquals(0.625, getAccuracyCrossValidation(
                ContextMemoryReport.crossValidationCombinedIdFiles, "Weka"), 0.1);
        assertEquals(0.75, getAccuracyCrossValidation(
                ContextMemoryReport.crossValidationCombinedIdFiles, "Libsvm"), 0.1);
        assertEquals(0.8, getAccuracyCrossValidation(
                ContextMemoryReport.crossValidationCombinedIdFiles, "Liblinear"), 0.1);
        assertEquals(0.62, getAccuracyCrossValidation(
                ContextMemoryReport.crossValidationCombinedIdFiles, "Xgboost"), 0.1);
    }

    private void runExperimentCrossValidation() throws Exception
    {
        ExperimentCrossValidation experiment = new ExperimentCrossValidation(
                "crossValidation",2);
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(getParameterSpace());
        experiment.addReport(BatchCrossValidationReport.class);
        experiment.addReport(ContextMemoryReport.class);
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        
        Lab.getInstance().run(experiment);         
    }

    private Integer getSumOfExpectedTasksForCrossValidation()
    {

        Integer sum = 0;

        sum += 4; // 2 x FeatExtract Train/Test
        sum += 4; // 2 x Facade + 2x ML Adapter
        sum += 2; // 2 x Meta
        sum *= 4; // 4 adapter in the setup

        sum += 1; // 1 x Init
        sum += 1; // 1 x Outcome
        sum += 4; // 4 x Crossvalidation

        return sum;
    }

    private double getAccuracy(List<File> id2outcomeFiles, String simpleName) throws Exception
    {

        for (File f : id2outcomeFiles) {
            if (f.getAbsolutePath().toLowerCase().contains(simpleName.toLowerCase())) {

                EvaluationData<String> data = Tc2LtlabEvalConverter
                        .convertSingleLabelModeId2Outcome(f);
                Accuracy<String> acc = new Accuracy<>(data);
                return acc.getResult();
            }
        }

        return -1;
    }

    private double getAccuracyCrossValidation(List<File> id2outcomeFiles, String simpleName)
        throws Exception
    {

        for (File f : id2outcomeFiles) {

            File file = new File(f.getParentFile(), "ATTRIBUTES.txt");
            Set<String> readSubTasks = readSubTasks(file);
            for (String k : readSubTasks) {
                File file2 = new File(f.getParentFile().getParentFile() + "/" + k,
                        "ATTRIBUTES.txt");
                if (!file2.exists()) {
                    continue;
                }
                Set<String> readSubTasks2 = readSubTasks(file2);
                for (String j : readSubTasks2) {
                    if (j.toLowerCase().contains(simpleName.toLowerCase())) {
                        EvaluationData<String> data = Tc2LtlabEvalConverter
                                .convertSingleLabelModeId2Outcome(f);
                        Accuracy<String> acc = new Accuracy<>(data);
                        return acc.getResult();
                    }
                }
            }
        }

        return -1;
    }

    private Set<String> readSubTasks(File attributesTXT) throws Exception
    {
        List<String> readLines = FileUtils.readLines(attributesTXT, "utf-8");

        int idx = 0;
        boolean found = false;
        for (String line : readLines) {
            if (line.startsWith(BatchTask.SUBTASKS_KEY)) {
                found = true;
                break;
            }
            idx++;
        }

        if (!found) {
            return new HashSet<>();
        }

        String line = readLines.get(idx);
        int start = line.indexOf("[") + 1;
        int end = line.indexOf("]");
        String subTasks = line.substring(start, end);

        String[] tasks = subTasks.split(",");

        Set<String> results = new HashSet<>();

        for (String task : tasks) {
            results.add(task.trim());
        }

        return results;
    }

    private Integer getSumOfMachineLearningAdapterTasks()
    {

        Integer sum = 0;

        sum += 1; // Weka
        sum += 1; // Libsvm
        sum += 1; // Liblinear
        sum += 1; // Xgboost

        return sum;
    }

    private Integer getSumOfExpectedTasksForTrainTest()
    {

        Integer sum = 0;

        sum += 2; // 1 x Facade + 1x ML Adapter
        sum *= 4; // 3 adapter in setup
        
        sum += 2; // 2 folder FeatExtract Train+Test shared by Liblinear/Libsvm/xgboost
        sum += 2; // 2 folder FeatExtract Train+Test for Weka
        
        sum += 2; // 2 x Init
        sum += 1; // 1 x Outcome
        sum += 1; // 1 x Meta

        return sum;
    }
    
    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }
}
