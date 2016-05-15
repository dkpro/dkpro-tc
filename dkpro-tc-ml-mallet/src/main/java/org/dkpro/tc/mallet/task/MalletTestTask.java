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
package org.dkpro.tc.mallet.task;

import java.io.File;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.mallet.MalletAdapter;

public class MalletTestTask
    extends ExecutableTaskBase
    implements Constants
{
    public static final String MALLET_ALGO = "malletTrainingAlgo";
    @Discriminator(name = MALLET_ALGO)
    MalletAlgo malletAlgo;

    private double gaussianPriorVariance = 10.0; // Gaussian Prior Variance

    @Discriminator
    private int iterations = 100; // Number of iterations

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {

        File trainFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        String fileName = MalletAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        File fileTrain = new File(trainFolder, fileName);

        File testFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY);
        File fileTest = new File(testFolder, fileName);

        File fileModel = aContext.getFile(MODEL_CLASSIFIER, AccessMode.READWRITE);

        String pred = MalletAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.predictionsFile);
        File predictions = aContext.getFile(pred, AccessMode.READWRITE);

        String string = malletAlgo.toString();

        if (string.startsWith("CRF")) {
            ConditionalRandomFields crf = new ConditionalRandomFields(fileTrain, fileTest,
                    fileModel, predictions, gaussianPriorVariance, iterations, malletAlgo);
            crf.run();
        }
        else if (string.startsWith("HMM")) {
            HiddenMarkov hmm = new HiddenMarkov(fileTrain, fileTest, fileModel, predictions,
                    iterations);
            hmm.run();
        }
        else {
            throw new IllegalStateException("No algorithmen set");
        }
    }

}