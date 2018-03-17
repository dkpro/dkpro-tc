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
package org.dkpro.tc.examples.deeplearning.dl4j.document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.deeplearning.dl4j.document.DeepLearning4jDocumentTrainTest;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.junit.Test;

public class DeepLearning4jDocumentTest
    extends TestCaseSuperClass
{
    @Test
    public void runDocumentTest() throws Exception
    {
        DeepLearning4jDocumentTrainTest dl4j = new DeepLearning4jDocumentTrainTest();
        dl4j.runTrainTest(DeepLearning4jDocumentTrainTest.getParameterSpace());

        List<String> lines = FileUtils.readLines(ContextMemoryReport.id2outcomeFiles.get(0),
                "utf-8");
        assertEquals(203, lines.size());
        // line-wise compare
        assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
        assertEquals("#labels 0=bollywood 1=business 2=crime 3=politics", lines.get(1));
        assertTrue(lines.get(3).matches("1=[0-9]+;1;-1"));
        assertTrue(lines.get(4).matches("10=[0-9]+;1;-1"));
    }
}
