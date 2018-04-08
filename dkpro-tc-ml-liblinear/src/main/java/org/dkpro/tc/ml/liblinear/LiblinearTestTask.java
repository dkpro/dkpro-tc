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
package org.dkpro.tc.ml.liblinear;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatTestTask;
import org.dkpro.tc.ml.liblinear.core.LiblinearPredictor;
import org.dkpro.tc.ml.liblinear.core.LiblinearTrainer;
import org.dkpro.tc.ml.liblinear.util.LiblinearUtils;

import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.SolverType;

public class LiblinearTestTask
    extends LibsvmDataFormatTestTask
    implements Constants
{

    @Override
    protected Object trainModel(TaskContext aContext) throws Exception
    {

        File fileTrain = getTrainFile(aContext);

        SolverType solver = LiblinearUtils.getSolver(classificationArguments);
        double C = LiblinearUtils.getParameterC(classificationArguments);
        double eps = LiblinearUtils.getParameterEpsilon(classificationArguments);

        File modelTarget = aContext.getFile(MODEL_CLASSIFIER, AccessMode.READWRITE);

        LiblinearTrainer trainer = new LiblinearTrainer();
        trainer.train(solver, C, eps, fileTrain, modelTarget);

        return Linear.loadModel(modelTarget);
    }

    @Override
    protected void runPrediction(TaskContext aContext, Object trainedModel) throws Exception
    {

        Model model = (Model) trainedModel;

        File fileTest = getTestFile(aContext);

        LiblinearPredictor predicter = new LiblinearPredictor();
        List<String> predictions = predicter.predict(fileTest, model);

        File predFolder = aContext.getFolder("", AccessMode.READWRITE);
        File predictionsFile = new File(predFolder, Constants.FILENAME_PREDICTIONS);

        List<String> predWithGold = mergeWithGold(predictions, fileTest);
        writePredictions(predictionsFile, predWithGold, true);

    }

    private List<String> mergeWithGold(List<String> predictions, File fileTest) throws Exception
    {
        List<String> gold = new ArrayList<>();

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(fileTest), "utf-8"));

            String l = null;
            while ((l = reader.readLine()) != null) {
                gold.add(l.split("\t")[0]);
            }
        }
        finally {
            IOUtils.closeQuietly(reader);
        }

        List<String> merge = new ArrayList<>();
        for (int i = 0; i < predictions.size(); i++) {
            merge.add(predictions.get(i) + ";" + gold.get(i));
        }

        return merge;
    }

    public static void writePredictions(File predictionsFile, List<String> data,
            boolean writeHeader)
        throws Exception
    {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(predictionsFile), "utf-8"));
            if (writeHeader) {
                writer.append("#PREDICTION;GOLD" + "\n");
            }

            for (String s : data) {
                writer.write(s);
                writer.write("\n");
            }

        }
        finally {
            IOUtils.closeQuietly(writer);
        }
    }

}