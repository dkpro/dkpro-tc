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
package org.dkpro.tc.examples.deeplearning.dl4j.sequence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.deeplearning.dl4j.sequence.DeepLearningDl4jSeq2SeqTrainTest;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.examples.util.LabFolderTrackerReport;
import org.junit.Before;
import org.junit.Test;

public class DeepLearning4jSequenceTest extends TestCaseSuperClass
{
    LabFolderTrackerReport folderTracker;
    
    @Before
	public void setup() throws Exception {
		super.setup();
		folderTracker = new LabFolderTrackerReport();
	}
    
    @Test
    public void runSequenceTest()
        throws Exception
    {
        DeepLearningDl4jSeq2SeqTrainTest dl4j = new DeepLearningDl4jSeq2SeqTrainTest();
        dl4j.runTrainTest(DeepLearningDl4jSeq2SeqTrainTest.getParameterSpace(), folderTracker);

        List<String> vocabulary = getPreparationVocabulary();
        assertEquals(18, vocabulary.size());

        List<String> outcomes = getPreparationOutcomes();
        assertEquals(15, outcomes.size());
        assertTrue(assertOutcomes(outcomes));

        String vecTrain = getVectorizationTrainData();
        String vecOutcome = getVectorizationTrainOutcome();

        assertEquals(26, vecTrain.split(" ").length);
        assertEquals(26, vecOutcome.split(" ").length);

        assertTrue(compareContent(Arrays.asList(vecTrain.replaceAll("\n", " ").split(" "))));
        
        
    	List<String> lines = FileUtils.readLines(ContextMemoryReport.id2outcomeFiles.get(0), "utf-8");
		assertEquals(30, lines.size());
		// line-wise compare
		assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
		assertEquals("#labels 0=AP 1=AT 2=BER 3=CC 4=CS 5=DOD 6=DTS 7=HV 8=IN 9=NN 10=PPO 11=PPS 12=TO 13=VBD 14=pct", lines.get(1));
		assertTrue(lines.get(3).matches("000000_000000_000000_said=[0-9]+;13;-1"));
		assertTrue(lines.get(4).matches("000000_000000_000001_it=[0-9]+;11;-1"));
		assertTrue(lines.get(5).matches("000000_000000_000002_did=[0-9]+;5;-1"));
		assertTrue(lines.get(6).matches("000000_000000_000003_that=[0-9]+;4;-1"));
		assertTrue(lines.get(7).matches("000000_000000_000004_many=[0-9]+;0;-1"));
		assertTrue(lines.get(8).matches("000000_000000_000005_of=[0-9]+;8;-1"));
		assertTrue(lines.get(9).matches("000000_000000_000006_and=[0-9]+;3;-1"));
		assertTrue(lines.get(10).matches("000000_000000_000007_``=[0-9]+;14;-1"));
		assertTrue(lines.get(11).matches("000000_000000_000008_are=[0-9]+;2;-1"));
		assertTrue(lines.get(12).matches("000000_000000_000009_or=[0-9]+;3;-1"));
		assertTrue(lines.get(13).matches("000000_000000_000010_and=[0-9]+;3;-1"));
		assertTrue(lines.get(14).matches("000000_000000_000011_''=[0-9]+;14;-1"));
		assertTrue(lines.get(15).matches("000000_000000_000012_.=[0-9]+;14;-1"));
		assertTrue(lines.get(16).matches("000000_000001_000000_that=[0-9]+;4;-1"));
		assertTrue(lines.get(17).matches("000000_000001_000001_``=[0-9]+;14;-1"));
		assertTrue(lines.get(18).matches("000000_000001_000002_to=[0-9]+;12;-1"));
		assertTrue(lines.get(19).matches("000000_000001_000003_have=[0-9]+;7;-1"));
		assertTrue(lines.get(20).matches("000000_000001_000004_these=[0-9]+;6;-1"));
		assertTrue(lines.get(21).matches("000000_000001_000005_and=[0-9]+;3;-1"));
		assertTrue(lines.get(22).matches("000000_000001_000006_to=[0-9]+;8;-1"));
		assertTrue(lines.get(23).matches("000000_000001_000007_the=[0-9]+;1;-1"));
		assertTrue(lines.get(24).matches("000000_000001_000008_end=[0-9]+;9;-1"));
		assertTrue(lines.get(25).matches("000000_000001_000009_of=[0-9]+;8;-1"));
		assertTrue(lines.get(26).matches("000000_000001_000010_and=[0-9]+;3;-1"));
		assertTrue(lines.get(27).matches("000000_000001_000011_them=[0-9]+;10;-1"));
		assertTrue(lines.get(28).matches("000000_000001_000012_''=[0-9]+;14;-1"));
		assertTrue(lines.get(29).matches("000000_000001_000013_.=[0-9]+;14;-1"));

    }

    private boolean assertOutcomes(List<String> outcomes)
    {
        List<String> expected = new ArrayList<>();
        expected.add("AP");
        expected.add("AT");
        expected.add("BER");
        expected.add("CC");
        expected.add("CS");
        expected.add("DOD");
        expected.add("DTS");
        expected.add("HV");
        expected.add("IN");
        expected.add("NN");
        expected.add("PPO");
        expected.add("PPS");
        expected.add("TO");
        expected.add("VBD");
        expected.add("pct");

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), outcomes.get(i).trim());
        }

        return true;
    }

    private List<String> getPreparationOutcomes()
        throws IOException
    {
        File f = new File(folderTracker.preparationTask + "/output/"
                + DeepLearningConstants.FILENAME_OUTCOMES);
        return FileUtils.readLines(f, "utf-8");
    }

    private List<String> getPreparationVocabulary()
        throws IOException
    {
        File f = new File(folderTracker.preparationTask + "/output/"
                + DeepLearningConstants.FILENAME_VOCABULARY);
        return FileUtils.readLines(f, "utf-8");
    }

    private boolean compareContent(List<String> content)
    {
        List<String> expected = new ArrayList<>();
        expected.add("said");
        expected.add("it");
        expected.add("did");
        expected.add("that");
        expected.add("many");
        expected.add("of");
        expected.add("and");
        expected.add("``");
        expected.add("are");
        expected.add("or");
        expected.add("and");
        expected.add("''");
        expected.add(".");
        expected.add("that");
        expected.add("``");
        expected.add("to");
        expected.add("have");
        expected.add("these");
        expected.add("and");
        expected.add("to");
        expected.add("the");
        expected.add("end");
        expected.add("of");
        expected.add("and");
        expected.add("them");
        expected.add("''");

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), content.get(i).trim());
        }

        return true;
    }

    private String getVectorizationTrainOutcome()
        throws IOException
    {
        File f = new File(folderTracker.vectorizationTaskTrain + "/output/"
                + DeepLearningConstants.FILENAME_OUTCOME_VECTOR);
        return FileUtils.readFileToString(f, "utf-8");
    }

    private String getVectorizationTrainData()
        throws IOException
    {
        File f = new File(folderTracker.vectorizationTaskTrain + "/output/"
                + DeepLearningConstants.FILENAME_INSTANCE_VECTOR);
        return FileUtils.readFileToString(f, "utf-8");
    }

}
