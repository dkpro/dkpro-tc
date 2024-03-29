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

package org.dkpro.tc.ml.vowpalwabbit;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.TcShallowClassifierTaskBase;
import org.dkpro.tc.ml.vowpalwabbit.core.VowpalWabbit;
import org.dkpro.tc.ml.vowpalwabbit.core.VowpalWabbitPredictor;
import org.dkpro.tc.ml.vowpalwabbit.core.VowpalWabbitTrainer;

import de.tudarmstadt.ukp.dkpro.core.api.resources.PlatformDetector;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

public class VowpalWabbitTestTask
    extends TcShallowClassifierTaskBase
{

    @Discriminator(name = DIM_LEARNING_MODE)
    protected String learningMode;

    @Discriminator(name = DIM_FEATURE_MODE)
    protected String featureMode;

    @Override
    public void execute(TaskContext aContext) throws Exception
    {
        super.execute(aContext);
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        if (multiLabel) {
            throw new TextClassificationException("Multi-label requested, but not supported.");
        }

        File model = trainModel(aContext);
        List<String> predictionValues = testModel(aContext, model);

        writeFileWithPredictedLabels(aContext, predictionValues);

    }

    protected void writeFileWithPredictedLabels(TaskContext aContext, List<String> predictionValues)
        throws Exception
    {
        File predictionsFile = aContext.getFile(Constants.FILENAME_PREDICTIONS,
                AccessMode.READWRITE);

        StringBuilder sb = new StringBuilder();
        for (String p : predictionValues) {
            sb.append(p + "\n");
        }
        FileUtils.writeStringToFile(predictionsFile, sb.toString(), UTF_8);

    }

    public static File loadAndPrepareFeatureDataFile(TaskContext aContext, File tmpLocation,
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

        try (InputStream is = new FileInputStream(srcFile)) {
            FileUtils.copyInputStreamToFile(is, trainDestFile);
        }

        return ResourceUtils.getUrlAsFile(trainDestFile.toURI().toURL(), true);
    }

    protected static boolean isWindows()
    {
        return VowpalWabbit.getPlatformDetector().getPlatformId()
                .startsWith(PlatformDetector.OS_WINDOWS);
    }

    protected File trainModel(TaskContext aContext) throws Exception
    {
        VowpalWabbitTrainer trainer = new VowpalWabbitTrainer();

        File executable = new VowpalWabbit().getExecutable();
        File train = loadAndPrepareFeatureDataFile(aContext, executable.getParentFile(),
                TEST_TASK_INPUT_KEY_TRAINING_DATA);
        File modelLocation = new File(executable.getParentFile(), MODEL_CLASSIFIER);

        List<String> parameters = getParameters(classificationArguments);
        parameters.remove(VowpalWabbitAdapter.class.getSimpleName());

        parameters = automaticallyAddParametersForClassificationMode(aContext, parameters,
                learningMode, featureMode);

        trainer.train(train, modelLocation, parameters);

        deleteTmpFeatureFileIfCreated(aContext, train, TEST_TASK_INPUT_KEY_TRAINING_DATA);

        return writeModel(aContext, modelLocation);
    }

    public static List<String> automaticallyAddParametersForClassificationMode(TaskContext aContext,
            List<String> parameters, String learningMode, String featureMode)
        throws IOException
    {
        if (!isClassification(learningMode)) {
            return parameters;
        }

        if (isSequenceMode(featureMode)) {

            if (!containsRequiredSeqParameter(parameters, "--search")) {
                parameters = addParameter(parameters, "--search",
                        determineNumberOfClasses(aContext).toString());
            }
            if (!containsRequiredSeqParameter(parameters, "--search_task")) {
                parameters = addParameter(parameters, "--search_task", "sequence");
            }
            if (!containsRequiredSeqParameter(parameters, "--search_passes_per_policy")) {
                parameters = addParameter(parameters, "--search_passes_per_policy", "2");
                LogFactory.getLog(VowpalWabbitTestTask.class).debug(
                        "Fallback configuration: set parameter [--search_passes_per_policy] to value [2]");
            }
            if (!containsRequiredSeqParameter(parameters, "--cache")) {
                parameters = addParameter(parameters, "--cache", null);
            }

        }
        else {

            if (!containsNeededNonSequenceClassificationParameter(parameters)) {
                LogFactory.getLog(VowpalWabbitTestTask.class).info(
                        "No classification strategy provided, falling back to classification strategy [--oaa]");
                parameters.add("--oaa");
            }
            parameters.add(determineNumberOfClasses(aContext).toString());
        }

        return parameters;
    }

    protected static List<String> addParameter(List<String> parameters, String parameter,
            String parameterValue)
    {
        parameters.add(parameter);
        if (parameterValue != null) {
            parameters.add(parameterValue.toString());
        }

        return parameters;
    }

    protected static boolean containsRequiredSeqParameter(List<String> parameters, String parameter)
    {
        for (String s : parameters) {
            if (s.equals(parameter)) {
                return true;
            }
        }

        return false;
    }

    protected static boolean isSequenceMode(String featureMode)
    {
        return featureMode.equals(FM_SEQUENCE);
    }

    public static boolean containsNeededNonSequenceClassificationParameter(List<String> parameters)
    {
        for (String strategy : new String[] { "--csoaa_ldf", "--wap", "--csoaa", "--ect", "--oaa",
                "--log_multi" }) {
            if (parameters.contains(strategy)) {
                return true;
            }
        }
        return false;
    }

    protected static Integer determineNumberOfClasses(TaskContext aContext) throws IOException
    {
        File folder = aContext.getFolder(OUTCOMES_INPUT_KEY, AccessMode.READONLY);
        File file = new File(folder, FILENAME_OUTCOMES);
        List<String> outcomes = FileUtils.readLines(file, UTF_8);
        return outcomes.size();
    }

    public static boolean isClassification(String learningMode)
    {

        return learningMode.equals(Constants.LM_SINGLE_LABEL);
    }

    protected List<String> getParameters(List<Object> subList)
    {
        List<String> s = new ArrayList<>();

        for (Object o : subList) {
            s.add(o.toString());
        }

        return s;
    }

    protected List<String> testModel(TaskContext aContext, File model) throws Exception
    {
        File executable = new VowpalWabbit().getExecutable();
        File testFile = loadAndPrepareFeatureDataFile(aContext, executable.getParentFile(),
                TEST_TASK_INPUT_KEY_TEST_DATA);

        VowpalWabbitPredictor predictor = new VowpalWabbitPredictor();
        List<String> prediction = predictor.predict(testFile, model);

        return prediction;
    }

    protected void deleteTmpFeatureFileIfCreated(TaskContext aContext, File input, String key)
    {
        File folder = aContext.getFolder(key, AccessMode.READONLY);
        File f = new File(folder, FILENAME_DATA_IN_CLASSIFIER_FORMAT);

        if (f.getAbsolutePath().length() >= 254 && isWindows()) {
            FileUtils.deleteQuietly(input);
        }
    }

    protected File writeModel(TaskContext aContext, File model) throws Exception
    {

        try (FileInputStream fis = new FileInputStream(model)) {
            aContext.storeBinary(MODEL_CLASSIFIER, fis);
        }
        File modelLocation = aContext.getFile(MODEL_CLASSIFIER, AccessMode.READONLY);
        FileUtils.deleteQuietly(model);
        return modelLocation;
    }

}
