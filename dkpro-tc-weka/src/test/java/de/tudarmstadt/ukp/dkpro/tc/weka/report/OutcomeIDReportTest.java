/**
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import weka.core.Instances;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;

/**
 * Tests the correct generation of the OutcomeIdReport for various setups.
 * 
 * @author daxenberger
 * 
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

        singleLabelData = TaskUtils.getInstances(singleLabelFile, false);
        multiLabelData = TaskUtils.getInstances(multiLabelFile, true);
        regressionData = TaskUtils.getInstances(regressionFile, false);
    }

    @Test
    public void testGenerateOutcomeIdPropertiesSingleLabel()
    {
        Properties props = WekaOutcomeIDReport.generateProperties(singleLabelData, false);

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
        Properties props = WekaOutcomeIDReport.generateProperties(multiLabelData, true);

        assertEquals(6, props.size());
        assertEquals(";__grain", props.getProperty("14828.txt"));
        assertEquals("__wheat,__sorghum,__grain,__corn,__acq;", props.getProperty("14829.txt"));
        assertEquals("__acq;__grain,__corn", props.getProperty("14832.txt"));
    }

    @Test
    public void testGenerateOutcomeIdPropertiesRegression()
    {
        Properties props = WekaOutcomeIDReport.generateProperties(regressionData, false);

        assertEquals(375, props.size());
        assertEquals("3.44168;3.75", props.getProperty("STS.input.MSRpar.txt-1"));
        assertEquals("2.640227;1.75", props.getProperty("STS.input.MSRpar.txt-100"));
        assertEquals("4.41385;5.0", props.getProperty("STS.input.MSRpar.txt-133"));
    }
}
