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
package org.dkpro.tc.ml.xgboost.core;

import java.io.File;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;

public abstract class Xgboost
{
    private RuntimeProvider runtimeProvider;

    public Xgboost()
    {
        //Groovy
    }

    public static String flipBackslash(String s)
    {
        // flip backslash to be a forwards-slash instead otherwise this might lead to issues on Windows OS
        return s.replaceAll("\\\\", "/");
    }
    
    public File getExecutable() throws Exception
    {

        if (runtimeProvider == null) {
            runtimeProvider = new RuntimeProvider("classpath:/org/dkpro/tc/ml/xgboost/");
        }

        return runtimeProvider.getFile("xgboost");
    }
    
    public void uninstallExecutable()
    {
        if (runtimeProvider != null) {
            runtimeProvider.uninstall();
        }
    }
    
    public void runCommand(List<String> aCommand) throws Exception
    {
        Process process = new ProcessBuilder().inheritIO().command(aCommand).start();
        process.waitFor();
    }
}
