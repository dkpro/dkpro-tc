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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
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

public class CRFSuiteTestTask
    extends ExecutableTaskBase
    implements Constants
{
    @Discriminator(name = DIM_LEARNING_MODE)
    private String learningMode;
    
    @Discriminator(name = DIM_CLASSIFICATION_ARGS)
    private List<Object> classificationArguments;

    public static final String FILE_PER_CLASS_PRECISION_RECALL_F1 = "precisionRecallF1PerWordClass.txt";
    Log logger = null;

    private String executablePath = null;
    private String modelLocation = null;
    private File trainFile = null;
    private File testFile = null;
    private String algoName;
    private List<String> algoParameters;

    private static RuntimeProvider runtimeProvider = null;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        if (multiLabel) {
            throw new TextClassificationException(
                    "Multi-label requested, but CRFSuite only supports single label setups.");
        }

        processParameters(classificationArguments);

        executablePath = getExecutablePath();
        modelLocation = trainModel(aContext);
        String rawTextOutput = testModel(aContext);

        writePredictions2File(aContext, rawTextOutput);

    }

    private void processParameters(List<Object> classificationArguments)
        throws Exception
    {
        algoName = CRFUtil.getAlgorithm(classificationArguments);
        algoParameters = CRFUtil.getAlgorithmConfigurationParameter(classificationArguments);
    }

    public static String getExecutablePath()
        throws Exception
    {

        if (runtimeProvider == null) {
            PlatformDetector pd = new PlatformDetector();
            String platform = pd.getPlatformId();
            LogFactory.getLog(CRFSuiteTestTask.class.getName())
                    .info("Load binary for platform: [" + platform + "]");

            runtimeProvider = new RuntimeProvider("classpath:/org/dkpro/tc/ml/crfsuite/");
        }

        String executablePath = runtimeProvider.getFile("crfsuite").getAbsolutePath();

        LogFactory.getLog(CRFSuiteTestTask.class.getName())
                .info("Will use binary: [" + executablePath + "]");

        return executablePath;
    }

    private void writePredictions2File(TaskContext aContext, String aRawTextOutput)
        throws Exception
    {

        writeCRFSuiteGeneratedReports2File(aContext);

        List<String> predictionValues = new ArrayList<String>(
                Arrays.asList(aRawTextOutput.split("\n")));

        writeFileWithPredictedLabels(aContext, predictionValues);
    }

    private void writeFileWithPredictedLabels(TaskContext aContext, List<String> predictionValues)
        throws Exception
    {
        File predictionsFile = aContext.getFile(Constants.FILENAME_PREDICTIONS, AccessMode.READWRITE);

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

    private void writeCRFSuiteGeneratedReports2File(TaskContext aContext)
        throws Exception
    {
        String precRecF1perClass = getPrecisionRecallF1PerClass();
        log(precRecF1perClass);
        File precRecF1File = aContext.getFile(FILE_PER_CLASS_PRECISION_RECALL_F1,
                AccessMode.READWRITE);
        FileUtils.write(precRecF1File, "\n" + precRecF1perClass, "utf-8");
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
        String output = captureProcessOutput(process).toString();

        return output;
    }

    private String testModel(TaskContext aContext)
        throws Exception
    {

        List<String> testModelCommand = buildTestCommand(aContext);
        log("Testing model");
        String output = runTest(testModelCommand).toString();
        log("Testing model finished");

        return output;
    }

    public static StringBuilder runTest(List<String> aTestModelCommand)
        throws Exception
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

    private List<String> buildTestCommand(TaskContext aContext)
        throws Exception
    {
        File tmpFileFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY);
        File tmpTest = new File(tmpFileFolder.getPath() + "/" + FILENAME_DATA_IN_CLASSIFIER_FORMAT);
        testFile = ResourceUtils.getUrlAsFile(tmpTest.toURI().toURL(), true);

        return wrapTestCommandAsList(testFile, executablePath, modelLocation);
    }

    public static List<String> wrapTestCommandAsList(File aTestFile, String aExecutablePath,
            String aModelLocation)
    {
        List<String> commandTestModel = new ArrayList<String>();
        commandTestModel.add(aExecutablePath);
        commandTestModel.add("tag");
        commandTestModel.add("-r");
        commandTestModel.add("-m");
        commandTestModel.add(aModelLocation);
        commandTestModel.add(aTestFile.getAbsolutePath());
        return commandTestModel;
    }

    private String trainModel(TaskContext aContext)
        throws Exception
    {
        String tmpModelLocation = System.getProperty("java.io.tmpdir") + File.separator
                + MODEL_CLASSIFIER;
        List<String> modelTrainCommand = buildTrainCommand(aContext, tmpModelLocation);
        runTrain(modelTrainCommand);
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
        aContext.storeBinary(MODEL_CLASSIFIER, new FileInputStream(new File(aTmpModelLocation)));
        File modelLocation = aContext.getFile(MODEL_CLASSIFIER, AccessMode.READONLY);
        return modelLocation.getAbsolutePath();
    }

    private List<String> buildTrainCommand(TaskContext aContext, String aTmpModelLocation)
        throws Exception
    {
        File trainFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        File tmpTrain = new File(trainFolder.getPath() + "/" + FILENAME_DATA_IN_CLASSIFIER_FORMAT);

        trainFile = ResourceUtils.getUrlAsFile(tmpTrain.toURI().toURL(), true);

        return getTrainCommand(aTmpModelLocation, trainFile.getAbsolutePath(), algoName,
                algoParameters);
    }

    public static List<String> getTrainCommand(String modelOutputLocation, String trainingFile,
            String algorithm, List<String> algoParameter)
                throws Exception
    {
        List<String> commandTrainModel = new ArrayList<String>();
        commandTrainModel.add(getExecutablePath());
        commandTrainModel.add("learn");
        commandTrainModel.add("-m");
        commandTrainModel.add(modelOutputLocation);

        commandTrainModel.add("-a");
        commandTrainModel.add(algorithm);
        
        for(String p : algoParameter){
            commandTrainModel.add(p.replaceAll(" ", ""));
        }

        commandTrainModel.add(trainingFile);
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
