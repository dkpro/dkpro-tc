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

import org.dkpro.tc.ml.base.TcTrainer;

public class CrfSuiteTrainer
    extends CrfSuite implements TcTrainer
{

    public CrfSuiteTrainer()
    {
        //
    }
    
    @Override
    public void train(File aData, File aModel, List<String> parameters) throws Exception
    {
        sanityCheckParameters(parameters);

        CrfSuiteAlgo algo = getAlgorithm(parameters.get(0));

        List<String> trainCommand = getTrainCommand(algo.toString(),
                parameters.subList(1, parameters.size()), getExecutable(), aData, aModel);
        executeTrainingCommand(trainCommand);
    }

    public static List<String> getTrainCommand(String algorithm, List<String> algoParameter,
            File crfBinary, File trainingData, File model)
        throws Exception
    {
        List<String> parameterList = new ArrayList<String>();
        parameterList.add("learn");
        parameterList.add("-m");
        parameterList.add(model.getAbsolutePath());

        parameterList.add("-a");
        parameterList.add(algorithm);

        for (String p : algoParameter) {

            if (p.equals("-p")) {
                // legacy support - the switch had to be provided manually since 0.9.0
                continue;
            }

            parameterList.add("-p");
            parameterList.add(p.replaceAll(" ", ""));
        }

        parameterList.add(trainingData.getAbsolutePath());
        return assembleCrfCommand(crfBinary, parameterList.toArray(new String[0]));
    }

    public static void executeTrainingCommand(List<String> aCommand) throws Exception
    {
        Process process = new ProcessBuilder().inheritIO().command(aCommand).start();
        process.waitFor();
    }

    private void sanityCheckParameters(List<String> parameters)
    {
        if (parameters == null) {
            throw new NullPointerException("The provided parameters are null");
        }

        if (parameters.size() == 0) {
            throw new IllegalArgumentException(
                    "At least the name of the Crfsuite Algorithm has to be provided");
        }
    }
}
