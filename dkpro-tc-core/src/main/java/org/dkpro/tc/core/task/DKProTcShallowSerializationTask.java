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
package org.dkpro.tc.core.task;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.DefaultBatchTask;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;

public class DKProTcShallowSerializationTask
    extends DefaultBatchTask
    implements Constants
{

    @Discriminator(name = DIM_CLASSIFICATION_ARGS)
    protected List<Object> classArgs;

    private ExtractFeaturesTask featuresTrainTask;

    private OutcomeCollectionTask collectionTask;

    private MetaInfoTask metaInfoTask;

    private File outputFolder;

    private String experimentName;

    public DKProTcShallowSerializationTask(MetaInfoTask metaInfoTask,
            ExtractFeaturesTask featuresTrainTask, OutcomeCollectionTask collectionTask,
            File outputFolder, String experimentName)
    {
        this.metaInfoTask = metaInfoTask;
        this.featuresTrainTask = featuresTrainTask;
        this.collectionTask = collectionTask;
        this.outputFolder = outputFolder;
        this.experimentName = experimentName;
    }

    @Override
    public void initialize(TaskContext aContext)
    {

        super.initialize(aContext);

        TcShallowLearningAdapter adapter = (TcShallowLearningAdapter) classArgs.get(0);
        ModelSerializationTask serializationTask = adapter.getSaveModelTask();

        serializationTask.addImport(metaInfoTask, MetaInfoTask.META_KEY);
        serializationTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
                TEST_TASK_INPUT_KEY_TRAINING_DATA);
        serializationTask.addImport(collectionTask, OutcomeCollectionTask.OUTPUT_KEY,
                OUTCOMES_INPUT_KEY);
        serializationTask.setOutputFolder(outputFolder);
        serializationTask.setAttribute(TC_TASK_TYPE, TcTaskType.SERIALIZATION_TASK.toString());
        serializationTask.setType(serializationTask.getType() + "-" + experimentName);
        
        this.tasks = new HashSet<>();
        addTask(serializationTask);

    }
    
    @Override
    public boolean isInitialized()
    {
        // This is a hack - the facade-task has to re-initialize at every
        // execution to load the <i>current</i> machine learning adapter from
        // the classification arguments
        return false;
    }
}
