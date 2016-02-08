/**
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.serialization;

import java.io.File;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.ml.ExperimentTrainTest;

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