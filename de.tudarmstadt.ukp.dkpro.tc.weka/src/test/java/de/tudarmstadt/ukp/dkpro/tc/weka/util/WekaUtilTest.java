package de.tudarmstadt.ukp.dkpro.tc.weka.util;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.cleartk.classifier.weka.test.Feature;
import org.cleartk.classifier.weka.test.Instance;
import org.cleartk.classifier.weka.test.InstanceList;
import org.cleartk.classifier.weka.util.WekaUtils;
import org.junit.Test;

public class WekaUtilTest
{

    @Test
    public void instanceToArffTest()
            throws Exception
    {
                
        Instance i1 = new Instance();
        i1.addFeature(new Feature("feature1", 2));
        i1.addFeature(new Feature("feature2", 2));
        i1.addFeature(new Feature("feature3-{{", "a"));


        Instance i2 = new Instance();
        i2.addFeature(new Feature("feature1", 1));
        i2.addFeature(new Feature("feature2", 1));
        i2.addFeature(new Feature("feature3-{{", "b"));

        Instance i3 = new Instance();
        i3.addFeature(new Feature("feature1", 1));
        i3.addFeature(new Feature("feature2", 1));
        i3.addFeature(new Feature("feature3-{{", "b"));
               
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
