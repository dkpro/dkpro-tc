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
package org.dkpro.tc.examples.shallow.xgboost.document;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.junit.Before;
import org.junit.Test;

/**
 * This test just ensures that the experiment runs without throwing any exception.
 */
public class XgboostDocumentPlainTest
    extends TestCaseSuperClass
{
    XgboostDocumentPlain javaExperiment;

    @Before
    public void setup() throws Exception
    {
        super.setup();

        javaExperiment = new XgboostDocumentPlain();
    }

    @Test
    public void runTest() throws Exception
    {
        javaExperiment.runTrainTest(XgboostDocumentPlain.getParameterSpace());

        assertEquals(1, ContextMemoryReport.id2outcomeFiles.size());

        List<String> lines = FileUtils.readLines(ContextMemoryReport.id2outcomeFiles.get(0),
                "utf-8");
        assertEquals(11, lines.size());

        // line-wise compare
        assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
        assertEquals("#labels 0=alt.atheism 1=comp.graphics", lines.get(1));
        // line 2 is a time-stamp
        assertEquals("alt.atheism/53068.txt=0;0;-1", lines.get(3));
        assertEquals("alt.atheism/53257.txt=0;0;-1", lines.get(4));
        assertEquals("alt.atheism/53260.txt=0;0;-1", lines.get(5));
        assertEquals("alt.atheism/53261.txt=0;0;-1", lines.get(6));
        assertEquals("comp.graphics/38758.txt=0;1;-1", lines.get(7));
        assertEquals("comp.graphics/38761.txt=1;1;-1", lines.get(8));
        assertEquals("comp.graphics/38762.txt=0;1;-1", lines.get(9));
        assertEquals("comp.graphics/38763.txt=1;1;-1", lines.get(10));

    }
}
