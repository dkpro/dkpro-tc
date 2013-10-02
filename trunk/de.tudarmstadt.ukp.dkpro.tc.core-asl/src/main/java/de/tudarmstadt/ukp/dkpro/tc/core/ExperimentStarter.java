package de.tudarmstadt.ukp.dkpro.tc.core;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Java wrapper class for the execution of Groovy scripts. It is needed to avoid the pre-compilation
 * of Groovy scripts.
 * 
 * @author Artem Vovk
 * 
 */
public class ExperimentStarter
{
    /**
     * Method which executes Groovy script provided in the pathToScript.
     * 
     * @param pathToScript
     *            path to Groovy script.
     * 
     */
    public static void start(String pathToScript)
        throws InstantiationException, IllegalAccessException, IOException
    {
        try {
            ClassLoader parent = ExperimentStarter.class.getClassLoader();
            GroovyClassLoader loader = new GroovyClassLoader(parent);
            Class<?> groovyClass = loader.parseClass(new File(parent.getResource(pathToScript)
                    .toURI()));
            GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
            Object[] a = {};
            groovyObject.invokeMethod("run", a);
        }
        catch (URISyntaxException e) {
            throw new IOException("Wrong path to the script file", e);
        }
    }
}
