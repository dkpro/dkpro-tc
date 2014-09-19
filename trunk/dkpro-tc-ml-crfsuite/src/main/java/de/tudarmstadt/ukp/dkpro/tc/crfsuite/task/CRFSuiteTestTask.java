/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.crfsuite.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteAdapter;
import de.tudarmstadt.ukp.dkpro.tc.ml.TCMachineLearningAdapter.AdapterNameEntries;

public class CRFSuiteTestTask
    extends ExecutableTaskBase
    implements Constants
{
    @Discriminator
    private List<String> classificationArguments;
    @Discriminator
    private String featureMode;
    @Discriminator
    private String learningMode;
    @Discriminator
    String threshold;

    private static final String MODELNAME = "model.crfsuite";
    Log logger = null;

    private String executablePath = null;
    private String modelLocation = null;
    private File trainFile = null;
    private File testFile = null;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        if (multiLabel) {
            throw new TextClassificationException(
                    "Multi-label requested, but CRFSuite only supports single label setups.");
        }

        executablePath = getExecutablePath();
        modelLocation = trainModel(aContext);
        String rawTextOutput = testModel(aContext);

        // FIXME that is supposed to be in the evaluation modul
        evaluate(aContext, rawTextOutput);

    }

    private String getExecutablePath()
        throws Exception
    {
        return new RuntimeProvider("classpath:/de/tudarmstadt/ukp/dkpro/tc/crfsuite/").getFile(
                "crfsuite").getAbsolutePath();
    }

    private void evaluate(TaskContext aContext, String aRawTextOutput)
        throws Exception
    {
        String[] lines = aRawTextOutput.split("\n");

        int correct = 0;
        int incorrect = 0;

        List<String> predictionValues = new ArrayList<String>();
        for (String line : lines) {
            String[] split = line.split("\t");
            if (split.length < 2) {
                continue;
            }
            String actual = split[0];
            String prediction = split[1];
            predictionValues.add(prediction);
            if (actual.equals(prediction)) {
                correct++;
            }
            else {
                incorrect++;
            }
        }

        // call crfsuite again to obtain p/r/f1 per class as calculated by crfsuite
        String precRecF1perClass = getPrecisionRecallF1PerClass();
        log(precRecF1perClass);
        File precRecF1File = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
                AccessMode.READWRITE), "precisionRecallF1PerWordClass.txt");
        FileUtils.write(precRecF1File, "\n" + precRecF1perClass);

        double denominator = correct + incorrect;
        double numerator = correct;
        double accuracy = 0;
        if (denominator > 0) {
            accuracy = numerator / denominator;
        }
        log("Accuracy: " + accuracy * 100 + " (" + correct + " correct, " + incorrect
                + " incorrect)");

        File predictionsFile = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
                AccessMode.READWRITE), CRFSuiteAdapter.getInstance().getFrameworkFilename(
                AdapterNameEntries.predictionsFile));

        StringBuilder sb = new StringBuilder();
        for (String p : predictionValues) {
            sb.append(p + "\n");
        }
        FileUtils.writeStringToFile(predictionsFile, sb.toString());

        // evaluate and write results

        // file to hold prediction results
        File evalFile = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
                AccessMode.READWRITE), CRFSuiteAdapter.getInstance().getFrameworkFilename(
                AdapterNameEntries.evaluationFile));
        sb = new StringBuilder();
        sb.append(ReportConstants.CORRECT + "=" + correct + "\n");
        sb.append(ReportConstants.INCORRECT + "=" + incorrect + "\n");
        sb.append(ReportConstants.PCT_CORRECT + "=" + accuracy + "\n");
        FileUtils.writeStringToFile(evalFile, sb.toString());
    }

    private String getPrecisionRecallF1PerClass()
        throws Exception
    {
        String executablePath = getExecutablePath();
        List<String> evalCommand = new ArrayList<String>();
        evalCommand.add(executablePath);
        evalCommand.add("tag");
        evalCommand.add("-qt");
        evalCommand.add("-m");
        evalCommand.add(modelLocation);
        evalCommand.add(testFile.getAbsolutePath());

        Process process = new ProcessBuilder().command(evalCommand).start();
        String output = captureProcessOutput(process);

        return output;
    }

    private String testModel(TaskContext aContext)
        throws Exception
    {

        List<String> testModelCommand = buildTestCommand(aContext);
        log("Testing model");
        String output = runTest(testModelCommand);
        log("Testing model finished");

        return output;
    }

    private String runTest(List<String> aTestModelCommand)
        throws Exception
    {
        Process process = new ProcessBuilder().command(aTestModelCommand).start();

        String output = captureProcessOutput(process);

        return output;

    }

    private String captureProcessOutput(Process aProcess)
    {
        InputStream src = aProcess.getInputStream();
        Scanner sc = new Scanner(src);
        StringBuilder dest = new StringBuilder();
        while (sc.hasNextLine()) {
            String l = sc.nextLine();
            dest.append(l + "\n");
        }
        sc.close();
        return dest.toString();
    }

    private List<String> buildTestCommand(TaskContext aContext)
        throws Exception
    {
        File tmpTest = new File(aContext.getStorageLocation(TEST_TASK_INPUT_KEY_TEST_DATA,
                AccessMode.READONLY).getPath()
                + "/"
                + CRFSuiteAdapter.getInstance().getFrameworkFilename(
                        AdapterNameEntries.trainingFile));
        testFile = ResourceUtils.getUrlAsFile(tmpTest.toURI().toURL(), true);

        // Evaluate model against test data
        List<String> commandTestModel = new ArrayList<String>();
        commandTestModel.add(executablePath);
        commandTestModel.add("tag");
        commandTestModel.add("-r");
        commandTestModel.add("-m");
        commandTestModel.add(modelLocation);
        commandTestModel.add(testFile.getAbsolutePath());
        return commandTestModel;
    }

    private String trainModel(TaskContext aContext)
        throws Exception
    {
        String tmpModelLocation = System.getProperty("java.io.tmpdir") + MODELNAME;
        List<String> modelTrainCommand = buildTrainCommand(aContext, tmpModelLocation);

        log("Start training model");
        long time = System.currentTimeMillis();
        runTrain(modelTrainCommand);
        long completedIn = System.currentTimeMillis() - time;
        String formattedDuration = DurationFormatUtils.formatDuration(completedIn, "HH:mm:ss:SS");
        log("Training finished after " + formattedDuration);

        return writeModel(aContext, tmpModelLocation);
    }

    private void runTrain(List<String> aModelTrainCommand)
        throws Exception
    {
        Process process = new ProcessBuilder().inheritIO().command(aModelTrainCommand).start();
        process.waitFor();
    }

    private String writeModel(TaskContext aContext, String aTmpModelLocation)
        throws Exception
    {
        aContext.storeBinary(MODELNAME, new FileInputStream(new File(aTmpModelLocation)));

        File modelLocation = aContext.getStorageLocation(MODELNAME, AccessMode.READONLY);

        return modelLocation.getAbsolutePath();
    }

    private List<String> buildTrainCommand(TaskContext aContext, String aTmpModelLocation)
        throws Exception
    {
        File tmpTrain = new File(aContext.getStorageLocation(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY).getPath()
                + "/"
                + CRFSuiteAdapter.getInstance().getFrameworkFilename(
                        AdapterNameEntries.trainingFile));

        trainFile = ResourceUtils.getUrlAsFile(tmpTrain.toURI().toURL(), true);

        List<String> commandTrainModel = new ArrayList<String>();
        commandTrainModel.add(executablePath);
        commandTrainModel.add("learn");
        commandTrainModel.add("-m");
        commandTrainModel.add(aTmpModelLocation);
        commandTrainModel.add(trainFile.getAbsolutePath());
        return commandTrainModel;
    }

    private void log(String text)
    {
        if (logger == null) {
            logger = LogFactory.getLog(getClass());
        }
        logger.info(text);
    }
}
