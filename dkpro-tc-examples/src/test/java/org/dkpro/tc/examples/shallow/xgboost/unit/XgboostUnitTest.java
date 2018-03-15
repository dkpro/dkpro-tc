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
package org.dkpro.tc.examples.shallow.xgboost.unit;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.ml.report.util.Tc2LtlabEvalConverter;
import org.junit.Before;
import org.junit.Test;

import de.unidue.ltl.evaluation.measures.Accuracy;

/**
 * This test just ensures that the experiment runs without throwing
 * any exception.
 */
public class XgboostUnitTest extends TestCaseSuperClass
{
	XgboostUnit javaExperiment;
    ParameterSpace pSpace;

    @Before
    public void setup()
        throws Exception
    {
        super.setup();
        
        javaExperiment = new XgboostUnit();
        pSpace = XgboostUnit.getParameterSpace();
    }

    
    @Test
    public void testTrainTest()
        throws Exception
    {
        javaExperiment.runTrainTest(pSpace);
        
		Accuracy<String> accuracy = new Accuracy<>(
				Tc2LtlabEvalConverter.convertSingleLabelModeId2Outcome(ContextMemoryReport.id2outcomeFiles.get(0)));

        assertEquals(0.86, accuracy.getResult(), 0.01);
        
        
        List<String> lines = FileUtils.readLines(ContextMemoryReport.id2outcomeFiles.get(0), "utf-8");
        assertEquals(197, lines.size());
        
        //line-wise compare
        assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
        assertEquals("#labels 0=ABX 1=AP 2=AT 3=BEDZ 4=BEN 5=BER 6=CC 7=CS 8=DOD 9=DT 10=DTS 11=HV 12=HVD 13=IN 14=JJ 15=JJT 16=MD 17=NN 18=NNS 19=NP 20=NPg 21=PPO 22=PPS 23=QL 24=RB 25=TO 26=VB 27=VBD 28=VBG 29=VBN 30=WDT 31=pct", lines.get(1));
        //line 2 is a time-stamp
        assertEquals("0=31;31;-1", lines.get(3));
        assertEquals("1=2;2;-1", lines.get(4));
        assertEquals("10=17;17;-1", lines.get(5));
        assertEquals("100=18;18;-1", lines.get(6));
        assertEquals("101=13;25;-1", lines.get(7));
        assertEquals("102=26;26;-1", lines.get(8));
        assertEquals("103=14;14;-1", lines.get(9));

    }
}
