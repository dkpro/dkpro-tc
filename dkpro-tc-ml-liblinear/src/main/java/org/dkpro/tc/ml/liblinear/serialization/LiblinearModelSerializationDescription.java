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

package org.dkpro.tc.ml.liblinear.serialization;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.liblinear.LiblinearDataWriter;
import org.dkpro.tc.ml.liblinear.util.LiblinearUtils;

import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class LiblinearModelSerializationDescription
    extends ModelSerializationTask
    implements Constants
{

    @Discriminator(name = DIM_CLASSIFICATION_ARGS)
    private List<String> classificationArguments;

    boolean trainModel = true;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {

        // if (trainModel) {
        trainAndStoreModel(aContext);
        // }
        // else {
        // copyAlreadyTrainedModel(aContext);
        // }

        writeModelConfiguration(aContext, LiblinearAdapter.class.getName());
    }

    // private void copyAlreadyTrainedModel(TaskContext aContext)
    // throws Exception
    // {
    // File file = aContext.getFile(MODEL_CLASSIFIER, AccessMode.READONLY);
    //
    // FileInputStream fis = new FileInputStream(file);
    // FileOutputStream fos = new FileOutputStream(new File(outputFolder, MODEL_CLASSIFIER));
    // IOUtils.copy(fis, fos);
    // }

    private void trainAndStoreModel(TaskContext aContext)
        throws Exception
    {
        File trainFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        String trainFileName = LiblinearAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        File fileTrain = new File(trainFolder, trainFileName);

        Problem train = Problem.readFromFile(fileTrain, 1.0);

        SolverType solver = LiblinearUtils.getSolver(classificationArguments);
        double C = LiblinearUtils.getParameterC(classificationArguments);
        double eps = LiblinearUtils.getParameterEpsilon(classificationArguments);

        Linear.setDebugOutput(null);

        Parameter parameter = new Parameter(solver, C, eps);
        Model model = Linear.train(train, parameter);
        model.save(new File(outputFolder, MODEL_CLASSIFIER));

        File mappingFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY);
        File mappingFile = new File(mappingFolder,LiblinearAdapter.getOutcomeMappingFilename());
        FileUtils.copyFile(mappingFile,
                new File(outputFolder, LiblinearAdapter.getOutcomeMappingFilename()));
    }

    public void trainModel(boolean b)
    {
        trainModel = b;
    }
}