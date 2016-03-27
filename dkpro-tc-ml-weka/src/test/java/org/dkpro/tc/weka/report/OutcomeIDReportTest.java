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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import weka.core.Instances;

import org.dkpro.tc.weka.report.WekaOutcomeIDReport;
import org.dkpro.tc.weka.util.WekaUtils;

/**
 * Tests the correct generation of the OutcomeIdReport for various setups.
 */
public class OutcomeIDReportTest
{
    Instances singleLabelData;
    Instances multiLabelData;
    Instances regressionData;

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
        }
        catch (URISyntaxException e) {
            throw new IOException(e);
        }

        singleLabelData = WekaUtils.getInstances(singleLabelFile, false);
        multiLabelData = WekaUtils.getInstances(multiLabelFile, true);
        regressionData = WekaUtils.getInstances(regressionFile, false);
    }

    @Test
    public void testGenerateOutcomeIdPropertiesSingleLabel()
    {
        Properties props = WekaOutcomeIDReport.generateProperties(singleLabelData, false, false);

        assertEquals(16, props.size());
        assertEquals("comp.graphics;alt.atheism", props.getProperty("alt.atheism/53261.txt"));
        assertEquals("comp.graphics;comp.sys.ibm.pc.hardware",
                props.getProperty("comp.sys.ibm.pc.hardware/60738.txt"));
        assertEquals("comp.os.ms-windows.misc;comp.os.ms-windows.misc",
                props.getProperty("comp.os.ms-windows.misc/10006.txt"));
    }

    @Test
    public void testGenerateOutcomeIdPropertiesMultiLabel()
    {
        Properties props = WekaOutcomeIDReport.generateProperties(multiLabelData, true, false);

        assertEquals(8, props.size());
        assertEquals("__grain_Comp,__corn_Comp;__grain_Comp,__corn_Comp", props.getProperty("138.txt"));
        assertEquals(";__crude_Comp", props.getProperty("151.txt"));
        assertEquals(";__acq_Comp", props.getProperty("212.txt"));
    }

    @Test
    public void testGenerateOutcomeIdPropertiesRegression()
    {
        Properties props = WekaOutcomeIDReport.generateProperties(regressionData, false, true);

        assertEquals(376, props.size());
        assertEquals("3.44168;3.75", props.getProperty("STS.input.MSRpar.txt-1"));
        assertEquals("2.640227;1.75", props.getProperty("STS.input.MSRpar.txt-100"));
        assertEquals("4.41385;5.0", props.getProperty("STS.input.MSRpar.txt-133"));
        assertEquals("0.87415;0.0", props.getProperty("test"));
    }
}
