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

import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.compress.utils.IOUtils;

public class CrfSuitePredict
    extends CrfSuite
{

    public CrfSuitePredict()
    {
        //
    }

    /**
     * Executes CrfSuites with the provided model and data file
     * 
     * @param data
     *            The feature file
     * @param model
     *            The model to be used
     * @return The predictions as string
     * @throws Exception
     *             In case of errors
     */
    public String predict(File data, File model) throws Exception
    {
        List<String> testingCommand = getTestCommand(data, model);
        String predictions = executePrediction(testingCommand);
        return predictions;
    }

    /**
     * Executes CrfSuites with the provided model, the feature informaiton are provided loaded into
     * a string
     * 
     * @param dataAsString
     *            The string representation of the features
     * @param model
     *            The model to be used
     * @return The predictions as string
     * @throws Exception
     *             In case of errors
     */
    public String predict(String dataAsString, File model) throws Exception
    {
        List<String> command = getTestCommandForPredictionFromStdin(model);
        String prediction = executePredictionFromStdin(command, dataAsString);
        return prediction;
    }

    /**
     * Builds a command that can be executed with a {@link ProcessBuilder}, which calls CrfSuite
     * with the provided model
     * 
     * @param anExecutable
     *            The Crf binary
     * @param aModel
     *            The model to be used
     * @return The command as list
     * @throws Exception
     *             In case of errors
     */
    public static List<String> getTestCommandForPredictionFromStdin(File aModel) throws Exception
    {

        List<String> command = new ArrayList<String>();
        command.add("tag");
        command.add("-m");
        command.add(aModel.getAbsolutePath());
        command.add("-"); // Read from STDIN

        return assembleCrfCommand(getExecutable(), command.toArray(new String[0]));
    }

    /**
     * Performs a prediction where the feature information is provided as strng
     * 
     * @param command
     *            The command that is executed
     * @param aDataString
     *            The feature file as string
     * @return The predictions as string
     * @throws Exception
     *             In case of errors
     */
    public static String executePredictionFromStdin(List<String> command, String aDataString)
        throws Exception
    {
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectError(Redirect.INHERIT);
        pb.command(command);
        Process process = pb.start();

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "utf-8"));
            writer.write(aDataString);
        }
        finally {
            IOUtils.closeQuietly(writer);
        }

        return captureProcessOutput(process);
    }

    /**
     * @param aTestFile
     *          A file with the feature information for prediction
     * @param aModel
     *          The model to be used
     * @return
     *      The assembled test command
     * @throws Exception 
     */
    public static List<String> getTestCommand(File aTestFile, File aModel) throws Exception
    {
        List<String> parameters = new ArrayList<String>();
        parameters.add("tag");
        parameters.add("-r");
        parameters.add("-m");
        parameters.add(aModel.getAbsolutePath());
        parameters.add(aTestFile.getAbsolutePath());

        return assembleCrfCommand(getExecutable(), parameters.toArray(new String[0]));
    }

    /**
     * Takes an assembled command and executes it, the retrieved output is returned as string
     * 
     * @param command
     *            The command for execution
     * @return The output as string
     * @throws Exception
     *             In case of errors
     */
    public static String executePrediction(List<String> command) throws Exception
    {
        Process process = new ProcessBuilder().command(command).start();
        String output = captureProcessOutput(process);
        return output;
    }

    /**
     * Takes a process object and reads the process output
     * 
     * @param aProcess
     *            The process
     * @return The output as string
     */
    public static String captureProcessOutput(Process aProcess)
    {
        InputStream src = aProcess.getInputStream();
        Scanner sc = new Scanner(src, "utf-8");
        StringBuilder dest = new StringBuilder();
        while (sc.hasNextLine()) {
            String l = sc.nextLine();
            dest.append(l + "\n");
        }
        sc.close();
        return dest.toString();
    }

}
