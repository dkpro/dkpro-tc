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
package org.dkpro.tc.examples.shallow.xgboost.regression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.junit.Before;
import org.junit.Test;

/**
 * This test just ensures that the experiment runs without throwing any
 * exception.
 */
public class XgboostRegressionTest extends TestCaseSuperClass {
	XgboostRegression javaExperiment;
	ParameterSpace pSpace;

	@Before
	public void setup() throws Exception {
		super.setup();

		javaExperiment = new XgboostRegression();
		pSpace = XgboostRegression.getParameterSpace();
	}

	@Test
	public void testJavaTrainTest() throws Exception {
		javaExperiment.runTrainTest(pSpace);

		assertEquals(1, ContextMemoryReport.id2outcomeFiles.size());

		List<String> lines = FileUtils.readLines(ContextMemoryReport.id2outcomeFiles.get(0), "utf-8");
		assertEquals(53, lines.size());

		// line-wise compare
		assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
		assertEquals("#labels ", lines.get(1));
		assertTrue(lines.get(3).matches("0=[0-9\\.]+;1;-1"));
		assertTrue(lines.get(4).matches("1=[0-9\\.]+;1;-1"));
		assertTrue(lines.get(5).matches("10=[0-9\\.]+;7;-1"));
		assertTrue(lines.get(6).matches("11=[0-9\\.]+;3;-1"));
		assertTrue(lines.get(7).matches("12=[0-9\\.]+;2;-1"));
		assertTrue(lines.get(8).matches("13=[0-9\\.]+;2;-1"));
		assertTrue(lines.get(9).matches("14=[0-9\\.]+;2;-1"));
		assertTrue(lines.get(10).matches("15=[0-9\\.]+;1;-1"));
	}

	 
}
