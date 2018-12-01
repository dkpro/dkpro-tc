/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.ml.crfsuite.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.dkpro.tc.ml.crfsuite.CrfSuiteTestTask;

import de.tudarmstadt.ukp.dkpro.core.api.resources.PlatformDetector;
import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;

public class CrfSuite
{
    RuntimeProvider runtimeProvider = null;
    final static PlatformDetector detector = new PlatformDetector();

    private static final String classpath = "classpath:/org/dkpro/tc/ml/crfsuite/";

    public CrfSuite()
    {
        //
    }

    public static List<String> assembleCrfCommand(File crfBinary, String... parameters)
    {

        List<String> command = new ArrayList<>();

        command.add(crfBinary.getAbsolutePath());
        for (String p : parameters) {
            command.add(p);
        }

        return command;
    }

    public File getExecutable() throws Exception
    {

        if (runtimeProvider == null) {
            String platform = detector.getPlatformId();
            LogFactory.getLog(CrfSuiteTestTask.class.getName())
                    .info("Load binary for platform: [" + platform + "]");

            runtimeProvider = new RuntimeProvider(classpath);
        }

        return runtimeProvider.getFile("crfsuite");
    }

    public static PlatformDetector getPlatformDetector()
    {
        return detector;
    }

    /**
     * Converts the provided name to a {@link CrfSuiteAlgo} object, which ensures that the provided
     * algorithm name is valid
     * 
     * @param algoName
     *            The name of the algorithm
     * @return a {@link CrfSuiteAlgo} object if the algorithm is found
     * @throws IllegalArgumentException
     *             If provided name is unknown
     */

    public static CrfSuiteAlgo getAlgorithm(String algoName) throws IllegalArgumentException
    {
        CrfSuiteAlgo[] values = CrfSuiteAlgo.values();
        for (CrfSuiteAlgo a : values) {
            if (a.toString().equals(algoName)) {
                return a;
            }
        }

        throw new IllegalArgumentException("The algorithm [" + algoName + "] is unknown");
    }

}
