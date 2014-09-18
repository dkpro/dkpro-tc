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

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        if (multiLabel) {
            throw new TextClassificationException(
                    "Multi-label requested, but CRFSuite only supports single label setups.");
        }

        String executablePath = getExecutablePath();
        String modelLocation = trainModel(aContext, executablePath);
        String rawTextOutput = testModel(aContext, executablePath, modelLocation);

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

        printConfusionmatrix(lines, aContext);

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

    private void printConfusionmatrix(String[] aLines, TaskContext aContext)
        throws IOException
    {
        Set<String> predictedLables = new HashSet<String>();
        HashMap<String, HashMap<String, Integer>> map = new HashMap<String, HashMap<String, Integer>>();
        for (String line : aLines) {
            String[] split = line.split("\t");

            if (split.length < 2) {
                continue;
            }

            String actual = getActual(split[0]);
            String prediciton = split[1];

            predictedLables.add(prediciton);

            HashMap<String, Integer> wc = map.get(prediciton);
            if (wc == null) {
                wc = new HashMap<String, Integer>();
            }
            Integer integer = wc.get(prediciton);
            if (integer == null) {
                integer = new Integer(0);
            }
            map.put(prediciton, wc);
            integer++;
            wc.put(actual, integer);
        }

        List<String> uniqeLabelList = new ArrayList<String>();
        for (String l : predictedLables) {
            uniqeLabelList.add(l);
        }

        String data[][] = new String[predictedLables.size() + 1][predictedLables.size() + 1];
        data[0][0] = "";
        for (int i = 0; i < uniqeLabelList.size(); i++) {
            String label = uniqeLabelList.get(i);
            HashMap<String, Integer> perLabel = map.get(label);

            data[i + 1][0] = label;
            data[0][i + 1] = label;

            int totalSumEntries = 0;

            for (String key : perLabel.keySet()) {
                totalSumEntries += perLabel.get(key);
            }

            int j = 1;
            for (String prediction : uniqeLabelList) {
                Integer count = perLabel.get(prediction);
                data[j++][i + 1] = ""
                        + ((count == null) ? "0.0" : "" + (double) count / totalSumEntries);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < predictedLables.size() + 1; i++) {
            for (int j = 0; j < predictedLables.size() + 1; j++) {
                String format = "";
                if (j == 0 || i == 0) {
                    format = String.format("%6s", data[j][i]);
                }
                else {
                    Double doubleVal = new Double(data[j][i]);
                    if (doubleVal == 0) {
                        format = String.format("%6s", "");
                    }
                    else {
                        format = String.format("%6.2f", doubleVal.doubleValue());
                        format = format.replaceFirst("^0+(?!$)", "");
                    }
                }
                sb.append(format);
            }
            sb.append("\n");
        }
        StorageService store = aContext.getStorageService();
        File confMatrix = store.getStorageFolder(aContext.getId(), TEST_TASK_OUTPUT_KEY);
        confMatrix.getParentFile().mkdirs();
        BufferedWriter bf = new BufferedWriter(new FileWriter(new File(confMatrix.getAbsolutePath()
                + "/confusionMatrix.txt")));
        bf.write(sb.toString());
        bf.close();
        log("\n" + sb.toString());
    }

    private String getActual(String aString)
    {
        String actual = (aString.equals("(null)")) ? "XYZ" : aString;
        actual = actual.trim().equals("") ? "XYZ" : actual;

        return actual;
    }

    private String testModel(TaskContext aContext, String aExecutablePath, String aModelLocation)
        throws Exception
    {

        List<String> testModelCommand = buildTestCommand(aContext, aExecutablePath, aModelLocation);
        log("Testing model");
        String output = runTest(testModelCommand);
        log("Testing model finished");

        return output;
    }

    private String runTest(List<String> aTestModelCommand)
        throws Exception
    {
        Process process = new ProcessBuilder().command(aTestModelCommand).start();
        InputStream src = process.getInputStream();
        Scanner sc = new Scanner(src);
        StringBuilder dest = new StringBuilder();
        while (sc.hasNextLine()) {
            String l = sc.nextLine();
            dest.append(l + "\n");
        }
        sc.close();

        return dest.toString();

    }

    private List<String> buildTestCommand(TaskContext aContext, String aExecutablePath,
            String aModelLocation)
        throws Exception
    {
        File tmpTest = new File(aContext.getStorageLocation(TEST_TASK_INPUT_KEY_TEST_DATA,
                AccessMode.READONLY).getPath()
                + "/"
                + CRFSuiteAdapter.getInstance().getFrameworkFilename(
                        AdapterNameEntries.trainingFile));
        File test = ResourceUtils.getUrlAsFile(tmpTest.toURI().toURL(), true);

        // Evaluate model against test data
        List<String> commandTestModel = new ArrayList<String>();
        commandTestModel.add(aExecutablePath);
        commandTestModel.add("tag");
        commandTestModel.add("-r");
        commandTestModel.add("-m");
        commandTestModel.add(aModelLocation);
        commandTestModel.add(test.getAbsolutePath());
        return commandTestModel;
    }

    private String trainModel(TaskContext aContext, String aExecutablePath)
        throws Exception
    {

        String tmpModelLocation = System.getProperty("java.io.tmpdir") + MODELNAME;
        List<String> modelTrainCommand = buildTrainCommand(aContext, aExecutablePath,
                tmpModelLocation);
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

    private List<String> buildTrainCommand(TaskContext aContext, String aExecutablePath,
            String aTmpModelLocation)
        throws Exception
    {
        File tmpTrain = new File(aContext.getStorageLocation(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY).getPath()
                + "/"
                + CRFSuiteAdapter.getInstance().getFrameworkFilename(
                        AdapterNameEntries.trainingFile));

        File train = ResourceUtils.getUrlAsFile(tmpTrain.toURI().toURL(), true);

        List<String> commandTrainModel = new ArrayList<String>();
        commandTrainModel.add(aExecutablePath);
        commandTrainModel.add("learn");
        commandTrainModel.add("-m");
        commandTrainModel.add(aTmpModelLocation);
        commandTrainModel.add(train.getAbsolutePath());
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
