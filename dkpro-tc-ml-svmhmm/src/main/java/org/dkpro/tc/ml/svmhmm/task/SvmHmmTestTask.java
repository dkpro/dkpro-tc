/**
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
 */

package org.dkpro.tc.ml.svmhmm.task;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatTestTask;
import org.dkpro.tc.ml.svmhmm.core.SvmHmm;
import org.dkpro.tc.ml.svmhmm.core.SvmHmmPredictor;
import org.dkpro.tc.ml.svmhmm.core.SvmHmmTrainer;
public class SvmHmmTestTask
    extends LibsvmDataFormatTestTask
    implements Constants
{

    protected void combinePredictionAndExpectedGoldLabels(List<String> dataWithGoldLabel,
            List<String> predictions, File predictionOutFile)
        throws Exception
    {

        if (dataWithGoldLabel.size() != predictions.size()) {
            throw new IllegalStateException("Number of predictions is wrong. Expected ["
                    + dataWithGoldLabel.size() + "] but got [" + predictions.size() + "]");
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(predictionOutFile), UTF_8))) {
            merge(dataWithGoldLabel, predictions, writer);
        }

    }

    protected void merge(List<String> dataWithGoldLabel, List<String> predictions,
            BufferedWriter writer)
        throws IOException
    {
        writer.write("#PREDICTION;GOLD" + "\n");
        for (int i = 0; i < dataWithGoldLabel.size(); i++) {
            String p = predictions.get(i);
            String g = dataWithGoldLabel.get(i).split("\t")[0];

            writer.write(p + ";" + g + "\n");
        }
    }

    @Override
    protected Object trainModel(TaskContext aContext) throws Exception
    {

        File fileTrain = getTrainFile(aContext);

        // SvmHmm struggles with paths longer than 255 characters to circumvent this
        // issue, we copy all files together into a local directory to ensure short path
        // names that are below this threshold
        File parent = new SvmHmm().getTrainExecutable().getParentFile();
        File newTrainFileLocation = new File(parent, fileTrain.getName());
        File tmpModelLocation = new File(parent, "model.tmp");
        tmpModelLocation.deleteOnExit();
        
        FileUtils.copyFile(fileTrain, newTrainFileLocation);

        SvmHmmTrainer trainer = new SvmHmmTrainer();
        trainer.train(newTrainFileLocation, tmpModelLocation, getParameters());

        File modelFile = new File(aContext.getFolder("", AccessMode.READWRITE),
                MODEL_CLASSIFIER);
        FileUtils.copyFile(tmpModelLocation, modelFile);

        FileUtils.deleteQuietly(newTrainFileLocation);
        FileUtils.deleteQuietly(tmpModelLocation);

        return modelFile;
    }

    protected List<String> getParameters()
    {
        List<String> stringArgs = new ArrayList<>();
        for (int i = 1; i < classificationArguments.size(); i++) {
            stringArgs.add((String) classificationArguments.get(i));
        }

        return stringArgs;
    }

    @Override
    protected void runPrediction(TaskContext aContext, Object model) throws Exception
    {

        File fileTest = getTestFile(aContext);
        File modelFile = (File) model;

        // SvmHmm struggles with paths longer than 255 characters to circumvent this
        // issue, we copy all files together into a local directory to ensure short path
        // names that are below this threshold
        File parent = new SvmHmm().getPredictionExecutable().getParentFile();
        File localModel = new File(parent, "model.tmp");
        FileUtils.copyFile(modelFile, localModel);
        File localTestFile = new File(parent, "testfile.txt");
        FileUtils.copyFile(fileTest, localTestFile);

        SvmHmmPredictor predictor = new SvmHmmPredictor();
        List<String> predictions = predictor.predict(localTestFile, localModel);

        FileUtils.deleteQuietly(localModel);
        FileUtils.deleteQuietly(localTestFile);

        File predictionOutFile = new File(aContext.getFolder("", AccessMode.READWRITE),
                FILENAME_PREDICTIONS);
        combinePredictionAndExpectedGoldLabels(FileUtils.readLines(fileTest, UTF_8), predictions,
                predictionOutFile);
    }

}
