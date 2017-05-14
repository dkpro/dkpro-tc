/*******************************************************************************
 * Copyright 2017
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
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
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
        trainAndStoreModel(aContext);

        writeModelConfiguration(aContext, LiblinearAdapter.class.getName());
    }

    private void trainAndStoreModel(TaskContext aContext)
        throws Exception
    {
        
        boolean isRegression = learningMode.equals(Constants.LM_REGRESSION);
        
        //create mapping and persist mapping
        File fileTrain = getTrainFile(aContext);
        Map<String, Integer> outcomeMapping = LiblinearUtils.createMapping(isRegression, fileTrain);
        File mappedTrainFile = LiblinearUtils.replaceOutcome(fileTrain, outcomeMapping);
        File mappingFile = new File(outputFolder, LiblinearAdapter.getOutcomeMappingFilename());
        FileUtils.writeStringToFile(mappingFile, LiblinearUtils.outcomeMap2String(outcomeMapping));

        File featureNameFile = new File(aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY),LiblinearAdapter.getFeatureNameMappingFilename()); 
        File featureOutFile = new File(outputFolder, LiblinearAdapter.getFeatureNameMappingFilename());
        FileUtils.copyFile(featureNameFile, featureOutFile);

        Problem train = Problem.readFromFile(mappedTrainFile, 1.0);

        SolverType solver = LiblinearUtils.getSolver(classificationArguments);
        double C = LiblinearUtils.getParameterC(classificationArguments);
        double eps = LiblinearUtils.getParameterEpsilon(classificationArguments);

        Linear.setDebugOutput(null);

        Parameter parameter = new Parameter(solver, C, eps);
        Model model = Linear.train(train, parameter);
        model.save(new File(outputFolder, MODEL_CLASSIFIER));
    }
    
    private File getTrainFile(TaskContext aContext)
    {
        File trainFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        String trainFileName = LiblinearAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        File fileTrain = new File(trainFolder, trainFileName);

        return fileTrain;
    }

    public void trainModel(boolean b)
    {
        trainModel = b;
    }
}