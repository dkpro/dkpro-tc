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
package org.dkpro.tc.ml.vowpalwabbit.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.dkpro.tc.ml.base.TcPredictor;

public class VowpalWabbitPredictor
    extends VowpalWabbit implements TcPredictor
{

    public VowpalWabbitPredictor()
    {
        
    }

    /**
     * Executes the binary with the provided model and data file. The data is read from file.
     * 
     * @param data
     *            The feature file
     * @param model
     *            The model to be used
     * @return The predictions as string
     * @throws Exception
     *             In case of errors
     */
    @Override
    public List<String> predict(File data, File model) throws Exception
    {
    	File tempFile = Files.createTempFile("vowpalWabbitPrediction" + System.currentTimeMillis(), ".txt").toFile();
    	tempFile.deleteOnExit();
        List<String> testingCommand = getTestCommand(data, model, tempFile);
        
        executePrediction(testingCommand);
        
        List<String> readLines = FileUtils.readLines(tempFile, "utf-8");
        return readLines;
    }

    /**
     * Executes the binary with the provided model, the feature information are provided loaded into
     * a string which is provided via stdin
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
     * Builds a command that can be executed with a {@link ProcessBuilder}, which calls the binary
     * with the provided model
     * 
     * @param aModel
     *            The model to be used
     * @return The command as list
     * @throws Exception
     *             In case of errors
     */
    public static List<String> getTestCommandForPredictionFromStdin(File aModel) throws Exception
    {

        List<String> command = new ArrayList<String>();
        command.add("-testonly");
        command.add("-initial_regressor");
        command.add(aModel.getAbsolutePath());
        command.add("--predictions");
        command.add("/dev/stdout");

        return assembleCommand(getExecutable(), command.toArray(new String[0]));
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

        return readProcessOutput(process);
    }

    /**
     * @param aTestFile
     *          A file with the feature information for prediction
     * @param aModel
     *          The model to be used
     * @param tempFile 
     * @return
     *      The assembled test command
     * @throws Exception 
     *          In case of an exception
     */
    public static List<String> getTestCommand(File aTestFile, File aModel, File anOutputTargetFile) throws Exception
    {
        List<String> parameters = new ArrayList<String>();
        parameters.add("--testonly");
        parameters.add("--initial_regressor");
        parameters.add(aModel.getAbsolutePath());
        parameters.add("--data");
        parameters.add(aTestFile.getAbsolutePath());
        parameters.add("--predictions");
        parameters.add(anOutputTargetFile.getAbsolutePath());

        return assembleCommand(getExecutable(), parameters.toArray(new String[0]));
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
    public static void executePrediction(List<String> command) throws Exception
    {
        Process process = new ProcessBuilder().command(command).start();
        readProcessOutput(process);
    }

    public static String readProcessOutput(Process aProcess)
    {
        InputStream src = aProcess.getInputStream();
        Scanner sc = new Scanner(src, "utf-8");
        StringBuilder dest = new StringBuilder(1024);
        while (sc.hasNextLine()) {
            String l = sc.nextLine();
            System.err.println(l);
        }
        sc.close();
        return dest.toString();
    }

}
