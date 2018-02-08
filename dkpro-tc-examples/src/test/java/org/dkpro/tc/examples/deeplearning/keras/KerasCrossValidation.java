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
package org.dkpro.tc.examples.deeplearning.keras;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.deeplearning.PythonLocator;
import org.dkpro.tc.examples.util.CvContextMemoryReport;
import org.dkpro.tc.examples.util.DemoUtils;
import org.junit.Test;

public class KerasCrossValidation extends PythonLocator {
	@Test
	public void runTest() throws Exception {

		DemoUtils.setDkproHome(KerasDocumentCrossValidation.class.getSimpleName());

		boolean testConditon = true;
		String python3 = null;
		try {
			python3 = getEnvironment();
		} catch (Exception e) {
			System.err.println("Failed to locate Python with Keras - will skip this test case");
			testConditon = false;
		}

		if (testConditon) {
			ParameterSpace ps = KerasDocumentCrossValidation.getParameterSpace(python3);
			KerasDocumentCrossValidation.runCrossValidation(ps);
			
			assertEquals(2, CvContextMemoryReport.mlaAdapters);
			assertTrue(new File(CvContextMemoryReport.mlaAdapters.get(0)+"/" + Constants.ID_OUTCOME_KEY).exists());
			assertTrue(new File(CvContextMemoryReport.mlaAdapters.get(1)+"/" + Constants.ID_OUTCOME_KEY).exists());
		}
	}
}
