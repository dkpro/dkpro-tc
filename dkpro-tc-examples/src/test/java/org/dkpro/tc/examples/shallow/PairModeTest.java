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

import java.io.File;
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
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.shallow.io.STSReader;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.features.pair.core.length.DiffNrOfTokensPairFeatureExtractor;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.report.util.Tc2LtlabEvalConverter;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.dkpro.tc.ml.weka.task.WekaTestTask;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.evaluation.measures.regression.MeanAbsoluteError;
import weka.classifiers.functions.SMOreg;
import weka.core.SerializationHelper;

/**
 * This test just ensures that the experiment runs without throwing any exception.
 */
public class PairModeTest
    extends TestCaseSuperClass
    implements Constants
{
    public static final String LANGUAGE_CODE = "en";
    public static final String inputFileTrain = "src/main/resources/data/sts2012/STS.input.MSRpar.txt";
    public static final String goldFileTrain = "src/main/resources/data/sts2012/STS.gs.MSRpar.txt";

    public static final String inputFileTest = "src/main/resources/data/sts2012/STS.input.MSRvid.txt";
    public static final String goldFileTest = "src/main/resources/data/sts2012/STS.gs.MSRvid.txt";

    ContextMemoryReport contextReport;
    
    @Test
    public void testTrainTest() throws Exception
    {
        runExperimentTrainTest();

        // weka offers to calculate this value too - we take weka as "reference" value
        weka.classifiers.Evaluation eval = (weka.classifiers.Evaluation) SerializationHelper
                .read(new File(contextReport.id2outcomeFiles.get(0).getParent() + "/"
                        + WekaTestTask.evaluationBin).getAbsolutePath());
        double wekaMeanAbsoluteError = eval.meanAbsoluteError();

        MeanAbsoluteError mae = new MeanAbsoluteError(Tc2LtlabEvalConverter
                .convertRegressionModeId2Outcome(contextReport.id2outcomeFiles.get(0)));

        assertEquals(wekaMeanAbsoluteError, mae.getResult(), 0.1);
    }

    private void runExperimentTrainTest() throws Exception
    {
        contextReport = new ContextMemoryReport();
        
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, getTrainReader());
        dimReaders.put(DIM_READER_TEST, getTestReader());

        Map<String, Object> weka = new HashMap<>();
        weka.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new WekaAdapter(), SMOreg.class.getName() });
        weka.put(DIM_DATA_WRITER, new WekaAdapter().getDataWriterClass());
        weka.put(DIM_FEATURE_USE_SPARSE, new WekaAdapter().useSparseFeatures());

        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", weka);

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET, new TcFeatureSet(
                TcFeatureFactory.create(DiffNrOfTokensPairFeatureExtractor.class)));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, Constants.LM_REGRESSION),
                Dimension.create(DIM_FEATURE_MODE, Constants.FM_PAIR), dimFeatureSets, mlas);

        ExperimentTrainTest experiment = new ExperimentTrainTest(
                "NamedEntitySequenceDemoTrainTest");
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(pSpace);
        experiment.addReport(BatchTrainTestReport.class);
        experiment.addReport(contextReport);
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

        Lab.getInstance().run(experiment);
    }

    public CollectionReaderDescription getTrainReader() throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(STSReader.class,
                STSReader.PARAM_INPUT_FILE, inputFileTrain, STSReader.PARAM_GOLD_FILE,
                goldFileTrain);
    }

    public CollectionReaderDescription getTestReader() throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(STSReader.class,
                STSReader.PARAM_INPUT_FILE, inputFileTest, STSReader.PARAM_GOLD_FILE, goldFileTest);
    }

    public static AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class));
    }
}
