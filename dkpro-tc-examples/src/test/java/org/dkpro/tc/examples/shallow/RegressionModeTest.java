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
import org.dkpro.tc.examples.shallow.feature.LengthFeatureNominal;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.features.maxnormalization.SentenceRatioPerDocument;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.io.DelimiterSeparatedValuesReader;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.report.CrossValidationReport;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.report.util.Tc2LtlabEvalConverter;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.dkpro.tc.ml.xgboost.XgboostAdapter;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.regression.MeanSquaredError;
import weka.classifiers.functions.LinearRegression;

/**
 * This test just ensures that the experiment runs without throwing any exception.
 */
public class RegressionModeTest
    extends TestCaseSuperClass
    implements Constants
{
    ContextMemoryReport contextReport;

    @Test
    public void testJavaTrainTest() throws Exception
    {
        runTrainTestExperiment();

        assertEquals(getSumOfExpectedTasksForTrainTest().intValue(),
                contextReport.allIds.size());
        assertEquals(getSumOfMachineLearningAdapterTasks().intValue(),
                contextReport.id2outcomeFiles.size());

        assertEquals(1.3, getMeanSquaredError(contextReport.id2outcomeFiles, "Xgboost"), 0.1);
        assertEquals(0.5, getMeanSquaredError(contextReport.id2outcomeFiles, "Weka"), 0.1);
        assertEquals(0.6, getMeanSquaredError(contextReport.id2outcomeFiles, "Libsvm"), 0.1);
        assertEquals(2.8, getMeanSquaredError(contextReport.id2outcomeFiles, "Liblinear"),
                0.2);

        verifyId2Outcome(getId2outcomeFile(contextReport.id2outcomeFiles, "Xgboost"));
        verifyId2Outcome(getId2outcomeFile(contextReport.id2outcomeFiles, "Weka"));
        verifyId2Outcome(getId2outcomeFile(contextReport.id2outcomeFiles, "Libsvm"));
        verifyId2Outcome(getId2outcomeFile(contextReport.id2outcomeFiles, "Liblinear"));
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

    private void runTrainTestExperiment() throws Exception
    {
        contextReport = new ContextMemoryReport();

        ExperimentTrainTest experiment = new ExperimentTrainTest("trainTest");
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(getParameterSpace());
        experiment.addReport(BatchTrainTestReport.class);
        experiment.addReport(contextReport);
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

        Lab.getInstance().run(experiment);
    }

    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }

    private void verifyId2Outcome(File id2outcomeFile) throws IOException
    {
        List<String> lines = FileUtils.readLines(id2outcomeFile, "utf-8");

        assertEquals(53, lines.size());

        // line-wise compare
        assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
        assertEquals("#labels", lines.get(1).trim());
        assertTrue(lines.get(3).matches("[A-Za-z0-9_]+=[0-9\\.]+;[0-9\\.]+;.*"));
        assertTrue(lines.get(4).matches("[A-Za-z0-9_]+=[0-9\\.]+;[0-9\\.]+;.*"));
        assertTrue(lines.get(5).matches("[A-Za-z0-9_]+=[0-9\\.]+;[0-9\\.]+;.*"));
        assertTrue(lines.get(6).matches("[A-Za-z0-9_]+=[0-9\\.]+;[0-9\\.]+;.*"));
        assertTrue(lines.get(7).matches("[A-Za-z0-9_]+=[0-9\\.]+;[0-9\\.]+;.*"));
        assertTrue(lines.get(8).matches("[A-Za-z0-9_]+=[0-9\\.]+;[0-9\\.]+;.*"));
        assertTrue(lines.get(9).matches("[A-Za-z0-9_]+=[0-9\\.]+;[0-9\\.]+;.*"));
        assertTrue(lines.get(10).matches("[A-Za-z0-9_]+=[0-9\\.]+;[0-9\\.]+;.*"));
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
        runCrossValidationExperiment();

        assertEquals(getSumOfExpectedTasksForCrossValidation().intValue(),
                contextReport.allIds.size());
        assertTrue(combinedId2OutcomeReportsAreDissimilar(
                contextReport.crossValidationCombinedIdFiles));

        // Larger variance is acceptable, i.e. Windows, OSX and Linux compute slightly different
        // values
        assertEquals(1.4, getMeanSquaredErrorCrossValidation(
                contextReport.crossValidationCombinedIdFiles, "Weka"), 0.3);
        assertEquals(3.2, getMeanSquaredErrorCrossValidation(
                contextReport.crossValidationCombinedIdFiles, "Xgboost"), 0.3);
        assertEquals(1.3, getMeanSquaredErrorCrossValidation(
                contextReport.crossValidationCombinedIdFiles, "Libsvm"), 0.3);
        assertEquals(4.1, getMeanSquaredErrorCrossValidation(
                contextReport.crossValidationCombinedIdFiles, "Liblinear"), 0.3);
    }

    private void runCrossValidationExperiment() throws Exception
    {
        contextReport = new ContextMemoryReport();
        
        ExperimentCrossValidation experiment = new ExperimentCrossValidation("crossValidation", 2);
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(getParameterSpace());
        experiment.addReport(CrossValidationReport.class);
        experiment.addReport(contextReport);
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

        Lab.getInstance().run(experiment);
    }

    private boolean combinedId2OutcomeReportsAreDissimilar(List<File> crossValidationTaskIds)
        throws IOException
    {

        Set<String> idset = new HashSet<>();

        for (File f : crossValidationTaskIds) {
            String idfile = FileUtils.readFileToString(f, "utf-8");
            if (idset.contains(idfile)) {
                return false;
            }
            idset.add(idfile);
        }

        return true;
    }

    private Integer getSumOfExpectedTasksForCrossValidation()
    {

        Integer sum = 0;

        sum += 8 * 2; // Feature Extraction * num_folds
        sum += 4 * 2; // meta collection
        sum += 8 * 2; // MLA tasks + facade tasks
        sum += 4; // CV tasks
        sum += 1; // Init
        sum += 1; // Outcome

        return sum;
    }

    private double getMeanSquaredError(List<File> id2outcomeFiles, String simpleName)
        throws Exception
    {

        for (File f : id2outcomeFiles) {
            if (f.getAbsolutePath().contains(simpleName + "TestTask-")) {

                EvaluationData<Double> data = Tc2LtlabEvalConverter
                        .convertRegressionModeId2Outcome(f);
                MeanSquaredError mse = new MeanSquaredError(data);
                return mse.getResult();
            }
        }

        return -1;
    }

    private double getMeanSquaredErrorCrossValidation(List<File> id2outcomeFiles, String simpleName)
        throws Exception
    {

        for (File f : id2outcomeFiles) {

            File file = new File(f.getParentFile(), "ATTRIBUTES.txt");
            Set<String> readSubTasks = readSubTasks(file);
            for (String s : readSubTasks) {
                File file2 = new File(f.getParentFile().getParentFile() + "/" + s,
                        "ATTRIBUTES.txt");
                if (!file2.exists()) {
                    continue;
                }
                Set<String> readSubTasks2 = readSubTasks(file2);
                for (String k : readSubTasks2) {

                    if (k.toLowerCase().contains(simpleName.toLowerCase())) {

                        EvaluationData<Double> data = Tc2LtlabEvalConverter
                                .convertRegressionModeId2Outcome(f);
                        MeanSquaredError mse = new MeanSquaredError(data);
                        return mse.getResult();
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

        sum += 2; // 2 x FeatExtract shared by Xgboost/Liblinear/Libsvm
        sum += 2; // 2 x FeatExtract for Weka
        sum += 2; // 2 x Init
        sum += 1; // 1 x Meta
        sum += 1; // 1 x Outcome

        return sum;
    }
}
