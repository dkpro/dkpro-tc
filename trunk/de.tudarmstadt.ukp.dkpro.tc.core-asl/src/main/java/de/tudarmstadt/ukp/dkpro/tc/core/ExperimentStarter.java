package de.tudarmstadt.ukp.dkpro.tc.core;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

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
     */
    public static void start(String pathToScript)
        throws InstantiationException, IllegalAccessException, IOException
    {
        ClassLoader parent = ExperimentStarter.class.getClassLoader();
        GroovyClassLoader loader = new GroovyClassLoader(parent);

        StringWriter writer = new StringWriter();
        IOUtils.copy(parent.getResourceAsStream(pathToScript), writer, "UTF-8");
        Class<?> groovyClass = loader.parseClass(writer.toString());
        GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
        Object[] a = {};
        groovyObject.invokeMethod("run", a);
        loader.close();
    }
}
