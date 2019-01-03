/*******************************************************************************
 * Copyright 2019
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.core;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.IOException;
import java.io.StringWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.commons.io.IOUtils;

/**
 * Java wrapper class for the execution of Groovy scripts. It is needed to avoid the pre-compilation
 * of Groovy scripts.
 */
public class ExperimentStarter 
{
    /**
     * Method which executes Groovy script provided in the pathToScript.
     * 
     * @param pathToScript
     *            path to Groovy script.
     * 
     * @throws InstantiationException
     *             if class cannot be instantiated
     * 
     * @throws IllegalAccessException
     *             if an illegal access occurred
     * 
     * @throws IOException
     *             general IO Exceptions
     */
    public static void start(String pathToScript)
        throws InstantiationException, IllegalAccessException, IOException
    {
        ClassLoader parent = ExperimentStarter.class.getClassLoader();
        GroovyClassLoader loader = AccessController
                .doPrivileged(new PrivilegedAction<GroovyClassLoader>()
                {
                    public GroovyClassLoader run()
                    {
                        return new GroovyClassLoader(parent);
                    }
                });

        StringWriter writer = new StringWriter();
        IOUtils.copy(parent.getResourceAsStream(pathToScript), writer, "UTF-8");
        Class<?> groovyClass = loader.parseClass(writer.toString());
        GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
        Object[] a = {};
        groovyObject.invokeMethod("run", a);
        loader.close();
    }
}
