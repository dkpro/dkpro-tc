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
package org.dkpro.tc.examples.shallow.svmhmm.sequence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.ml.report.util.Tc2LtlabEvalConverter;
import org.junit.Before;
import org.junit.Test;

import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.Accuracy;

/**
 * This test just ensures that the experiment runs without throwing any
 * exception.
 */
public class SVMHMMBrownPosDemoTest extends TestCaseSuperClass {
	SvmHmmBrownPosDemo javaExperiment;
	ParameterSpace pSpace;

	@Before
	public void setup() throws Exception {
		super.setup();

		javaExperiment = new SvmHmmBrownPosDemo();
	}

	@Test
	public void testSvmHmm() throws Exception {

		pSpace = SvmHmmBrownPosDemo.getParameterSpace();

		javaExperiment.runTrainTest(pSpace);

		File inputFile = ContextMemoryReport.id2outcomeFiles.get(0);
		System.out.println(inputFile.getAbsolutePath());
		EvaluationData<String> data = Tc2LtlabEvalConverter
				.convertSingleLabelModeId2Outcome(pathVerification(inputFile));
		Accuracy<String> acc = new Accuracy<String>(data);

		assertEquals(0.5, acc.getResult(), 0.05);
		
		assertEquals(1, ContextMemoryReport.id2outcomeFiles.size());
		List<String> lines = FileUtils.readLines(ContextMemoryReport.id2outcomeFiles.get(0), "utf-8");
		assertEquals(34, lines.size());
		// line-wise compare
		assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
		assertEquals(
				"#labels 1=ABX 2=AP 3=AT 4=BEDZ 5=BEN 6=BER 7=CC 8=CS 9=DOD 10=DT 11=DTS 12=HV 13=HVD 14=IN 15=JJ 16=JJT 17=MD 18=NN 19=NNS 20=NP 21=NPg 22=PPO 23=PPS 24=QL 25=RB 26=TO 27=VB 28=VBD 29=VBG 30=VBN 31=WDT 32=pct",
				lines.get(1));
		assertTrue(lines.get(3).matches("0=[0-9]+;7;-1"));
		assertTrue(lines.get(4).matches("1=[0-9]+;2;-1"));
		assertTrue(lines.get(5).matches("10=[0-9]+;32;-1"));
		assertTrue(lines.get(6).matches("11=[0-9]+;17;-1"));
		assertTrue(lines.get(7).matches("12=[0-9]+;27;-1"));
		assertTrue(lines.get(8).matches("13=[0-9]+;19;-1"));
		assertTrue(lines.get(9).matches("14=[0-9]+;32;-1"));
		assertTrue(lines.get(10).matches("15=[0-9]+;18;-1"));
		assertTrue(lines.get(11).matches("16=[0-9]+;19;-1"));
		assertTrue(lines.get(12).matches("17=[0-9]+;32;-1"));
		assertTrue(lines.get(13).matches("18=[0-9]+;18;-1"));
		assertTrue(lines.get(14).matches("19=[0-9]+;19;-1"));
		assertTrue(lines.get(15).matches("2=[0-9]+;19;-1"));
		assertTrue(lines.get(16).matches("20=[0-9]+;18;-1"));
		assertTrue(lines.get(17).matches("21=[0-9]+;32;-1"));
		assertTrue(lines.get(18).matches("22=[0-9]+;3;-1"));
	}

	private File pathVerification(File file) {

		if (file.exists()) {
			return file;
		}

		File fileA = new File(file.getAbsolutePath().replaceAll("\\\\", "\\"));
		if (fileA.exists()) {
			return fileA;
		}

		File fileB = new File(file.getAbsolutePath().replaceAll("\\\\", "/"));
		if (fileB.exists()) {
			return fileB;
		}

		throw new IllegalStateException("File not found tried following paths [" + file.getAbsolutePath() + "] ["
				+ fileA.getAbsolutePath() + "] [" + fileB.getAbsolutePath() + "]");
	}
}
