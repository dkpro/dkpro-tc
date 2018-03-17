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

package org.dkpro.tc.ml.crfsuite.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.crfsuite.writer.LabelSubstitutor;

import de.tudarmstadt.ukp.dkpro.core.api.resources.PlatformDetector;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;

public class CrfSuiteTestTask
    extends ExecutableTaskBase
    implements Constants
{
    @Discriminator(name = DIM_LEARNING_MODE)
    private String learningMode;

    @Discriminator(name = DIM_CLASSIFICATION_ARGS)
    private List<Object> classificationArguments;

    private File model = null;
    private String algoName;
    private List<String> algoParameters;

    private static RuntimeProvider runtimeProvider = null;
    private static PlatformDetector detector = new PlatformDetector();

    @Override
    public void execute(TaskContext aContext) throws Exception
    {
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        if (multiLabel) {
            throw new TextClassificationException(
                    "Multi-label requested, but CRFSuite only supports single label setups.");
        }

        processParameters(classificationArguments);

        model = trainModel(aContext);
        String rawTextOutput = testModel(aContext);

        writePredictions2File(aContext, rawTextOutput);

    }

    private void writePredictions2File(TaskContext aContext, String aRawTextOutput) throws Exception
    {
        List<String> predictionValues = new ArrayList<String>(
                Arrays.asList(aRawTextOutput.split("\n")));
        writeFileWithPredictedLabels(aContext, predictionValues);
    }

    private void writeFileWithPredictedLabels(TaskContext aContext, List<String> predictionValues)
        throws Exception
    {
        File predictionsFile = aContext.getFile(Constants.FILENAME_PREDICTIONS,
                AccessMode.READWRITE);

        StringBuilder sb = new StringBuilder();
        sb.append("#Prediction\tGold\n");
        for (String p : predictionValues) {
            sb.append(LabelSubstitutor.undoLabelReplacement(p) + "\n");
            // NOTE: CRFSuite has a bug when the label is ':' (as in
            // PennTreeBank Part-of-speech tagset for instance)
            // We perform a substitutions to something crfsuite can handle
            // correctly, see class
            // LabelSubstitutor for more details
        }
        FileUtils.writeStringToFile(predictionsFile, sb.toString(), "utf-8");

    }

    static File loadAndPrepareFeatureDataFile(TaskContext aContext, File tmpLocation,
            String sourceFolder)
        throws Exception
    {

        // Depending on a users' setup, we might exceed the path-length limit of 255
        // character, which is a problem on Windows, we provide all needed files in the
        // folder where the binary is located to enforce short paths

        File folder = aContext.getFolder(sourceFolder, AccessMode.READONLY);
        File srcFile = new File(folder, FILENAME_DATA_IN_CLASSIFIER_FORMAT);

        if (srcFile.getAbsolutePath().length() < 254 || !isWindows()) {
            return ResourceUtils.getUrlAsFile(srcFile.toURI().toURL(), true);
        }

        File trainDestFile = new File(tmpLocation, FILENAME_DATA_IN_CLASSIFIER_FORMAT);

        InputStream is = null;
        try {
            is = new FileInputStream(srcFile);
            FileUtils.copyInputStreamToFile(is, trainDestFile);
        }
        finally {
            IOUtils.closeQuietly(is);
        }

        return ResourceUtils.getUrlAsFile(trainDestFile.toURI().toURL(), true);
    }

    private static boolean isWindows()
    {
        return detector.getPlatformId().startsWith(PlatformDetector.OS_WINDOWS);
    }

    private File trainModel(TaskContext aContext) throws Exception
    {
        File executable = getExecutable();
        File train = loadAndPrepareFeatureDataFile(aContext, executable.getParentFile(),
                TEST_TASK_INPUT_KEY_TRAINING_DATA);

        File modelLocation = new File(executable.getParentFile(), MODEL_CLASSIFIER);

        List<String> command = getTrainCommand(executable, modelLocation, train, algoName,
                algoParameters);
        runTrain(command);

        deleteTmpFeatureFileIfCreated(aContext, train, TEST_TASK_INPUT_KEY_TRAINING_DATA);

        return writeModel(aContext, modelLocation);
    }

    private String testModel(TaskContext aContext) throws Exception
    {

        File executable = getExecutable();
        File testFile = loadAndPrepareFeatureDataFile(aContext, executable.getParentFile(),
                TEST_TASK_INPUT_KEY_TEST_DATA);

        List<String> command = getTestCommand(executable, testFile, model);
        String output = runTest(command).toString();

        deleteTmpFeatureFileIfCreated(aContext, testFile, TEST_TASK_INPUT_KEY_TEST_DATA);

        return output;
    }

    public static List<String> getTestCommand(File anExecutable, File aTestFile, File aModel)
    {
        List<String> commandTestModel = new ArrayList<String>();
        commandTestModel.add(anExecutable.getAbsolutePath());
        commandTestModel.add("tag");
        commandTestModel.add("-r");
        commandTestModel.add("-m");
        commandTestModel.add(aModel.getAbsolutePath());
        commandTestModel.add(aTestFile.getAbsolutePath());
        return commandTestModel;
    }

    public static List<String> getTrainCommand(File executable, File model, File train,
            String algorithm, List<String> algoParameter)
        throws Exception
    {
        List<String> commandTrainModel = new ArrayList<String>();
        commandTrainModel.add(executable.getAbsolutePath());
        commandTrainModel.add("learn");
        commandTrainModel.add("-m");
        commandTrainModel.add(model.getAbsolutePath());

        commandTrainModel.add("-a");
        commandTrainModel.add(algorithm);

        for (String p : algoParameter) {
            commandTrainModel.add(p.replaceAll(" ", ""));
        }

        commandTrainModel.add(train.getAbsolutePath());

        return commandTrainModel;
    }

    public static StringBuilder runTest(List<String> aTestModelCommand) throws Exception
    {
        Process process = new ProcessBuilder().command(aTestModelCommand).start();
        StringBuilder output = captureProcessOutput(process);
        return output;
    }

    public static StringBuilder captureProcessOutput(Process aProcess)
    {
        InputStream src = aProcess.getInputStream();
        Scanner sc = new Scanner(src, "utf-8");
        StringBuilder dest = new StringBuilder();
        while (sc.hasNextLine()) {
            String l = sc.nextLine();
            dest.append(l + "\n");
        }
        sc.close();
        return dest;
    }

    private void deleteTmpFeatureFileIfCreated(TaskContext aContext, File input, String key)
    {
        File folder = aContext.getFolder(key, AccessMode.READONLY);
        File f = new File(folder, FILENAME_DATA_IN_CLASSIFIER_FORMAT);

        if (f.getAbsolutePath().length() >= 254 && isWindows()) {
            FileUtils.deleteQuietly(input);
        }
    }

    private void runTrain(List<String> aModelTrainCommand) throws Exception
    {
        Process process = new ProcessBuilder().inheritIO().command(aModelTrainCommand).start();
        process.waitFor();
    }

    private File writeModel(TaskContext aContext, File model) throws Exception
    {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(model);
            aContext.storeBinary(MODEL_CLASSIFIER, fis);
        }
        finally {
            IOUtils.closeQuietly(fis);
        }
        File modelLocation = aContext.getFile(MODEL_CLASSIFIER, AccessMode.READONLY);
        FileUtils.deleteQuietly(model);
        return modelLocation;
    }

    private void processParameters(List<Object> classificationArguments) throws Exception
    {
        algoName = CrfUtil.getAlgorithm(classificationArguments);
        algoParameters = CrfUtil.getAlgorithmConfigurationParameter(classificationArguments);
    }

    public static File getExecutable() throws Exception
    {

        if (runtimeProvider == null) {
            String platform = detector.getPlatformId();
            LogFactory.getLog(CrfSuiteTestTask.class.getName())
                    .info("Load binary for platform: [" + platform + "]");

            runtimeProvider = new RuntimeProvider("classpath:/org/dkpro/tc/ml/crfsuite/");
        }

        return runtimeProvider.getFile("crfsuite");
    }
}
