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

public class CrfSuiteTrain
    extends CrfSuite
{

    public CrfSuiteTrain()
    {
        //
    }

    public void train(String algoName, List<String> parameters, File trainingData,
            File modelTargetLocation)
        throws Exception
    {
        CrfSuiteAlgo algo = getAlgorithm(algoName);
        
        List<String> trainCommand = getTrainCommand(algo.toString(), parameters, getExecutable(),
                trainingData, modelTargetLocation);
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
}
