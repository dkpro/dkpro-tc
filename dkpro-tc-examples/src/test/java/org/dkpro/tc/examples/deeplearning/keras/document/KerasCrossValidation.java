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

import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.examples.deeplearning.PythonLocator;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.examples.util.DemoUtils;
import org.junit.Test;

public class KerasCrossValidation
    extends PythonLocator
{
    @Test
    public void runTest() throws Exception
    {

        DemoUtils.setDkproHome(KerasDocumentCrossValidation.class.getSimpleName());

        boolean testConditon = true;
        String python3 = null;
        python3 = getEnvironment();

        if (python3 == null) {
            System.err.println("Failed to locate Python with Keras - will skip this test case");
            testConditon = false;
        }

        if (testConditon) {
            ParameterSpace ps = KerasDocumentCrossValidation.getParameterSpace(python3);
            KerasDocumentCrossValidation.runCrossValidation(ps);

            // The comined file in the CV folder + 2 single files in the per-fold folder
            assertEquals(3, ContextMemoryReport.id2outcomeFiles.size());
            assertTrue(ContextMemoryReport.id2outcomeFiles.get(0).exists());
            assertTrue(ContextMemoryReport.id2outcomeFiles.get(1).exists());
            assertTrue(ContextMemoryReport.id2outcomeFiles.get(2).exists());
        }
    }
}
