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
package org.dkpro.tc.ml.libsvm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatTestTask;
import org.dkpro.tc.ml.libsvm.core.LibsvmPrediction;
import org.dkpro.tc.ml.libsvm.core.LibsvmTrain;

public class LibsvmTestTask
    extends LibsvmDataFormatTestTask
    implements Constants
{

    private String[] buildParameters()
    {
        List<String> parameters = new ArrayList<>();
        if (classificationArguments != null) {
            for (int i = 1; i < classificationArguments.size(); i++) {
                String a = (String) classificationArguments.get(i);
                parameters.add(a);
            }
        }
        return parameters.toArray(new String[0]);
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


    @Override
    protected Object trainModel(TaskContext aContext) throws Exception
    {
        File fileTrain = getTrainFile(aContext);
        File model = new File(aContext.getFolder("", AccessMode.READWRITE),
                Constants.MODEL_CLASSIFIER);
        
        LibsvmTrain trainer = new LibsvmTrain();
        trainer.train(fileTrain, model, buildParameters());

        return model;
    }

    @Override
    protected void runPrediction(TaskContext aContext, Object model) throws Exception
    {
        File theModel = (File) model;
        File fileTest = getTestFile(aContext);
        
        LibsvmPrediction predicter = new LibsvmPrediction();
        File prediction = predicter.prediction(fileTest, theModel);
        mergePredictionWithGold(aContext, prediction);
        
    }

    private void mergePredictionWithGold(TaskContext aContext, File predFile) throws Exception
    {

        File fileTest = getTestFile(aContext);
        File prediction = getPredictionFile(aContext);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(prediction), "utf-8"));

            List<String> gold = pickGold(FileUtils.readLines(fileTest, "utf-8"));
            List<String> pred = FileUtils.readLines(predFile, "utf-8");
            bw.write("#PREDICTION;GOLD" + "\n");
            for (int i = 0; i < gold.size(); i++) {
                String p = pred.get(i);
                String g = gold.get(i);
                bw.write(p + ";" + g);
                bw.write("\n");
            }
        }
        finally {
            IOUtils.closeQuietly(bw);
        }
    }

}