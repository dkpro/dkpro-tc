package de.tudarmstadt.ukp.dkpro.tc.weka.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import weka.core.Attribute;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.api.features.InstanceList;

public class WekaUtilTest
{

    @Test
    public void instanceToArffTest()
        throws Exception
    {

        Instance i1 = new Instance();
        i1.addFeature(new Feature("feature1", 2));
        i1.addFeature(new Feature("feature2", 2));
        i1.addFeature(new Feature("feature3_{{", "a"));
        i1.setOutcomes("1");

        Instance i2 = new Instance();
        i2.addFeature(new Feature("feature1", 1));
        i2.addFeature(new Feature("feature2", 1));
        i2.addFeature(new Feature("feature3_{{", "b"));
        i2.setOutcomes("2");

        Instance i3 = new Instance();
        i3.addFeature(new Feature("feature1", 1));
        i3.addFeature(new Feature("feature2", 1));
        i3.addFeature(new Feature("feature3_{{", "b"));
        i3.setOutcomes("2");

        InstanceList iList = new InstanceList();
        iList.addInstance(i1);
        iList.addInstance(i2);
        iList.addInstance(i3);

        File outfile = new File("target/test/out.txt");
        outfile.mkdirs();
        outfile.createNewFile();
        outfile.deleteOnExit();

        WekaUtils.instanceListToArffFile(outfile, iList);

        System.out.println(FileUtils.readFileToString(outfile));
    }

    @Test
    public void instanceToArffTest_multiLabel()
        throws Exception
    {

        Instance i1 = new Instance();
        i1.addFeature(new Feature("feature1", 2));
        i1.addFeature(new Feature("feature2", 2));
        i1.addFeature(new Feature("feature3_{{", "a"));
        i1.setOutcomes("1", "2");

        Instance i2 = new Instance();
        i2.addFeature(new Feature("feature1", 1));
        i2.addFeature(new Feature("feature2", 1));
        i2.addFeature(new Feature("feature3_{{", "b"));
        i2.setOutcomes("2", "3");

        Instance i3 = new Instance();
        i3.addFeature(new Feature("feature1", 1));
        i3.addFeature(new Feature("feature2", 1));
        i3.addFeature(new Feature("feature3_{{", "b"));
        i3.setOutcomes("2");

        InstanceList iList = new InstanceList();
        iList.addInstance(i1);
        iList.addInstance(i2);
        iList.addInstance(i3);

        File outfile = new File("target/test/out.txt");
        outfile.mkdirs();
        outfile.createNewFile();
        outfile.deleteOnExit();

        WekaUtils.instanceListToArffFile(outfile, iList);

        System.out.println(FileUtils.readFileToString(outfile));
    }

    @Test
    public void tcInstanceToWekaInstanceTest()
        throws Exception
    {
        List<String> outcomeValues = Arrays.asList(new String[] { "outc_1", "outc_2", "outc_3" });

        Instance i1 = new Instance();
        i1.addFeature(new Feature("feature1", 2));
        i1.addFeature(new Feature("feature2", 2));
        i1.addFeature(new Feature("feature3_{{", "a"));

        Instance i2 = new Instance();
        i2.addFeature(new Feature("feature1", 1));
        i2.addFeature(new Feature("feature4", "val_1"));
        i2.addFeature(new Feature("feature3_{{", "b"));

        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("feature5"));
        attributes.add(new Attribute("feature2"));
        attributes.add(new Attribute("feature4", Arrays.asList(new String[] { "val_1", "val_2" })));
        attributes.add(new Attribute("feature1"));
        attributes.add(new Attribute("outcome", outcomeValues));

        weka.core.Instance wekaInstance1 = WekaUtils.tcInstanceToWekaInstance(i1, attributes,
                outcomeValues, false);
        weka.core.Instance wekaInstance2 = WekaUtils.tcInstanceToWekaInstance(i2, attributes,
                outcomeValues, false);

        assertEquals(true, wekaInstance1.equalHeaders(wekaInstance2));
        assertEquals(5, wekaInstance1.numAttributes());

        wekaInstance1.dataset().add(wekaInstance1);
        wekaInstance2.dataset().add(wekaInstance2);
        System.out.println(wekaInstance1.dataset() + "\n");
        System.out.println(wekaInstance2.dataset() + "\n");
    }

    @Test
    public void tcInstanceToMekaInstanceTest()
        throws Exception
    {
        List<String> outcomeValues = Arrays.asList(new String[] { "outc_1", "outc_2", "outc_3" });

        Instance i1 = new Instance();
        i1.addFeature(new Feature("feature1", 2));
        i1.addFeature(new Feature("feature2", 2));
        i1.addFeature(new Feature("feature3_{{", "a"));

        Instance i2 = new Instance();
        i2.addFeature(new Feature("feature1", 1));
        i2.addFeature(new Feature("feature4", "val_1"));
        i2.addFeature(new Feature("feature3_{{", "b"));

        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("outc_1", Arrays.asList(new String[] { "0", "1" })));
        attributes.add(new Attribute("outc_2", Arrays.asList(new String[] { "0", "1" })));
        attributes.add(new Attribute("outc_3", Arrays.asList(new String[] { "0", "1" })));
        attributes.add(new Attribute("feature5"));
        attributes.add(new Attribute("feature2"));
        attributes.add(new Attribute("feature4", Arrays.asList(new String[] { "val_1", "val_2" })));
        attributes.add(new Attribute("feature1"));

        weka.core.Instance wekaInstance1 = WekaUtils.tcInstanceToMekaInstance(i1, attributes,
                outcomeValues);
        weka.core.Instance wekaInstance2 = WekaUtils.tcInstanceToMekaInstance(i2, attributes,
                outcomeValues);

        assertEquals(true, wekaInstance1.equalHeaders(wekaInstance2));
        assertEquals(7, wekaInstance1.numAttributes());

        wekaInstance1.dataset().add(wekaInstance1);
        wekaInstance2.dataset().add(wekaInstance2);
        System.out.println(wekaInstance1.dataset() + "\n");
        System.out.println(wekaInstance2.dataset() + "\n");
    }

    @Test(expected = IllegalArgumentException.class)
    public void tcInstanceToWekaInstanceFailTest()
        throws Exception
    {
        List<String> outcomeValues = Arrays.asList(new String[] { "outc_1", "outc_2", "outc_3" });

        Instance i1 = new Instance();
        i1.addFeature(new Feature("feature1", 2));
        i1.addFeature(new Feature("feature4", "val_1"));
        i1.addFeature(new Feature("feature3_{{", "a"));

        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("feature2"));
        attributes.add(new Attribute("feature4", Arrays.asList(new String[] { "val_4", "val_2" })));
        attributes.add(new Attribute("outcome", outcomeValues));

        @SuppressWarnings("unused")
        weka.core.Instance wekaInstance1 = WekaUtils.tcInstanceToWekaInstance(i1, attributes,
                outcomeValues, false);
    }
}
