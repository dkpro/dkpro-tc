/*******************************************************************************
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
 ******************************************************************************/
package org.dkpro.tc.ml.libsvm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.pear.util.FileUtil;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.libsvm.api.LibsvmPredict;
import org.dkpro.tc.ml.libsvm.api.LibsvmTrainModel;

import libsvm.svm;
import libsvm.svm_model;

/**
 * Libsvm offers various configuration switches for the SVM type and Kernel we defined constant
 * values in this class with more detail explanations.
 * 
 * <pre>
 * -s svm_type (see constants)
 * -s kernel_type (see constants) 
 * -d degree : set degree in kernel function (default 3) 
 * -g gamma : set gamma in kernel function (default 1/num_features) 
 * -r coef0 : set coef0 in kernel function (default 0)
 * -c cost : set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1)
 * -n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5) 
 * -p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1) 
 * -m cachesize : set cache memory size in MB (default 100) 
 * -e epsilon : set tolerance of termination criterion (default 0.001) 
 * -h shrinking: whether to use the shrinking heuristics, 0 or 1 (default 1) 
 * -b probability_estimates : whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0) 
 * -wi weight : set the parameter C of class i to weight*C, for C-SVC (default 1)
 * </pre>
 */
public class LibsvmTestTask
    extends ExecutableTaskBase
    implements Constants
{
    @Discriminator(name = DIM_CLASSIFICATION_ARGS)
    private List<String> classificationArguments;
    @Discriminator(name = DIM_LEARNING_MODE)
    private String learningMode;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        if (multiLabel) {
            throw new TextClassificationException("Multi-label is not yet implemented");
        }

        File fileTrain = getTrainFile(aContext);

        File model = new File(aContext.getFolder("", AccessMode.READWRITE),
                Constants.MODEL_CLASSIFIER);

        LibsvmTrainModel ltm = new LibsvmTrainModel();
        ltm.run(buildParameters(fileTrain, model));
        prediction(model, aContext);
    }

    private String[] buildParameters(File fileTrain, File model)
    {
        List<String> parameters = new ArrayList<>();
        if (classificationArguments != null) {
            for (String a : classificationArguments) {
                parameters.add(a);
            }
        }
        parameters.add(fileTrain.getAbsolutePath());
        parameters.add(model.getAbsolutePath());
        return parameters.toArray(new String[0]);
    }

    private void prediction(File model, TaskContext aContext)
        throws Exception
    {
        File fileTest = getTestFile(aContext);
        LibsvmPredict predictor = new LibsvmPredict();

        BufferedReader r = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileTest), "utf-8"));
        File prediction = getPredictionFile(aContext);

        File predTmp = createTemporaryPredictionFile(aContext);

        DataOutputStream output = new DataOutputStream(new FileOutputStream(predTmp));
        svm_model trainedModel = svm.svm_load_model(model.getAbsolutePath());
        predictor.predict(r, output, trainedModel, 0);
        output.close();

        mergePredictedValuesWithExpected(fileTest, predTmp, prediction);
        copyOutcomeMappingToThisFolder(aContext);
    }

    private void copyOutcomeMappingToThisFolder(TaskContext aContext)
        throws IOException
    {
        File trainDataFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        String mapping = LibsvmAdapter.getOutcomeMappingFilename();

        File outFolder = aContext.getFolder("", AccessMode.READWRITE);
        FileUtils.copyFile(new File(trainDataFolder, mapping), new File(outFolder, mapping));
    }

    // We only get the predicted values but we loose the information which value was expected - we
    // thus use the test file and restore the expected values from there
    private void mergePredictedValuesWithExpected(File fileTest, File predTmp, File prediction)
        throws IOException
    {
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(prediction), "utf-8"));

        List<String> gold = pickGold(FileUtils.readLines(fileTest));
        List<String> pred = FileUtils.readLines(predTmp);
        bw.write("#PREDICTION;GOLD" + "\n");
        for (int i = 0; i < gold.size(); i++) {
            String p = pred.get(i).replaceAll("\\.0", ""); // primitive double to int conversion
            String g = gold.get(i);
            bw.write(p + ";" + g);
            bw.write("\n");
        }
        bw.close();
    }

    private List<String> pickGold(List<String> readLines)
    {
        List<String> gold = new ArrayList<>();
        for (String l : readLines) {
            if (l.isEmpty()) {
                continue;
            }
            int indexOf = l.indexOf("\t");
            gold.add(l.substring(0, indexOf));
        }

        return gold;
    }

    private File createTemporaryPredictionFile(TaskContext aContext)
        throws IOException
    {
        return FileUtil.createTempFile("libsvmPrediction" + System.nanoTime(), ".libsvmtmp");
    }

    private File getPredictionFile(TaskContext aContext)
    {
        String fileName = LibsvmAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.predictionsFile);
        File folder = aContext.getFolder("", AccessMode.READWRITE);

        return new File(folder, fileName);
    }

    private File getTestFile(TaskContext aContext)
    {
        File testFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY);
        String testFileName = LibsvmAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        File fileTest = new File(testFolder, testFileName);
        return fileTest;
    }

    private File getTrainFile(TaskContext aContext)
    {
        File trainFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        String trainFileName = LibsvmAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        File fileTrain = new File(trainFolder, trainFileName);

        return fileTrain;
    }

    /** SVM type is set by switch [-s] */
    public static final String PARAM_SVM_TYPE_C_SVC_MULTI_CLASS = "0";
    /** SVM type is set by switch [-s] */
    public static final String PARAM_SVM_TYPE_NU_SVC_MULTI_CLASS = "1";
    /** SVM type is set by switch [-s] */
    public static final String PARAM_SVM_TYPE_ONE_CLASS_SVM = "2";
    /** SVM type is set by switch [-s] */
    public static final String PARAM_SVM_TYPE_EPISLON_SVR_REGRESSION = "3";
    /** SVM type is set by switch [-s] */
    public static final String PARAM_SVM_TYPE_NU_SVR_REGRESSION = "4";

    /** Polynomial kernel -> u'*v, set by switch [-t] (default) */
    public static final String PARAM_KERNEL_LINEAR = "0";
    /** Polynomial kernel -> (gamma*u'*v + coef0)^degree, set by switch [-t] */
    public static final String PARAM_KERNEL_POLYNOMIAL = "1";
    /** Radial based -> exp(-gamma*|u-v|^2), set by switch [-t] */
    public static final String PARAM_KERNEL_RADIAL_BASED = "2";
    /** sigmoid -> tanh(gamma*u'*v + coef0), set by switch [-t] */
    public static final String PARAM_KERNEL_SIGMOID = "3";
    /** precomputed kernel (kernel values in training_set_file), set by switch [-t] */
    public static final String PARAM_KERNEL_PRE_COMPUTED = "4";

}