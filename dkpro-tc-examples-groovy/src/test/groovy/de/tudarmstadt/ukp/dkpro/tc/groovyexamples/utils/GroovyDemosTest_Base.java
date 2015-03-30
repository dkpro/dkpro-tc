package de.tudarmstadt.ukp.dkpro.tc.groovyexamples.utils;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.tc.groovyexamples.single.document.TwentyNewsgroupsDemoExtended;

public class GroovyDemosTest_Base
{
    @Before
    public void setup()
        throws Exception
    {
        String path = "target/repository/" + getClass().getSimpleName() + "/"
                + name.getMethodName();
        System.setProperty("DKPRO_HOME", new File(path).getAbsolutePath());

    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }

}
