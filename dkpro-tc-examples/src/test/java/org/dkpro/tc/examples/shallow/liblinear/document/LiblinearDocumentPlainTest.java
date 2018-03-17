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
package org.dkpro.tc.examples.shallow.liblinear.document;

import static org.junit.Assert.assertEquals;

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
 * This test just ensures that the experiment runs without throwing any exception.
 */
public class LiblinearDocumentPlainTest
    extends TestCaseSuperClass
{
    LiblinearDocumentPlain javaExperiment;
    ParameterSpace pSpace;

    @Before
    public void setup() throws Exception
    {
        super.setup();

        javaExperiment = new LiblinearDocumentPlain();
        pSpace = LiblinearDocumentPlain.getParameterSpace();
    }

    @Test
    public void testJavaTrainTest() throws Exception
    {
        javaExperiment.runTrainTest(pSpace);
        assertEquals(0.75, getAccuracy(ContextMemoryReport.id2outcomeFiles, "Liblinear"), 0.01);

        assertEquals(1, ContextMemoryReport.id2outcomeFiles.size());

        List<String> lines = FileUtils.readLines(ContextMemoryReport.id2outcomeFiles.get(0),
                "utf-8");
        assertEquals(11, lines.size());

        // line-wise compare
        assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
        assertEquals("#labels 0=alt.atheism 1=comp.graphics", lines.get(1));
        // line 2 is a time-stamp
        assertEquals("alt.atheism/53068.txt=1;0;-1", lines.get(3));
        assertEquals("alt.atheism/53257.txt=0;0;-1", lines.get(4));
        assertEquals("alt.atheism/53260.txt=0;0;-1", lines.get(5));
        assertEquals("alt.atheism/53261.txt=0;0;-1", lines.get(6));
        assertEquals("comp.graphics/38758.txt=0;1;-1", lines.get(7));
        assertEquals("comp.graphics/38761.txt=1;1;-1", lines.get(8));
        assertEquals("comp.graphics/38762.txt=1;1;-1", lines.get(9));
        assertEquals("comp.graphics/38763.txt=1;1;-1", lines.get(10));

    }

    private double getAccuracy(List<File> id2outcomeFiles, String simpleName) throws Exception
    {

        for (File f : id2outcomeFiles) {
            if (f.getAbsolutePath().toLowerCase().contains(simpleName.toLowerCase())) {

                EvaluationData<String> data = Tc2LtlabEvalConverter
                        .convertSingleLabelModeId2Outcome(f);
                Accuracy<String> acc = new Accuracy<>(data);
                return acc.getResult();
            }
        }

        return -1;
    }

}
