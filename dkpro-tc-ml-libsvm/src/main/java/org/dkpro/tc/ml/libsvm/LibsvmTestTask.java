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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class LibsvmTestTask
    extends ExecutableTaskBase
    implements Constants
{
    @Discriminator(name = DIM_CLASSIFICATION_ARGS)
    private List<String> classificationArguments;
    @Discriminator(name = DIM_LEARNING_MODE)
    private String learningMode;

    private Map<String, Integer> outcome2id = new HashMap<>();

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        exceptMultiLabelMode();

        boolean isRegression = learningMode.equals(Constants.LM_REGRESSION);

        buildOutcome2IntegerMap(aContext, isRegression);
        File fileTrain = replaceOutcomeByIntegers(getTrainFile(aContext));

        File model = new File(aContext.getFolder("", AccessMode.READWRITE),
                Constants.MODEL_CLASSIFIER);

        LibsvmTrainModel ltm = new LibsvmTrainModel();
        ltm.run(buildParameters(fileTrain, model));
        prediction(model, aContext);

        writeMapping(aContext,isRegression);
    }

    private void exceptMultiLabelMode()
        throws TextClassificationException
    {
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);
        if (multiLabel) {
            throw new TextClassificationException("Multi-label is not supported");
        }
    }

    private void writeMapping(TaskContext aContext, boolean isRegression)
        throws IOException
    {
        if(isRegression){
            //regression has no mapping
            return;
        }
        
        String map2String = map2String(outcome2id);
        File file = aContext.getFile(LibsvmAdapter.getOutcomeMappingFilename(),
                AccessMode.READWRITE);
        FileUtils.writeStringToFile(file, map2String, "utf-8");
    }

    private File replaceOutcomeByIntegers(File trainFile)
        throws IOException
    {
        File createTempFile = FileUtil.createTempFile("libsvmTrainFile", ".tmp_libsvm");
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(createTempFile), "utf-8"));

        for (String s : FileUtils.readLines(trainFile)) {
            if (s.isEmpty()) {
                continue;
            }
            int indexOf = s.indexOf("\t");
            String val = map(s, indexOf);
            bw.write(val);
            bw.write(s.substring(indexOf));
            bw.write("\n");
        }

        bw.close();

        return createTempFile;
    }

    private String map(String s, int indexOf)
    {
        String outcome = s.substring(0, indexOf);
        Integer integer = outcome2id.get(outcome);
        if (integer == null) {
            // happens for regression i.e. no mapping needed
            return outcome;
        }
        return integer.toString();
    }

    private void buildOutcome2IntegerMap(TaskContext aContext, boolean isRegression)
        throws IOException
    {
        if (isRegression) {
            // no mapping for regression
            return;
        }

        String outcomes = LibsvmAdapter.getOutcomesFile();

        File folder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY);
        File trainOutcomes = new File(folder, outcomes);

        folder = aContext.getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY);
        File testOutcomes = new File(folder, outcomes);

        Set<String> uniqueOutcomes = new HashSet<>();
        uniqueOutcomes.addAll(FileUtils.readLines(testOutcomes));
        uniqueOutcomes.addAll(FileUtils.readLines(trainOutcomes));

        int i = 0;
        for (String o : uniqueOutcomes) {
            outcome2id.put(o, i++);
        }
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
        File fileTest = replaceOutcomeByIntegers(getTestFile(aContext));
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
            String p = pred.get(i); 
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

    private String map2String(Map<String, Integer> map)
    {
        StringBuilder sb = new StringBuilder();
        for (String k : map.keySet()) {
            sb.append(k + "\t" + map.get(k) + "\n");
        }

        return sb.toString();
    }

}