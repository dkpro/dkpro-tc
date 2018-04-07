/**
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
 */

package org.dkpro.tc.ml.svmhmm.task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatTestTask;
import org.dkpro.tc.ml.svmhmm.core.SvmHmmPredictor;
import org.dkpro.tc.ml.svmhmm.core.SvmHmmTrainer;

public class SvmHmmTestTask
    extends LibsvmDataFormatTestTask
    implements Constants
{

    private void combinePredictionAndExpectedGoldLabels(File fileTest, File predictionsFile)
        throws Exception
    {

        BufferedReader readerPrediction = new BufferedReader(
                new InputStreamReader(new FileInputStream(predictionsFile), "utf-8"));
        BufferedReader readerGold = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileTest), "utf-8"));

        File createTempFile = File.createTempFile("svmhmm", ".txt");
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(createTempFile), "utf-8"));

        String prediction = null;
        String gold = null;

        writer.write("#PREDICTION;GOLD" + "\n");
        do {

            prediction = readerPrediction.readLine();
            gold = readerGold.readLine();

            if (prediction == null || gold == null) {
                break;
            }

            gold = gold.split("\t")[0];
            writer.write(prediction + ";" + gold + "\n");

        }
        while (true);

        writer.close();
        readerGold.close();
        readerPrediction.close();

        FileUtils.deleteQuietly(predictionsFile);
        FileUtils.moveFile(createTempFile, predictionsFile);
    }

    @Override
    protected Object trainModel(TaskContext aContext) throws Exception
    {

        File fileTrain = getTrainFile(aContext);

        // SvmHmm struggles with paths longer than 255 characters to circumvent this
        // issue, we copy all files together into a local directory to ensure short path
        // names that are below this threshold
        File newTrainFileLocation = new File(SvmHmmTrainer.getTrainExecutable().getParentFile(),
                fileTrain.getName());
        File tmpModelLocation = new File(SvmHmmTrainer.getTrainExecutable().getParentFile(),
                "model.tmp");
        FileUtils.copyFile(fileTrain, newTrainFileLocation);

        SvmHmmTrainer trainer = new SvmHmmTrainer();
        trainer.train(newTrainFileLocation, tmpModelLocation, getParameters());

        File modelFile = new File(aContext.getFolder("", AccessMode.READWRITE),
                Constants.MODEL_CLASSIFIER);
        FileUtils.copyFile(tmpModelLocation, modelFile);

        FileUtils.deleteQuietly(newTrainFileLocation);
        FileUtils.deleteQuietly(tmpModelLocation);

        return modelFile;
    }

    private String[] getParameters()
    {
        List<String> stringArgs = new ArrayList<>();
        for (int i = 1; i < classificationArguments.size(); i++) {
            stringArgs.add((String) classificationArguments.get(i));
        }

        return stringArgs.toArray(new String[0]);
    }

    @Override
    protected void runPrediction(TaskContext aContext, Object model) throws Exception
    {

        File fileTest = getTestFile(aContext);
        File modelFile = (File) model;

        // SvmHmm struggles with paths longer than 255 characters to circumvent this
        // issue, we copy all files together into a local directory to ensure short path
        // names that are below this threshold
        File localModel = new File(SvmHmmPredictor.getPredictionExecutable().getParentFile(),
                "model.tmp");
        FileUtils.copyFile(modelFile, localModel);
        File localTestFile = new File(SvmHmmPredictor.getPredictionExecutable().getParentFile(),
                "testfile.txt");
        FileUtils.copyFile(fileTest, localTestFile);

        File predictions = new File(aContext.getFolder("", AccessMode.READWRITE),
                Constants.FILENAME_PREDICTIONS);

        SvmHmmPredictor predictor = new SvmHmmPredictor();
        predictor.predict(localTestFile, localModel, predictions);

        FileUtils.deleteQuietly(localModel);
        FileUtils.deleteQuietly(localTestFile);

        combinePredictionAndExpectedGoldLabels(fileTest, predictions);
    }

}
