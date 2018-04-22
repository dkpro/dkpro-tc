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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.shallow.annotators.SequenceOutcomeAnnotator;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.features.style.InitialCharacterUpperCase;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.crfsuite.CrfSuiteAdapter;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.report.util.Tc2LtlabEvalConverter;
import org.dkpro.tc.ml.svmhmm.SvmHmmAdapter;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;
import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.Accuracy;

/**
 * This test just ensures that the experiment runs without throwing any exception.
 */
public class SequenceTest
    extends TestCaseSuperClass implements Constants
{
    public static final String corpusFilePath = "src/main/resources/data/brown_tei/";

    @Test
    public void testSequence() throws Exception
    {
        runExperiment();

        assertEquals(2, ContextMemoryReport.id2outcomeFiles.size());
        assertEquals(0.22, getAccuracy(ContextMemoryReport.id2outcomeFiles, "Crfsuite"), 0.1);
        assertEquals(0.1, getAccuracy(ContextMemoryReport.id2outcomeFiles, "SvmHmm"), 0.1);
        
        verifyCrfSuite(getId2Outcome(ContextMemoryReport.id2outcomeFiles, "Crfsuite"));
        verifySvmHmm(getId2Outcome(ContextMemoryReport.id2outcomeFiles, "SvmHmm"));
    }
    
    private void verifySvmHmm(File f) throws Exception
    {
        List<String> lines = FileUtils.readLines(f, "utf-8");
        assertEquals(34, lines.size());

        assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
        assertEquals(
                "#labels 1=AP 2=AT 3=BEDZ 4=BEN 5=CC 6=DT 7=HVD 8=IN 9=JJ 10=MD 11=NN 12=NNS 13=NP 14=PPS 15=RB 16=TO 17=VB 18=VBD 19=VBN 20=WDT 21=pct",
                lines.get(1));
        // 2nd line time stamp

        assertTrue(lines.get(3).matches(".*=[0-9]+;[0-9]+;-1"));
        assertTrue(lines.get(4).matches(".*=[0-9]+;[0-9]+;-1"));
        assertTrue(lines.get(5).matches(".*=[0-9]+;[0-9]+;-1"));
        assertTrue(lines.get(6).matches(".*=[0-9]+;[0-9]+;-1"));
        assertTrue(lines.get(7).matches(".*=[0-9]+;[0-9]+;-1"));           
    }

    private void verifyCrfSuite(File f) throws Exception
    {
        List<String> lines = FileUtils.readLines(f, "utf-8");
        assertEquals(34, lines.size());

        assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
        assertEquals(
                "#labels 0=NN 1=JJ 2=pct 3=CC 4=NP 5=VBN 6=IN 7=WDT 8=BEDZ 9=BEN 10=VB 11=AP 12=RB 13=DT 14=NNS 15=PPS 16=AT 17=HVD 18=MD 19=VBD 20=TO 21=%28null%29",
                lines.get(1));
        // 2nd line time stamp

        assertTrue(lines.get(3).matches("0000_0000_0000_The=[0-9]+;[0-9]+;-1"));
        assertTrue(lines.get(4).matches("0000_0000_0001_bill=[0-9]+;[0-9]+;-1"));
        assertTrue(lines.get(5).matches("0000_0000_0002_,=[0-9]+;[0-9]+;-1"));
        assertTrue(lines.get(6).matches("0000_0000_0003_which=[0-9]+;[0-9]+;-1"));
        assertTrue(lines.get(7).matches("0000_0000_0004_Daniel=[0-9]+;[0-9]+;-1"));        
    }

    private void runExperiment() throws Exception
    {
        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, corpusFilePath,
                TeiReader.PARAM_PATTERNS, "a01.xml");

        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, corpusFilePath,
                TeiReader.PARAM_PATTERNS, "a02.xml");

        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, readerTrain);
        dimReaders.put(DIM_READER_TEST, readerTest);

        Map<String, Object> crfsuite = new HashMap<>();
        crfsuite.put(DIM_CLASSIFICATION_ARGS, new Object[] { new CrfSuiteAdapter(),
                CrfSuiteAdapter.ALGORITHM_LBFGS, "max_iterations=5" });
        crfsuite.put(DIM_DATA_WRITER, new CrfSuiteAdapter().getDataWriterClass());
        crfsuite.put(DIM_FEATURE_USE_SPARSE, new CrfSuiteAdapter().useSparseFeatures());
        
        
        Map<String, Object> svmHmm = new HashMap<>();
        svmHmm.put(DIM_CLASSIFICATION_ARGS, new Object[] { new SvmHmmAdapter(),
                "-c", "1000", "-e", "100" });
        svmHmm.put(DIM_DATA_WRITER, new SvmHmmAdapter().getDataWriterClass());
        svmHmm.put(DIM_FEATURE_USE_SPARSE, new SvmHmmAdapter().useSparseFeatures());
        
        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", crfsuite, svmHmm);

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                new TcFeatureSet(TcFeatureFactory.create(TokenRatioPerDocument.class),
                        TcFeatureFactory.create(InitialCharacterUpperCase.class)));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, Constants.FM_SEQUENCE), dimFeatureSets, mlas);
        
        ExperimentTrainTest experiment = new ExperimentTrainTest(
                "NamedEntitySequenceDemoTrainTest");
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(pSpace);
        experiment.addReport(BatchTrainTestReport.class);
        experiment.addReport(ContextMemoryReport.class);
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        
        Lab.getInstance().run(experiment);
        
    }
    
    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(SequenceOutcomeAnnotator.class);
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
    
    private File getId2Outcome(List<File> id2outcomeFiles, String simpleName) throws Exception
    {

        for (File f : id2outcomeFiles) {
            if (f.getAbsolutePath().toLowerCase().contains(simpleName.toLowerCase())) {
               return f;
            }
        }

        return null;
    }
}
