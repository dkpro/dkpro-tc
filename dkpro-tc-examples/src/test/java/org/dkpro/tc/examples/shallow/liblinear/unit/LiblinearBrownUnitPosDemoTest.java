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
package org.dkpro.tc.examples.shallow.liblinear.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
public class LiblinearBrownUnitPosDemoTest
    extends TestCaseSuperClass
{
    LiblinearUnitDemo javaExperiment;
    ParameterSpace pSpace;

    @Before
    public void setup() throws Exception
    {
        super.setup();

        javaExperiment = new LiblinearUnitDemo();
        pSpace = LiblinearUnitDemo.getParameterSpace();
    }

    @Test
    public void testTrainTest() throws Exception
    {
        javaExperiment.runTrainTest(pSpace);

        EvaluationData<String> data = Tc2LtlabEvalConverter
                .convertSingleLabelModeId2Outcome(ContextMemoryReport.id2outcomeFiles.get(0));
        Accuracy<String> acc = new Accuracy<String>(data);

        assertEquals(0.76, acc.getResult(), 0.01);

        assertEquals(1, ContextMemoryReport.id2outcomeFiles.size());
        List<String> lines = FileUtils.readLines(ContextMemoryReport.id2outcomeFiles.get(0),
                "utf-8");
        assertEquals(197, lines.size());
        // line-wise compare
        assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
        assertEquals(
                "#labels 0=ABX 1=AP 2=AT 3=BEDZ 4=BEN 5=BER 6=CC 7=CS 8=DOD 9=DT 10=DTS 11=HV 12=HVD 13=IN 14=JJ 15=JJT 16=MD 17=NN 18=NNS 19=NP 20=NPg 21=PPO 22=PPS 23=QL 24=RB 25=TO 26=VB 27=VBD 28=VBG 29=VBN 30=WDT 31=pct",
                lines.get(1));
        assertTrue(lines.get(3).matches("0=[0-9]+;31;-1"));
        assertTrue(lines.get(4).matches("1=[0-9]+;2;-1"));
        assertTrue(lines.get(5).matches("10=[0-9]+;17;-1"));
        assertTrue(lines.get(6).matches("100=[0-9]+;18;-1"));
        assertTrue(lines.get(7).matches("101=[0-9]+;25;-1"));
        assertTrue(lines.get(8).matches("102=[0-9]+;26;-1"));
        assertTrue(lines.get(9).matches("103=[0-9]+;14;-1"));
        assertTrue(lines.get(10).matches("104=[0-9]+;17;-1"));
    }
}
