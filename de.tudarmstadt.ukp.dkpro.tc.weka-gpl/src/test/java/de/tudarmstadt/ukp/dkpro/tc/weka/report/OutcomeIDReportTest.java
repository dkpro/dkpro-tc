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
        Properties props = OutcomeIDReport.generateProperties(singleLabelData, false);

        assertEquals(16, props.size());
        assertEquals("comp.graphics (is alt.atheism)", props.getProperty("alt.atheism/53261.txt"));
        assertEquals("comp.graphics (is comp.sys.ibm.pc.hardware)",
                props.getProperty("comp.sys.ibm.pc.hardware/60738.txt"));
        assertEquals("comp.os.ms-windows.misc (is comp.os.ms-windows.misc)",
                props.getProperty("comp.os.ms-windows.misc/10006.txt"));
    }

    @Test
    public void testGenerateOutcomeIdPropertiesMultiLabel()
    {
        Properties props = OutcomeIDReport.generateProperties(multiLabelData, true);

        assertEquals(6, props.size());
        assertEquals(" (is __grain)", props.getProperty("14828.txt"));
        assertEquals("__wheat,__sorghum,__grain,__corn,__acq (is )", props.getProperty("14829.txt"));
        assertEquals("__acq (is __grain,__corn)", props.getProperty("14832.txt"));
    }

    @Test
    public void testGenerateOutcomeIdPropertiesRegression()
    {
        Properties props = OutcomeIDReport.generateProperties(regressionData, false);

        assertEquals(375, props.size());
        assertEquals("3.44168 (is 3.75)", props.getProperty("STS.input.MSRpar.txt-1"));
        assertEquals("2.640227 (is 1.75)", props.getProperty("STS.input.MSRpar.txt-100"));
        assertEquals("4.41385 (is 5.0)", props.getProperty("STS.input.MSRpar.txt-133"));
    }
}
