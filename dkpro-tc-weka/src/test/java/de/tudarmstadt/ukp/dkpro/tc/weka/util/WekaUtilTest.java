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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import de.tudarmstadt.ukp.dkpro.tc.core.feature.MissingValue;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.MissingValue.MissingValueNonNominalType;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeatureStore;

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
        i1.addFeature(new Feature("feature4", Values.VALUE_1));
        i1.setOutcomes("1");

        Instance i2 = new Instance();
        i2.addFeature(new Feature("feature1", 1));
        i2.addFeature(new Feature("feature2", 1));
        i2.addFeature(new Feature("feature3_{{", "b"));
        i2.addFeature(new Feature("feature4", Values.VALUE_2));
        i2.setOutcomes("2");

        Instance i3 = new Instance();
        i3.addFeature(new Feature("feature1", 1));
        i3.addFeature(new Feature("feature2", 1));
        i3.addFeature(new Feature("feature3_{{", "b"));
        i3.addFeature(new Feature("feature4", Values.VALUE_3));
        i3.setOutcomes("2");

        // test missing values
        Instance i4 = new Instance();
        i4.addFeature(new Feature("feature1", new MissingValue(MissingValueNonNominalType.BOOLEAN)));
        i4.addFeature(new Feature("feature2", new MissingValue(MissingValueNonNominalType.NUMERIC)));
        i4.addFeature(new Feature("feature3_{{",
                new MissingValue(MissingValueNonNominalType.STRING)));
        i4.addFeature(new Feature("feature4", new MissingValue(Values.class)));
        i4.setOutcomes("2");

        SimpleFeatureStore iList = new SimpleFeatureStore();
        iList.addInstance(i1);
        iList.addInstance(i2);
        iList.addInstance(i3);
        iList.addInstance(i4);

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
        i1.addFeature(new Feature("feature4", Values.VALUE_1));
        i1.setOutcomes("1", "2");

        Instance i2 = new Instance();
        i2.addFeature(new Feature("feature1", 1));
        i2.addFeature(new Feature("feature2", 1));
        i2.addFeature(new Feature("feature3_{{", "b"));
        i2.addFeature(new Feature("feature4", Values.VALUE_2));
        i2.setOutcomes("2", "3");

        Instance i3 = new Instance();
        i3.addFeature(new Feature("feature1", 1));
        i3.addFeature(new Feature("feature2", 1));
        i3.addFeature(new Feature("feature3_{{", "b"));
        i3.addFeature(new Feature("feature4", Values.VALUE_3));
        i3.setOutcomes("2");

        // test missing values
        Instance i4 = new Instance();
        i4.addFeature(new Feature("feature1", new MissingValue(MissingValueNonNominalType.BOOLEAN)));
        i4.addFeature(new Feature("feature2", new MissingValue(MissingValueNonNominalType.NUMERIC)));
        i4.addFeature(new Feature("feature3_{{",
                new MissingValue(MissingValueNonNominalType.STRING)));
        i4.addFeature(new Feature("feature4", new MissingValue(Values.class)));
        i4.setOutcomes("1", "3");

        SimpleFeatureStore iList = new SimpleFeatureStore();
        iList.addInstance(i1);
        iList.addInstance(i2);
        iList.addInstance(i3);
        iList.addInstance(i4);

        File outfile = new File("target/test/out.txt");
        outfile.mkdirs();
        outfile.createNewFile();
        outfile.deleteOnExit();

        WekaUtils.instanceListToArffFileMultiLabel(outfile, iList, false);

        System.out.println(FileUtils.readFileToString(outfile));
    }

    @Test
    public void tcInstanceToWekaInstanceRegressionTest()
        throws Exception
    {

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
        attributes.add(new Attribute("outcome"));

        weka.core.Instance wekaInstance1 = WekaUtils.tcInstanceToWekaInstance(i1, attributes,
                null, true);
        weka.core.Instance wekaInstance2 = WekaUtils.tcInstanceToWekaInstance(i2, attributes,
                null, true);

        assertEquals(true, wekaInstance1.equalHeaders(wekaInstance2));
        assertEquals(5, wekaInstance1.numAttributes());

        wekaInstance1.dataset().add(wekaInstance1);
        wekaInstance2.dataset().add(wekaInstance2);
        System.out.println(wekaInstance1.dataset() + "\n");
        System.out.println(wekaInstance2.dataset() + "\n");
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

        // test missing values
        Instance i3 = new Instance();
        i3.addFeature(new Feature("feature1", new MissingValue(MissingValueNonNominalType.BOOLEAN)));
        i3.addFeature(new Feature("feature2", new MissingValue(MissingValueNonNominalType.NUMERIC)));
        i3.addFeature(new Feature("feature3_{{",
                new MissingValue(MissingValueNonNominalType.STRING)));

        weka.core.Instance wekaInstance1 = WekaUtils.tcInstanceToWekaInstance(i1, attributes,
                outcomeValues, false);
        weka.core.Instance wekaInstance2 = WekaUtils.tcInstanceToWekaInstance(i2, attributes,
                outcomeValues, false);
        weka.core.Instance wekaInstance3 = WekaUtils.tcInstanceToWekaInstance(i3, attributes,
                outcomeValues, false);

        assertEquals(true, wekaInstance1.equalHeaders(wekaInstance2));
        assertEquals(5, wekaInstance1.numAttributes());

        wekaInstance1.dataset().add(wekaInstance1);
        wekaInstance2.dataset().add(wekaInstance2);
        wekaInstance3.dataset().add(wekaInstance3);
        System.out.println(wekaInstance1.dataset() + "\n");
        System.out.println(wekaInstance2.dataset() + "\n");
        System.out.println(wekaInstance3.dataset() + "\n");
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

    private enum Values
    {
        VALUE_1, VALUE_2, VALUE_3
    }
}
