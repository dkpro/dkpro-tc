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
package org.dkpro.tc.examples.deeplearning.keras.document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.examples.deeplearning.PythonLocator;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.examples.util.DemoUtils;
import org.junit.Test;

public class KerasDocumentTest extends PythonLocator {
	@Test
	public void runTest() throws Exception {

		DemoUtils.setDkproHome(KerasDocumentTrainTest.class.getSimpleName());

		boolean testConditon = true;
		String python3 = null;
		try {
			python3 = getEnvironment();
		} catch (Exception e) {
			System.err.println("Failed to locate Python with Keras - will skip this test case");
			testConditon = false;
		}
		
		if (testConditon) {
			ParameterSpace ps = KerasDocumentTrainTest.getParameterSpace(python3);
			KerasDocumentTrainTest.runTrainTest(ps);

			assertEquals(1, ContextMemoryReport.id2outcomeFiles.size());

			List<String> lines = FileUtils.readLines(ContextMemoryReport.id2outcomeFiles.get(0), "utf-8");
			assertEquals(11, lines.size());

			// line-wise compare
			assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
			assertEquals("#labels 0=alt.atheism 1=comp.graphics", lines.get(1));
			assertTrue(lines.get(3).matches(".*alt.atheism/53068.txt=[0-9]+;0;-1"));
			assertTrue(lines.get(4).matches(".*alt.atheism/53257.txt=[0-9]+;0;-1"));
			assertTrue(lines.get(5).matches(".*alt.atheism/53260.txt=[0-9]+;0;-1"));
			assertTrue(lines.get(6).matches(".*alt.atheism/53261.txt=[0-9]+;0;-1"));
			assertTrue(lines.get(7).matches(".*comp.graphics/38758.txt=[0-9]+;1;-1"));
			assertTrue(lines.get(8).matches(".*comp.graphics/38761.txt=[0-9]+;1;-1"));
			assertTrue(lines.get(9).matches(".*comp.graphics/38762.txt=[0-9]+;1;-1"));
			assertTrue(lines.get(10).matches(".*comp.graphics/38763.txt=[0-9]+;1;-1"));

			
		}
	}
}
