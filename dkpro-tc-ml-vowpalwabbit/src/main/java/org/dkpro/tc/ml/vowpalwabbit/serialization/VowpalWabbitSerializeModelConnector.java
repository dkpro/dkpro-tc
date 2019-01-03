/*******************************************************************************
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
 ******************************************************************************/

package org.dkpro.tc.ml.vowpalwabbit.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.ml.vowpalwabbit.VowpalWabbitAdapter;
import org.dkpro.tc.ml.vowpalwabbit.VowpalWabbitTestTask;
import org.dkpro.tc.ml.vowpalwabbit.core.VowpalWabbit;
import org.dkpro.tc.ml.vowpalwabbit.core.VowpalWabbitTrainer;
import org.dkpro.tc.ml.vowpalwabbit.writer.VowpalWabbitDataWriter;

public class VowpalWabbitSerializeModelConnector
    extends ModelSerializationTask
    implements Constants
{

    @Discriminator(name = DIM_CLASSIFICATION_ARGS)
    protected List<Object> classificationArguments;

    protected boolean trainModel = true;

    @Override
    public void execute(TaskContext aContext) throws Exception
    {

        if (trainModel) {
            trainAndStoreModel(aContext);
        }
        else {
            copyAlreadyTrainedModel(aContext);
        }

        writeModelConfiguration(aContext);
    }

    @Override
    protected void writeModelConfiguration(TaskContext aContext) throws Exception
    {
        super.writeModelConfiguration(aContext);
        copyMapping(aContext, outputFolder, VowpalWabbitDataWriter.STRING_MAPPING);
        copyMapping(aContext, outputFolder, VowpalWabbitDataWriter.OUTCOME_MAPPING);
    }

    protected void copyMapping(TaskContext aContext, File outputFolder, String key) throws IOException
    {
        File folder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY);
        File file = new File(folder, key);
        FileUtils.copyFile(file, new File(outputFolder, file.getName()));
    }

    protected void copyAlreadyTrainedModel(TaskContext aContext) throws Exception
    {
        File file = aContext.getFile(MODEL_CLASSIFIER, AccessMode.READONLY);

        try (FileInputStream fis = new FileInputStream(file);
                FileOutputStream fos = new FileOutputStream(
                        new File(outputFolder, MODEL_CLASSIFIER));) {
            IOUtils.copy(fis, fos);
        }
    }

    protected void trainAndStoreModel(TaskContext aContext) throws Exception
    {
        File model = new File(outputFolder, MODEL_CLASSIFIER);

        File executable = new VowpalWabbit().getExecutable();
        File train = VowpalWabbitTestTask.loadAndPrepareFeatureDataFile(aContext,
                executable.getParentFile(), TEST_TASK_INPUT_KEY_TRAINING_DATA);

        VowpalWabbitTrainer trainer = new VowpalWabbitTrainer();

        List<String> parameters = getParameters(classificationArguments);
        parameters.remove(VowpalWabbitAdapter.class.getSimpleName());
        parameters = VowpalWabbitTestTask.automaticallyAddParametersForClassificationMode(aContext,
                parameters, learningMode, featureMode);
        trainer.train(train, model, parameters);
    }

    protected List<String> getParameters(List<Object> subList)
    {
        List<String> s = new ArrayList<>();

        for (Object o : subList) {
            s.add(o.toString());
        }

        return s;
    }

    public void trainModel(boolean b)
    {
        trainModel = b;
    }

    @Override
    protected void writeAdapter() throws Exception
    {
        writeModelAdapterInformation(outputFolder, VowpalWabbitAdapter.class.getName());
    }
}