package de.tudarmstadt.ukp.dkpro.tc.weka.util;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

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


        Instance i2 = new Instance();
        i2.addFeature(new Feature("feature1", 1));
        i2.addFeature(new Feature("feature2", 1));
        i2.addFeature(new Feature("feature3_{{", "b"));

        Instance i3 = new Instance();
        i3.addFeature(new Feature("feature1", 1));
        i3.addFeature(new Feature("feature2", 1));
        i3.addFeature(new Feature("feature3_{{", "b"));
               
        InstanceList iList = new InstanceList();
        iList.addInstance(i1, "1");
        iList.addInstance(i2, "2");
        iList.addInstance(i3, "2");

        
        File outfile = new File("target/test/out.txt");
        outfile.mkdirs();
        outfile.createNewFile();
        outfile.deleteOnExit();
        
        WekaUtils.instanceListToArffFile(outfile, iList);
        
        System.out.println(FileUtils.readFileToString(outfile));
    }
}
