/**
 * Copyright 2016
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
package org.dkpro.tc.weka.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import weka.core.Instances;

import org.dkpro.tc.evaluation.Id2Outcome;
import org.dkpro.tc.weka.report.WekaOutcomeIDUsingTCEvaluationReport;
import org.dkpro.tc.weka.util.WekaUtils;

/**
 * Tests the correct generation of the OutcomeIdReport for the internal DKPro TC evaluation with
 * various setups.
 * 
 * @author daxenberger
 * 
 */
public class OutcomeIDReportUsingTCEvaluationTest
{
    Instances singleLabelData;
    Instances multiLabelData;
    Instances regressionData;

    File mlResults;

    @Before
    public void initialize()
        throws IOException
    {

        File singleLabelFile;
        File multiLabelFile;
        File regressionFile;
        try {
            singleLabelFile = new File(this.getClass()
                    .getResource("/predictions/singleLabelPredictions.arff").toURI());
            multiLabelFile = new File(this.getClass()
                    .getResource("/predictions/multiLabelPredictions.arff").toURI());
            regressionFile = new File(this.getClass()
                    .getResource("/predictions/regressionPredictions.arff").toURI());
            mlResults = new File(this.getClass()
                    .getResource("/predictions/multiLabelEvaluation.bin").toURI());
        }
        catch (URISyntaxException e) {
            throw new IOException(e);
        }

        singleLabelData = WekaUtils.getInstances(singleLabelFile, false);
        multiLabelData = WekaUtils.getInstances(multiLabelFile, true);
        regressionData = WekaUtils.getInstances(regressionFile, false);
    }

    @Test
    public void testGenerateOutcomeIdPropertiesSingleLabel() throws ClassNotFoundException, IOException
    {
        List<String> labels = WekaUtils.getClassLabels(singleLabelData, false);
        Properties props = WekaOutcomeIDUsingTCEvaluationReport.generateProperties(singleLabelData, false, false, labels, null);
        String header = WekaOutcomeIDUsingTCEvaluationReport.generateHeader(labels);
        List<String> labelsFromProps = Id2Outcome.getLabels(header);

        assertTrue(header.split("\n")[1].startsWith("labels"));
        assertEquals(labels, labelsFromProps);
        assertEquals(16, props.size());

        assertEquals(0, Id2Outcome.classNamesToMapping(labelsFromProps).get("alt.atheism").intValue());
        assertEquals(3, Id2Outcome.classNamesToMapping(labelsFromProps).get("comp.sys.ibm.pc.hardware").intValue());
        assertEquals(2, Id2Outcome.classNamesToMapping(labelsFromProps).get("comp.os.ms-windows.misc").intValue());

        assertEquals(Arrays.asList(1.),
                getPrediction(props.getProperty("alt.atheism/53261.txt")));
        assertEquals(Arrays.asList(0),
                getGoldStandard(props.getProperty("alt.atheism/53261.txt")));
        assertEquals(Arrays.asList(1.),
                getPrediction(props.getProperty("comp.sys.ibm.pc.hardware/60738.txt")));
        assertEquals(Arrays.asList(3),
                getGoldStandard(props.getProperty("comp.sys.ibm.pc.hardware/60738.txt")));
        assertEquals(Arrays.asList(2.),
                getPrediction(props.getProperty("comp.os.ms-windows.misc/10006.txt")));
        assertEquals(Arrays.asList(2),
                getGoldStandard(props.getProperty("comp.os.ms-windows.misc/10006.txt")));

    }


    @Test
    public void testGenerateOutcomeIdPropertiesMultiLabel() throws ClassNotFoundException, IOException
    {
        List<String> labels = WekaUtils.getClassLabels(multiLabelData, true);
        Properties props = WekaOutcomeIDUsingTCEvaluationReport.generateProperties(multiLabelData, true, false, labels, mlResults);
        String header = WekaOutcomeIDUsingTCEvaluationReport.generateHeader(labels);
        List<String> labelsFromProps = Id2Outcome.getLabels(header);

        assertTrue(header.split("\n")[1].startsWith("labels"));
        assertEquals(labels, labelsFromProps);
        assertEquals(8, props.size());

        assertEquals(0, Id2Outcome.classNamesToMapping(labelsFromProps).get("__oat_Comp").intValue());
        assertEquals(2, Id2Outcome.classNamesToMapping(labelsFromProps).get("__crude_Comp").intValue());
        assertEquals(4, Id2Outcome.classNamesToMapping(labelsFromProps).get("__acq_Comp").intValue());

        assertEquals(1., getPrediction(props.getProperty("138.txt")).get(1), 0.1);
        assertEquals(Arrays.asList(0, 1, 0, 1, 0), getGoldStandard(props.getProperty("138.txt")));
        assertEquals(0., getPrediction(props.getProperty("151.txt")).get(0), 0.1);
        assertEquals(Arrays.asList(0, 0, 1, 0, 0), getGoldStandard(props.getProperty("151.txt")));
        assertEquals(0., getPrediction(props.getProperty("212.txt")).get(4), 0.1);
        assertEquals(Arrays.asList(0, 0, 0, 0, 1), getGoldStandard(props.getProperty("212.txt")));

    }

    @Test
    public void testGenerateOutcomeIdPropertiesRegression() throws ClassNotFoundException, IOException
    {
        Properties props = WekaOutcomeIDUsingTCEvaluationReport.generateProperties(regressionData, false, true, null, null);

        assertEquals(376, props.size());
        assertEquals(3.44168, getPrediction(props.getProperty("STS.input.MSRpar.txt-1")).get(0), 0.0001);
        assertEquals(2.640227, getPrediction(props.getProperty("STS.input.MSRpar.txt-100")).get(0), 0.0001);
        assertEquals(4.41385, getPrediction(props.getProperty("STS.input.MSRpar.txt-133")).get(0), 0.0001);
        assertEquals(0.87415, getPrediction(props.getProperty("test")).get(0), 0.0001);
    }

    private List<Double> getPrediction(String propsString)
    {
        String[] s = propsString.split(WekaOutcomeIDUsingTCEvaluationReport.SEPARATOR_CHAR)[0].split(",");
        List<Double> a = new ArrayList<Double>();
        for (String st : s) {
            a.add(Double.valueOf(st));
        }
        return a;
    }

    private List<Integer> getGoldStandard(String propsString)
    {
        String[] s = propsString.split(WekaOutcomeIDUsingTCEvaluationReport.SEPARATOR_CHAR)[1].split(",");
        List<Integer> a = new ArrayList<Integer>();
        for (String st : s) {
            a.add(Integer.valueOf(st));
        }
        return a;
    }
}
