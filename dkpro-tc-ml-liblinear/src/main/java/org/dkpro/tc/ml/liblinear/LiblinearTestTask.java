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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatTestTask;
import org.dkpro.tc.ml.liblinear.core.LiblinearPredictor;
import org.dkpro.tc.ml.liblinear.core.LiblinearTrainer;
import org.dkpro.tc.ml.liblinear.util.LiblinearUtils;

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
        Model model = trainer.train(solver, C, eps, fileTrain, modelTarget);
        return model;
    }

    @Override
    protected void runPrediction(TaskContext aContext, Object trainedModel) throws Exception
    {

        Model model = (Model) trainedModel;

        File fileTest = getTestFile(aContext);

        LiblinearPredictor predicter = new LiblinearPredictor();
        List<Double[]> predWithGold = predicter.predict(fileTest, model);

        File predFolder = aContext.getFolder("", AccessMode.READWRITE);
        File predictionsFile = new File(predFolder, Constants.FILENAME_PREDICTIONS);

        writePredictions(predictionsFile, predWithGold, true, true);

    }

    public static void writePredictions(File predictionsFile, List<Double[]> predictions,
            boolean writeHeader, boolean writeGold)
        throws Exception
    {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(predictionsFile), "utf-8"));
            if (writeHeader) {
                writer.append("#PREDICTION;GOLD" + "\n");
            }

            for (Double[] p : predictions) {
                writer.write(p[0].toString());
                if (writeGold) {
                    writer.write(";");
                    writer.write(p[1].toString());
                }

                writer.write("\n");
            }

        }
        finally {
            IOUtils.closeQuietly(writer);
        }
    }

}