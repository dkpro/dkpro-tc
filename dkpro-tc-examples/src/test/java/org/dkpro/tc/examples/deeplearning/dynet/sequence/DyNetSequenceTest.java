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
package org.dkpro.tc.examples.deeplearning.dynet.sequence;

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
import org.dkpro.tc.examples.deeplearning.PythonLocator;
import org.dkpro.tc.examples.shallow.annotators.SequenceOutcomeAnnotator;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.ml.experiment.deep.DeepLearningExperimentTrainTest;
import org.dkpro.tc.ml.keras.KerasAdapter;
import org.dkpro.tc.ml.report.TrainTestReport;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;

public class DyNetSequenceTest
    extends PythonLocator
    implements Constants
{
    ContextMemoryReport contextReport;

    public static final String corpusFilePathTrain = "src/main/resources/data/brown_tei/keras";
    public static final String corpusFilePathTest = "src/main/resources/data/brown_tei/keras";

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

            // configure training and test data reader dimension
            Map<String, Object> dimReaders = new HashMap<String, Object>();

            CollectionReaderDescription train = CollectionReaderFactory.createReaderDescription(
                    TeiReader.class, TeiReader.PARAM_LANGUAGE, "en",
                    TeiReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain, TeiReader.PARAM_PATTERNS,
                    "*.xml");
            dimReaders.put(DIM_READER_TRAIN, train);

            // Careful - we need at least 2 sequences in the testing file otherwise
            // things will crash
            CollectionReaderDescription test = CollectionReaderFactory.createReaderDescription(
                    TeiReader.class, TeiReader.PARAM_LANGUAGE, "en",
                    TeiReader.PARAM_SOURCE_LOCATION, corpusFilePathTest, TeiReader.PARAM_PATTERNS,
                    "*.xml");
            dimReaders.put(DIM_READER_TEST, test);

            Map<String, Object> config = new HashMap<>();
            config.put(DIM_CLASSIFICATION_ARGS, new Object[] { new KerasAdapter(),
                    "src/main/resources/dynetCode/dynetPoStagger.py" });

            Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config);

            ParameterSpace pSpace = new ParameterSpace(
                    Dimension.createBundle("readers", dimReaders),
                    Dimension.create(DIM_FEATURE_MODE, Constants.FM_SEQUENCE),
                    Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
                    Dimension.create(DeepLearningConstants.DIM_PYTHON_INSTALLATION, python3),
                    Dimension.create(DeepLearningConstants.DIM_PRETRAINED_EMBEDDINGS,
                            "src/test/resources/wordvector/glove.6B.50d_250.txt"),
                    Dimension.create(DeepLearningConstants.DIM_SEED_VALUE, "1234"),
                    Dimension.create(DeepLearningConstants.DIM_RAM_WORKING_MEMORY, "4096"),
                    Dimension.create(DeepLearningConstants.DIM_VECTORIZE_TO_INTEGER, false), 
                    mlas);

            DeepLearningExperimentTrainTest experiment = new DeepLearningExperimentTrainTest(
                    "DynetSeq2Seq");
            experiment.setParameterSpace(pSpace);
            experiment.setPreprocessing(getPreprocessing());
            experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
            experiment.addReport(contextReport);
            experiment.addReport(new TrainTestReport());
            Lab.getInstance().run(experiment);

            assertEquals(1, contextReport.id2outcomeFiles.size());

            List<String> lines = FileUtils.readLines(contextReport.id2outcomeFiles.get(0), "utf-8");
            assertEquals(51, lines.size());

            // line-wise compare
            assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
            assertEquals(
                    "#labels 0=AP 1=AT 2=BER 3=CC 4=CS 5=DOD 6=DTS 7=HV 8=IN 9=JJ 10=NN 11=NNS 12=NP 13=NPg 14=PPO 15=PPS 16=RB 17=TO 18=VB 19=VBD 20=VBG 21=VBN 22=pct",
                    lines.get(1));
            assertTrue(lines.get(3).matches("000000_000000_000000_The=[0-9]+;1;-1"));
            assertTrue(lines.get(4).matches("000000_000000_000001_jury=[0-9]+;10;-1"));
            assertTrue(lines.get(5).matches("000000_000000_000002_said=[0-9]+;19;-1"));
            assertTrue(lines.get(6).matches("000000_000000_000003_it=[0-9]+;15;-1"));
            assertTrue(lines.get(7).matches("000000_000000_000004_did=[0-9]+;5;-1"));
            assertTrue(lines.get(8).matches("000000_000000_000005_find=[0-9]+;18;-1"));
            assertTrue(lines.get(9).matches("000000_000000_000006_that=[0-9]+;4;-1"));
            assertTrue(lines.get(10).matches("000000_000000_000007_many=[0-9]+;0;-1"));
            assertTrue(lines.get(11).matches("000000_000000_000008_of=[0-9]+;8;-1"));
            assertTrue(lines.get(12).matches("000000_000000_000009_Georgia's=[0-9]+;13;-1"));
            assertTrue(lines.get(13).matches("000000_000000_000010_registration=[0-9]+;10;-1"));
            assertTrue(lines.get(14).matches("000000_000000_000011_and=[0-9]+;3;-1"));
            assertTrue(lines.get(15).matches("000000_000000_000012_election=[0-9]+;10;-1"));
            assertTrue(lines.get(16).matches("000000_000000_000013_laws=[0-9]+;11;-1"));
            assertTrue(lines.get(17).matches("000000_000000_000014_``=[0-9]+;22;-1"));
            assertTrue(lines.get(18).matches("000000_000000_000015_are=[0-9]+;2;-1"));
            assertTrue(lines.get(19).matches("000000_000000_000016_outmoded=[0-9]+;9;-1"));
            assertTrue(lines.get(20).matches("000000_000000_000017_or=[0-9]+;3;-1"));
            assertTrue(lines.get(21).matches("000000_000000_000018_inadequate=[0-9]+;9;-1"));
            assertTrue(lines.get(22).matches("000000_000000_000019_and=[0-9]+;3;-1"));
            assertTrue(lines.get(23).matches("000000_000000_000020_often=[0-9]+;16;-1"));
            assertTrue(lines.get(24).matches("000000_000000_000021_ambiguous=[0-9]+;9;-1"));
            assertTrue(lines.get(25).matches("000000_000000_000022_''=[0-9]+;22;-1"));
            assertTrue(lines.get(26).matches("000000_000000_000023_.=[0-9]+;22;-1"));
            assertTrue(lines.get(27).matches("000000_000001_000000_It=[0-9]+;15;-1"));
            assertTrue(lines.get(28).matches("000000_000001_000001_recommended=[0-9]+;19;-1"));
            assertTrue(lines.get(29).matches("000000_000001_000002_that=[0-9]+;4;-1"));
            assertTrue(lines.get(30).matches("000000_000001_000003_Fulton=[0-9]+;12;-1"));
            assertTrue(lines.get(31).matches("000000_000001_000004_legislators=[0-9]+;11;-1"));
            assertTrue(lines.get(32).matches("000000_000001_000005_act=[0-9]+;18;-1"));
            assertTrue(lines.get(33).matches("000000_000001_000006_``=[0-9]+;22;-1"));
            assertTrue(lines.get(34).matches("000000_000001_000007_to=[0-9]+;17;-1"));
            assertTrue(lines.get(35).matches("000000_000001_000008_have=[0-9]+;7;-1"));
            assertTrue(lines.get(36).matches("000000_000001_000009_these=[0-9]+;6;-1"));
            assertTrue(lines.get(37).matches("000000_000001_000010_laws=[0-9]+;11;-1"));
            assertTrue(lines.get(38).matches("000000_000001_000011_studied=[0-9]+;21;-1"));
            assertTrue(lines.get(39).matches("000000_000001_000012_and=[0-9]+;3;-1"));
            assertTrue(lines.get(40).matches("000000_000001_000013_revised=[0-9]+;21;-1"));
            assertTrue(lines.get(41).matches("000000_000001_000014_to=[0-9]+;8;-1"));
            assertTrue(lines.get(42).matches("000000_000001_000015_the=[0-9]+;1;-1"));
            assertTrue(lines.get(43).matches("000000_000001_000016_end=[0-9]+;10;-1"));
            assertTrue(lines.get(44).matches("000000_000001_000017_of=[0-9]+;8;-1"));
            assertTrue(lines.get(45).matches("000000_000001_000018_modernizing=[0-9]+;20;-1"));
            assertTrue(lines.get(46).matches("000000_000001_000019_and=[0-9]+;3;-1"));
            assertTrue(lines.get(47).matches("000000_000001_000020_improving=[0-9]+;20;-1"));
            assertTrue(lines.get(48).matches("000000_000001_000021_them=[0-9]+;14;-1"));
            assertTrue(lines.get(49).matches("000000_000001_000022_''=[0-9]+;22;-1"));
            assertTrue(lines.get(50).matches("000000_000001_000023_.=[0-9]+;22;-1"));
        }
    }

    protected static AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(SequenceOutcomeAnnotator.class);
    }
}
