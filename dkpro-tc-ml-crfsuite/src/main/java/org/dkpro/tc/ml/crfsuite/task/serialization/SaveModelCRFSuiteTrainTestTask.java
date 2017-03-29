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

package org.dkpro.tc.ml.crfsuite.task.serialization;

import java.io.File;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.task.ExtractFeaturesTask;
import org.dkpro.tc.core.task.MetaInfoTask;
import org.dkpro.tc.ml.ExperimentTrainTest;

/**
 * Train-Test setup
 * 
 */
public class SaveModelCRFSuiteTrainTestTask
    extends ExperimentTrainTest
{

    private CRFSuiteModelSerializationDescription saveModelTask;
    private File outputFolder;

    public SaveModelCRFSuiteTrainTestTask()
    {/* needed for Groovy */
    }

    /**
     * Preconfigured train-test setup that also stores the trained classifier.
     * 
     * @param aExperimentName
     *            name of the experiment
     */
    public SaveModelCRFSuiteTrainTestTask(String aExperimentName,  File outputFolder, Class<? extends TCMachineLearningAdapter> mlAdapter)
            throws TextClassificationException
    {
    	super(aExperimentName, mlAdapter);
        setOutputFolder(outputFolder);
    }

    /**
     * Initializes the experiment. This is called automatically before execution. It's not done
     * directly in the constructor, because we want to be able to use setters instead of the
     * arguments in the constructor.
     * 
     * @throws IllegalStateException
     *             if not all necessary arguments have been set.
     */
    protected void init()
    {
       super.init();
        
        // store the model
        saveModelTask = new CRFSuiteModelSerializationDescription();
        saveModelTask.setType(saveModelTask.getType() + "-" + experimentName);
        saveModelTask.addImport(metaTask, MetaInfoTask.META_KEY);
        saveModelTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
                Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
        saveModelTask.setOutputFolder(outputFolder);
        
        saveModelTask.addImport(testTask, Constants.MODEL_CLASSIFIER);
        saveModelTask.trainModel(false);

        addTask(saveModelTask);
    }
    
    public void setOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
    }
}