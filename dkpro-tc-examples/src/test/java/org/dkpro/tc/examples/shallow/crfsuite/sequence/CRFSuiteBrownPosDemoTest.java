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
package org.dkpro.tc.examples.shallow.crfsuite.sequence;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.shallow.crfsuite.sequence.filter.FilterLuceneCharacterNgramStartingWithLetter;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.ml.crfsuite.CrfSuiteAdapter;
import org.junit.Before;
import org.junit.Test;

/**
 * This test just ensures that the experiment runs without throwing any
 * exception.
 */
public class CRFSuiteBrownPosDemoTest  extends TestCaseSuperClass {
	CRFSuiteBrownPosDemoSimpleDkproReader javaExperiment;

	@Before
	public void setup() throws Exception {
		super.setup();
		javaExperiment = new CRFSuiteBrownPosDemoSimpleDkproReader();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void runTrainTestNoFilter() throws Exception {
		// Random parameters for demonstration!
        //Number of iterations is set to an extreme low value (remove --> default: 100 iterations, or set accordingly)
		Dimension<List<Object>> dimClassificationArgs = Dimension.create(Constants.DIM_CLASSIFICATION_ARGS,
				asList(new Object[] { new CrfSuiteAdapter(), CrfSuiteAdapter.ALGORITHM_LBFGS, "-p", "max_iterations=5"}));
        ParameterSpace pSpace = CRFSuiteBrownPosDemoSimpleDkproReader.getParameterSpace(Constants.FM_SEQUENCE,
				Constants.LM_SINGLE_LABEL, dimClassificationArgs, null);

		javaExperiment.runTrainTest(pSpace);
		
		assertEquals(1, ContextMemoryReport.id2outcomeFiles.size());

		List<String> lines = FileUtils.readLines(ContextMemoryReport.id2outcomeFiles.get(0), "utf-8");
		assertEquals(34, lines.size());
		
		assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
		assertEquals("#labels 0=NN 1=JJ 2=NP 3=DTS 4=BEDZ 5=HV 6=PPO 7=DT 8=NNS 9=PPS 10=JJT 11=ABX 12=MD 13=DOD 14=VBD 15=VBG 16=QL 32=%28null%29 17=pct 18=CC 19=VBN 20=NPg 21=IN 22=WDT 23=BEN 24=VB 25=BER 26=AP 27=RB 28=CS 29=AT 30=HVD 31=TO", lines.get(1));
		// 2nd line time stamp
		
		// Crfsuite results are sensitive to some extend to the platform, to
		// account for this sensitivity we check only that the "prediction"
		// field is filled with any number but do not test for a specific value
		assertTrue(lines.get(3).matches("0000_0000_0000_The=[0-9]+;13;-1"));
		assertTrue(lines.get(4).matches("0000_0000_0001_bill=[0-9]+;0;-1"));
		assertTrue(lines.get(5).matches("0000_0000_0002_,=[0-9]+;1;-1"));
		assertTrue(lines.get(6).matches("0000_0000_0003_which=[0-9]+;7;-1"));
		assertTrue(lines.get(7).matches("0000_0000_0004_Daniel=[0-9]+;4;-1"));
		assertTrue(lines.get(8).matches("0000_0000_0005_said=[0-9]+;14;-1"));
		assertTrue(lines.get(9).matches("0000_0000_0006_he=[0-9]+;11;-1"));
		assertTrue(lines.get(10).matches("0000_0000_0007_drafted=[0-9]+;14;-1"));
		assertTrue(lines.get(11).matches("0000_0000_0008_personally=[0-9]+;10;-1"));
		assertTrue(lines.get(12).matches("0000_0000_0009_,=[0-9]+;1;-1"));
		assertTrue(lines.get(13).matches("0000_0000_0010_would=[0-9]+;16;-1"));
		assertTrue(lines.get(14).matches("0000_0000_0011_force=[0-9]+;8;-1"));
		assertTrue(lines.get(15).matches("0000_0000_0012_banks=[0-9]+;12;-1"));
		assertTrue(lines.get(16).matches("0000_0000_0013_,=[0-9]+;1;-1"));
		assertTrue(lines.get(17).matches("0000_0000_0014_insurance=[0-9]+;0;-1"));
		assertTrue(lines.get(18).matches("0000_0000_0015_firms=[0-9]+;12;-1"));
		assertTrue(lines.get(19).matches("0000_0000_0016_,=[0-9]+;1;-1"));
		assertTrue(lines.get(20).matches("0000_0000_0017_pipeline=[0-9]+;0;-1"));
		assertTrue(lines.get(21).matches("0000_0000_0018_companies=[0-9]+;12;-1"));
		assertTrue(lines.get(22).matches("0000_0000_0019_and=[0-9]+;3;-1"));
		assertTrue(lines.get(23).matches("0000_0000_0020_other=[0-9]+;9;-1"));
		assertTrue(lines.get(24).matches("0000_0000_0021_corporations=[0-9]+;12;-1"));
		assertTrue(lines.get(25).matches("0000_0000_0022_to=[0-9]+;15;-1"));
		assertTrue(lines.get(26).matches("0000_0000_0023_report=[0-9]+;8;-1"));
		assertTrue(lines.get(27).matches("0000_0000_0024_such=[0-9]+;2;-1"));
		assertTrue(lines.get(28).matches("0000_0000_0025_property=[0-9]+;0;-1"));
		assertTrue(lines.get(29).matches("0000_0000_0026_to=[0-9]+;6;-1"));
		assertTrue(lines.get(30).matches("0000_0000_0027_the=[0-9]+;13;-1"));
		assertTrue(lines.get(31).matches("0000_0000_0028_state=[0-9]+;0;-1"));
		assertTrue(lines.get(32).matches("0000_0000_0029_treasurer=[0-9]+;0;-1"));
		assertTrue(lines.get(33).matches("0000_0000_0030_.=[0-9]+;1;-1"));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void runTrainTestFilter() throws Exception {
		// Random parameters for demonstration!
		Dimension<List<Object>> dimClassificationArgs = Dimension.create(Constants.DIM_CLASSIFICATION_ARGS,
				asList(new CrfSuiteAdapter(), CrfSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR));

		Dimension<List<String>> dimFilter = Dimension.create(Constants.DIM_FEATURE_FILTERS,
				asList(FilterLuceneCharacterNgramStartingWithLetter.class.getName()));

		ParameterSpace pSpace = CRFSuiteBrownPosDemoSimpleDkproReader.getParameterSpace(Constants.FM_SEQUENCE,
				Constants.LM_SINGLE_LABEL, dimClassificationArgs, dimFilter);

		javaExperiment.runTrainTest(pSpace);
		
		
	}
}
