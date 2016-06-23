/*
 * Copyright 2016
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
 */

package org.dkpro.tc.svmhmm.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;

import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.svmhmm.SVMHMMAdapter;
import org.dkpro.tc.svmhmm.util.SVMHMMUtils;

/**
 * Wrapper for training and testing using SVM_HMM C implementation with default parameters. Consult
 * {@code http://www.cs.cornell.edu/people/tj/svm_light/svm_hmm.html} for parameter settings.
 * 
 * <pre>
 * Parameters:
 *   -c      Typical SVM parameter C trading-off slack vs. magnitude of the weight-vector. 
 *               NOTE: The default value for this parameter is unlikely to work well for your
 *               particular problem. A good value for C must be selected via cross-validation, ideally
 *               exploring values over several orders of magnitude. NOTE: Unlike in V1.01, the value of C is
 *               divided by the number of training examples. So, to get results equivalent to V1.01, multiply
 *               C by the number of training examples. Default value is set to 1.
 *   -e      Parameter "-e &lt;EPSILON&gt;": This specifies the precision to which constraints are
 *               required to be satisfied by the solution. The smaller EPSILON, the longer and the more memory
 *               training takes, but the solution is more precise. However, solutions more accurate than 0.5
 *               typically do not improve prediction accuracy.
 *   -t      Order of dependencies of transitions in HMM. Can be any number larger than 1. (default 1)
 *   -m      Order of dependencies of emissions in HMM. Can be any number
 *               larger than 0. (default 0) UPDATE: according to svm_struct_api.c: must be either 0 or 1;
 *               fails for &gt;1
 *   -b       A non-zero value turns on (approximate) beam search to replace
 *               the exact Viterbi algorithm both for finding the most violated constraint, as well as for
 *               computing predictions. The value is the width of the beam used (e.g. 100). (default 0).
 * 
 * </pre>
 */
public class SVMHMMTestTask
    extends ExecutableTaskBase
    implements Constants
{
    /*
     * for svm_hmm debugging purposes
     */
    public static boolean PRINT_STD_OUT = false;

    private static final String BINARIES_BASE_LOCATION = "classpath:/org/dkpro/tc/svmhmm/";

    /**
     * Learning mode discriminators; Only Constants.LM_SINGLE_LABEL is allowed
     */
    @Discriminator(name = DIM_LEARNING_MODE)
    private String learningMode = LM_SINGLE_LABEL;

    @Discriminator(name = DIM_CLASSIFICATION_ARGS)
    private List<String> classificationArguments;

    private double paramC = 5;

    private double paramEpsilon = 0.5;

    private int paramOrderT = 1;

    private int paramOrderE = 0;

    private int paramB = 0;

    // where the trained model is stored
    private static final String MODEL_NAME = "svm_struct.model";

    // logger
    private static Log log = LogFactory.getLog(SVMHMMTestTask.class);

    // mapping outcome labels to integers
    protected BidiMap labelsToIntegersMapping;

    @Override
    public void execute(TaskContext taskContext)
        throws Exception
    {
        processParameters(classificationArguments);

        // where the training date are located
        File trainingDataStorage = taskContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                StorageService.AccessMode.READONLY);

        // where the test data are located
        File testDataStorage = taskContext.getFolder(TEST_TASK_INPUT_KEY_TEST_DATA,
                StorageService.AccessMode.READONLY);

        // file name of the data; THE FILES HAVE SAME NAME FOR BOTH TRAINING AND
        // TESTING!!!!!!
        String fileName = new SVMHMMAdapter().getFrameworkFilename(
                TCMachineLearningAdapter.AdapterNameEntries.featureVectorsFile);

        File trainingFile = new File(trainingDataStorage, fileName);
        File testFile = new File(testDataStorage, fileName);

        if (!Constants.LM_SINGLE_LABEL.equals(learningMode)) {
            throw new TextClassificationException(
                    learningMode + " was requested but only single label setup is supported.");
        }

        // mapping outcome labels to integers
        SortedSet<String> outcomeLabels = SVMHMMUtils
                .extractOutcomeLabelsFromFeatureVectorFiles(trainingFile, testFile);
        labelsToIntegersMapping = SVMHMMUtils.mapVocabularyToIntegers(outcomeLabels);

        // save mapping to file
        File mappingFolder = taskContext.getFolder("", StorageService.AccessMode.READWRITE);
        File mappingFile = new File(mappingFolder,
                SVMHMMUtils.LABELS_TO_INTEGERS_MAPPING_FILE_NAME);
        SVMHMMUtils.saveMapping(labelsToIntegersMapping, mappingFile);

        File augmentedTrainingFile = SVMHMMUtils.replaceLabelsWithIntegers(trainingFile,
                labelsToIntegersMapping);

        File augmentedTestFile = SVMHMMUtils.replaceLabelsWithIntegers(testFile,
                labelsToIntegersMapping);

        // train the model
        trainModel(taskContext, augmentedTrainingFile, paramC, paramOrderE, paramOrderT,
                paramEpsilon, paramB);

        // test the model
        testModel(taskContext, augmentedTestFile);
    }

    private void processParameters(List<String> classificationArguments)
    {
        paramC = SVMHMMUtils.getParameterC(classificationArguments);
        paramEpsilon = SVMHMMUtils.getParameterEpsilon(classificationArguments);
        paramOrderE = SVMHMMUtils.getParameterOrderE_dependencyOfEmissions(classificationArguments);
        paramOrderT = SVMHMMUtils
                .getParameterOrderT_dependencyOfTransitions(classificationArguments);
        paramB = SVMHMMUtils.getParameterBeamWidth(classificationArguments);
    }

    /**
     * Tests the model against the test data and stores the outcomes in the
     * {@link org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries#predictionsFile}
     * file.
     *
     * @param taskContext
     *            context
     * @param testFile
     *            test file
     * @throws Exception
     */
    public void testModel(TaskContext taskContext, File testFile)
        throws Exception
    {
        // file to hold prediction results
        String predictionFileName = new SVMHMMAdapter()
                .getFrameworkFilename(TCMachineLearningAdapter.AdapterNameEntries.predictionsFile);
        File predictionsFile = taskContext.getFile(predictionFileName, AccessMode.READWRITE);

        // location of the trained model
        File modelFile = taskContext.getFile(MODEL_NAME, StorageService.AccessMode.READONLY);

        callTestCommand(predictionsFile, modelFile, testFile);
    }

    public static void callTestCommand(File predictionsFile, File modelFile, File testFile)
        throws Exception
    {
        // create tmp files as workaround to long path bug in svm_hmm
        // if java temp dir is not in /tmp but in a long path dir, this won't
        // work
        File tmpTestFile = File.createTempFile("tmp_svm_hmm_test", ".txt");
        FileUtils.copyFile(testFile, tmpTestFile);

        File tmpModelFile = File.createTempFile("tmp_svm_hmm", ".model");
        FileUtils.copyFile(modelFile, tmpModelFile);

        File tmpPredictionsFile = File.createTempFile("tmp_svm_hmm_predictions", ".txt");

        // command
        List<String> testCommand = buildTestCommand(tmpTestFile, tmpModelFile.getAbsolutePath(),
                tmpPredictionsFile.getAbsolutePath());

        runCommand(testCommand);

        // copy tmp predictions back to the predictions file
        // Note: the prediction file contains the predicted labels starting from index 1 - the
        // id2outcome.txt requires a start at zero i.e. the predictions here and the one you'll find
        // in the id2outcome report are higher by +1 this is not a bug but rather an ugly necessity.
        FileUtils.copyFile(tmpPredictionsFile, predictionsFile);

        // clean up
        FileUtils.deleteQuietly(tmpTestFile);
        FileUtils.deleteQuietly(tmpPredictionsFile);
        FileUtils.deleteQuietly(tmpModelFile);
    }

    /**
     * Trains the model and stores it into the task context
     *
     * @param taskContext
     *            context
     * @throws Exception
     */
    public void trainModel(TaskContext taskContext, File trainingFile, double paramC,
            int paramOrderE, int paramOrderT, double paramEpsilon, int paramB)
                throws Exception
    {
        File tmpModel = trainModelAtTemporaryLocation(trainingFile, paramC, paramOrderE,
                paramOrderT, paramEpsilon, paramB);

        FileInputStream stream = new FileInputStream(tmpModel);
        taskContext.storeBinary(MODEL_NAME, stream);

        // clean-up
        IOUtils.closeQuietly(stream);
        FileUtils.deleteQuietly(tmpModel);
    }

    public void trainModel(File modelOutputFile, File trainingFile, double paramC, int paramOrderE,
            int paramOrderT, double paramEpsilon, int paramB)
                throws Exception
    {
        File tmpModel = trainModelAtTemporaryLocation(trainingFile, paramC, paramOrderE,
                paramOrderT, paramEpsilon, paramB);
        FileUtils.copyFile(tmpModel, modelOutputFile);
        FileUtils.deleteQuietly(tmpModel);
    }

    private File trainModelAtTemporaryLocation(File trainingFile, double paramC, int paramOrderE,
            int paramOrderT, double paramEpsilon, int paramB)
                throws Exception
    {
        File tmpModelFile = File.createTempFile("tmp_svm_hmm", ".model");

        // we have to copy the training file to tmp to prevent long path issue
        // in svm_hmm
        File tmpTrainingFile = File.createTempFile("tmp_svm_hmm_training", ".txt");
        FileUtils.copyFile(trainingFile, tmpTrainingFile);

        List<String> modelTrainCommand = buildTrainCommand(tmpTrainingFile, tmpModelFile.getPath(),
                paramC, paramOrderE, paramOrderT, paramEpsilon, paramB);

        LogFactory.getLog(getClass())
                .info("Use following parametrization: " + toString(modelTrainCommand));

        log.debug("Start training model");
        long time = System.currentTimeMillis();
        runCommand(modelTrainCommand);
        long completedIn = System.currentTimeMillis() - time;
        String formattedDuration = DurationFormatUtils.formatDuration(completedIn, "HH:mm:ss:SS");
        log.info("Training finished after " + formattedDuration);

        FileUtils.deleteQuietly(tmpTrainingFile);

        return tmpModelFile;
    }

    private String toString(List<String> modelTrainCommand)
    {
        StringBuilder sb = new StringBuilder();
        modelTrainCommand.forEach(x -> sb.append(x + " "));
        return sb.toString();
    }

    /**
     * Builds command line parameters for testing predictions
     *
     * @param testFile
     *            test file
     * @param modelLocation
     *            trained model file
     * @param outputPredictions
     *            where the results will be stored
     * @return command as a list of Strings
     */
    private static List<String> buildTestCommand(File testFile, String modelLocation,
            String outputPredictions)
                throws IOException
    {
        List<String> result = new ArrayList<>();

        result.add(resolveSVMHmmClassifyCommand());
        result.add(testFile.getAbsolutePath());
        result.add(modelLocation);
        result.add(outputPredictions);

        return result;
    }

    /**
     * Returns absolute path to svm_hmm_classify binary
     *
     * @return binary path
     */
    public static String resolveSVMHmmClassifyCommand()
    {
        try {
            return new RuntimeProvider(BINARIES_BASE_LOCATION).getFile("svm_hmm_classify")
                    .getAbsolutePath();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns absolute path to svm_hmm_learn binary
     *
     * @return binary path
     */
    public static String resolveSVMHmmLearnCommand()
    {
        try {
            return new RuntimeProvider(BINARIES_BASE_LOCATION).getFile("svm_hmm_learn")
                    .getAbsolutePath();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds the cmd with parameters to run svm_hmm_train
     *
     * @param trainingFile
     *            training file
     * @param targetModelLocation
     *            where the trained model will be stored
     * @return command as list of Strings
     */
    private List<String> buildTrainCommand(File trainingFile, String targetModelLocation,
            double paramC, int paramOrderE, int paramOrderT, double paramEpsilon, int paramB)
    {
        List<String> result = new ArrayList<>();
        result.add(resolveSVMHmmLearnCommand());

        // svm struct params
        result.add("-c");
        result.add(String.format(Locale.ENGLISH, "%f", this.paramC));
        result.add("--e");
        result.add(Integer.toString(this.paramOrderE));
        result.add("--t");
        result.add(Integer.toString(this.paramOrderT));
        result.add("-e");
        result.add(String.format(Locale.ENGLISH, "%f", this.paramEpsilon));
        result.add("--b");
        result.add(Integer.toString(this.paramB));

        // training file
        result.add(trainingFile.getAbsolutePath());

        // output model
        result.add(targetModelLocation);

        return result;
    }

    /**
     * Executes the command (runs a new process outside the JVM and waits for its completion)
     *
     * @param command
     *            command as list of Strings
     */
    private static void runCommand(List<String> command)
        throws Exception
    {
        log.info(StringUtils.join(command, " "));

        if (PRINT_STD_OUT) {
            Process process = new ProcessBuilder().inheritIO().command(command).start();
            process.waitFor();
        }
        else {
            // create temp files for capturing output instead of printing to
            // stdout/stderr
            File tmpOutLog = File.createTempFile("tmp.out.", ".log");

            ProcessBuilder processBuilder = new ProcessBuilder(command);

            processBuilder.redirectError(tmpOutLog);
            processBuilder.redirectOutput(tmpOutLog);

            // run the process
            Process process = processBuilder.start();
            process.waitFor();

            // re-read the output and debug it
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(tmpOutLog)));
            String line;
            while ((line = br.readLine()) != null) {
                log.debug(line);
            }
            IOUtils.closeQuietly(br);

            // delete files
            FileUtils.deleteQuietly(tmpOutLog);
        }
    }
}
