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

package org.dkpro.tc.crfsuite.task.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;

import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.crfsuite.task.CRFSuiteTestTask;

public class CRFSuiteModelSerializationDescription
    extends ModelSerializationTask
    implements Constants
{

    @Discriminator
    private String[] classificationArguments;

    boolean trainModel = true;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {

        if (trainModel) {
            trainAndStoreModel(aContext);
        }
        else {
            copyAlreadyTrainedModel(aContext);
        }

        writeModelConfiguration(aContext, CRFSuiteAdapter.class.getName());
    }

    private void copyAlreadyTrainedModel(TaskContext aContext)
        throws Exception
    {
        File file = aContext.getFile(MODEL_CLASSIFIER, AccessMode.READONLY);

        FileInputStream fis = new FileInputStream(file);
        FileOutputStream fos = new FileOutputStream(new File(outputFolder, MODEL_CLASSIFIER));
        IOUtils.copy(fis, fos);
    }

    private void trainAndStoreModel(TaskContext aContext)
        throws Exception
    {
        File trainFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        String trainFileName = CRFSuiteAdapter.getInstance().getFrameworkFilename(
                AdapterNameEntries.featureVectorsFile);

        String classifierPath = outputFolder.getAbsolutePath() + "/" + MODEL_CLASSIFIER;
        String trainingDataPath = trainFolder.getPath() + "/" + trainFileName;
        String classificationArgs = classificationArguments != null ? classificationArguments[0]
                : null;
        List<String> commandTrainModel = CRFSuiteTestTask.getTrainCommand(classifierPath,
                trainingDataPath, classificationArgs);

        Process process = new ProcessBuilder().inheritIO().command(commandTrainModel).start();
        process.waitFor();
    }

    public void trainModel(boolean b)
    {
        trainModel = b;
    }
}