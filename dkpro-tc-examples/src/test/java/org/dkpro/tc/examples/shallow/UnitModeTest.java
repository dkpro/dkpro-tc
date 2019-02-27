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
package org.dkpro.tc.examples.shallow;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
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
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.shallow.annotators.UnitOutcomeAnnotator;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.features.ngram.CharacterNGram;
import org.dkpro.tc.ml.experiment.ExperimentTrainTest;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.report.RuntimeReport;
import org.dkpro.tc.ml.report.util.Tc2LtlabEvalConverter;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.dkpro.tc.ml.xgboost.XgboostAdapter;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;
import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.Accuracy;
import weka.classifiers.bayes.NaiveBayes;

/**
 * This test just ensures that the experiment runs without throwing any exception.
 */
public class UnitModeTest
    extends TestCaseSuperClass implements Constants
{
    public static final String corpusFilePathTrain = "src/main/resources/data/brown_tei/";
    
    ContextMemoryReport contextReport;

    @Test
    public void testTrainTest() throws Exception
    {
        runExperimentTrainTest();

        assertEquals(0.51, getAccuracy(contextReport.id2outcomeFiles, "Weka"), 0.2);
        assertEquals(0.25, getAccuracy(contextReport.id2outcomeFiles, "Libsvm"), 0.2);
        assertEquals(0.41, getAccuracy(contextReport.id2outcomeFiles, "Liblinear"), 0.2);
        assertEquals(0.32, getAccuracy(contextReport.id2outcomeFiles, "Xgboost"), 0.2);
        
        for(File f  : contextReport.id2outcomeFiles) {
            List<String> lines = FileUtils.readLines(f,
                    "utf-8");
            assertEquals(34, lines.size());

            // line-wise compare
            assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
            assertEquals("#labels 0=AP 1=AT 2=BEDZ 3=BEN 4=CC 5=DT 6=HVD 7=IN 8=JJ 9=MD 10=NN 11=NNS 12=NP 13=PPS 14=RB 15=TO 16=VB 17=VBD 18=VBN 19=WDT 20=pct", lines.get(1));
            // line 2 is a time-stamp
            assertTrue(lines.get(3).matches("[a-zA-Z0-9_,\\.]+=[0-9]+;1[\\.0-9]*;-1"));
            assertTrue(lines.get(4).matches("[a-zA-Z0-9_,\\.]+=[0-9]+;10[\\.0-9]*;-1"));
            assertTrue(lines.get(5).matches("[a-zA-Z0-9_,\\.]+=[0-9]+;20[\\.0-9]*;-1"));
            assertTrue(lines.get(6).matches("[a-zA-Z0-9_,\\.]+=[0-9]+;19[\\.0-9]*;-1"));
            assertTrue(lines.get(7).matches("[a-zA-Z0-9_,\\.]+=[0-9]+;12[\\.0-9]*;-1"));
        }
        
    }
    
    private void runExperimentTrainTest() throws Exception
    {
        contextReport = new ContextMemoryReport();
        
        ExperimentTrainTest experiment = new ExperimentTrainTest("BrownPosDemoCV");
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(getParameterSpace());
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        experiment.addReport(contextReport);
        experiment.addReport(RuntimeReport.class);

        // Run
        Lab.getInstance().run(experiment);
    }
    
    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(UnitOutcomeAnnotator.class);
    }

    private ParameterSpace getParameterSpace() throws Exception {
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                TeiReader.PARAM_PATTERNS, "a01.xml");

        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                TeiReader.PARAM_PATTERNS, "a02.xml");

        dimReaders.put(DIM_READER_TEST, readerTest);

        Map<String, Object> weka = new HashMap<>();
        weka.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new WekaAdapter(), NaiveBayes.class.getName() });
        weka.put(DIM_DATA_WRITER, new WekaAdapter().getDataWriterClass());
        weka.put(DIM_FEATURE_USE_SPARSE, new WekaAdapter().useSparseFeatures());

        Map<String, Object> libsvm = new HashMap<>();
        libsvm.put(DIM_CLASSIFICATION_ARGS, new Object[] { new LibsvmAdapter() });
        libsvm.put(DIM_DATA_WRITER, new LibsvmAdapter().getDataWriterClass());
        libsvm.put(DIM_FEATURE_USE_SPARSE, new LibsvmAdapter().useSparseFeatures());

        Map<String, Object> liblinear = new HashMap<>();
        liblinear.put(DIM_CLASSIFICATION_ARGS, new Object[] { new LiblinearAdapter() });
        liblinear.put(DIM_DATA_WRITER, new LiblinearAdapter().getDataWriterClass());
        liblinear.put(DIM_FEATURE_USE_SPARSE, new LiblinearAdapter().useSparseFeatures());

        Map<String, Object> xgboost = new HashMap<>();
        xgboost.put(DIM_CLASSIFICATION_ARGS, new Object[] { new XgboostAdapter() });
        xgboost.put(DIM_DATA_WRITER, new XgboostAdapter().getDataWriterClass());
        xgboost.put(DIM_FEATURE_USE_SPARSE, new XgboostAdapter().useSparseFeatures());

        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", weka, libsvm,
                liblinear, xgboost);

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(Constants.DIM_FEATURE_SET,
                new TcFeatureSet(TcFeatureFactory.create(TokenRatioPerDocument.class),
                        TcFeatureFactory.create(CharacterNGram.class,
                                CharacterNGram.PARAM_NGRAM_USE_TOP_K, 50)));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle(DIM_READERS, dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_UNIT), dimFeatureSets, mlas);

        return pSpace;
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
}
