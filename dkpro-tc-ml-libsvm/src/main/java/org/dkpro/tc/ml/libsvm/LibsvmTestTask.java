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
package org.dkpro.tc.ml.libsvm;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.libsvm.api.LibsvmTrainModel;

import libsvm.svm_model;
import libsvm.svm_parameter;
import libsvm.svm_problem;


public class LibsvmTestTask
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
    public static final double EPISILON_DEFAULT = 0.01;
    public static final double PARAM_C_DEFAULT = 1.0;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        if (multiLabel) {
            throw new TextClassificationException(
                    "Multi-label requested, but LIBLINEAR only supports single label setups.");
        }

        File fileTrain = getTrainFile(aContext);
        File fileTest = getTestFile(aContext);

        Map<String, Integer> outcomeMapping = LibsvmUtils.createMapping(fileTrain, fileTest);
        File idMappedTrainFile = LibsvmUtils.replaceOutcomeByIntegerValue(fileTrain, outcomeMapping);
        File idMappedTestFile = LibsvmUtils.replaceOutcomeByIntegerValue(fileTest, outcomeMapping);
        writeMapping(aContext, outcomeMapping);
        
        File model = new File(aContext.getFolder("", AccessMode.READWRITE), Constants.MODEL_CLASSIFIER);

        LibsvmTrainModel ltm = new LibsvmTrainModel();
        ltm.run(new String [] {fileTrain.getAbsolutePath(), model.getAbsolutePath()});
        
    }

    private void writeMapping(TaskContext aContext, Map<String, Integer> outcomeMapping) throws IOException
    {
        File mappingFile = new File(aContext.getFolder("", AccessMode.READWRITE), LibsvmAdapter.getOutcomeMappingFilename());
        FileUtils.writeStringToFile(mappingFile, LibsvmUtils.outcomeMap2String(outcomeMapping));        
    }
    

    private File getTestFile(TaskContext aContext)
    {
        File testFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY);
        String testFileName = LibsvmAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        File fileTest = new File(testFolder, testFileName);
        return fileTest;
    }

    private File getTrainFile(TaskContext aContext)
    {
        File trainFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        String trainFileName = LibsvmAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        File fileTrain = new File(trainFolder, trainFileName);

        return fileTrain;
    }

}