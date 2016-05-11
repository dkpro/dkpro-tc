/*******************************************************************************
 * Copyright 2015
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

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class LiblinearTestTask
    extends ExecutableTaskBase
    implements Constants
{
    @Discriminator(name = DIM_CLASSIFICATION_ARGS)
    private List<String> classificationArguments;
    @Discriminator(name = DIM_FEATURE_MODE)
    private String featureMode;
    @Discriminator(name = DIM_LEARNING_MODE)
    private String learningMode;

    public static String SEPARATOR_CHAR = ";";

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        if (multiLabel) {
            throw new TextClassificationException(
                    "Multi-label requested, but LIBLINEAR only supports single label setups.");
        }

        File trainFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        String trainFileName = LiblinearAdapter.getInstance().getFrameworkFilename(
                AdapterNameEntries.featureVectorsFile);
        File fileTrain = new File(trainFolder, trainFileName);

        File testFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY);
        String testFileName = LiblinearAdapter.getInstance().getFrameworkFilename(
                AdapterNameEntries.featureVectorsFile);
        File fileTest = new File(testFolder, testFileName);

        // default for bias is -1, documentation says to set it to 1 in order to get results closer
        // to libsvm
        // writer adds bias, so if we de-activate that here for some reason, we need to also
        // deactivate it there
        Problem train = Problem.readFromFile(fileTrain, 1.0);

        // FIXME this should be configurable over the classificationArgs
        SolverType solver = SolverType.L2R_LR; // -s 0
        double C = 1.0; // cost of constraints violation
        double eps = 0.01; // stopping criteria

        Linear.setDebugOutput(null);

        Parameter parameter = new Parameter(solver, C, eps);
        Model model = Linear.train(train, parameter);

        Problem test = Problem.readFromFile(fileTest, 1.0);

        predict(aContext, model, test);
    }

    private void predict(TaskContext aContext, Model model, Problem test)
        throws Exception
    {
        File predFolder = aContext.getFolder("", AccessMode.READWRITE);
        String predFileName = LiblinearAdapter.getInstance().getFrameworkFilename(
                AdapterNameEntries.predictionsFile);
        File predictionsFile = new File(predFolder, predFileName);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                predictionsFile), "utf-8"));
        writer.append("#PREDICTION;GOLD" + "\n");

        Feature[][] testInstances = test.x;
        for (int i = 0; i < testInstances.length; i++) {
            Feature[] instance = testInstances[i];
            Double prediction = Linear.predict(model, instance);

            writer.write(prediction.intValue() + SEPARATOR_CHAR + new Double(test.y[i]).intValue());
            writer.write("\n");
        }

        writer.close();
    }
}