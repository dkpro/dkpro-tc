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
package org.dkpro.tc.ml.experiment;

import java.io.File;
import java.util.List;

import org.dkpro.lab.reporting.Report;
import org.dkpro.lab.task.impl.TaskBase;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.task.DKProTcShallowSerializationTask;
import org.dkpro.tc.core.task.ExtractFeaturesTask;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.task.MetaInfoTask;
import org.dkpro.tc.core.task.OutcomeCollectionTask;
import org.dkpro.tc.core.task.TcTaskType;
import org.dkpro.tc.ml.base.Experiment_ImplBase;

/**
 * Save model batch
 * 
 */
public class ExperimentSaveModel
    extends Experiment_ImplBase
{
    private File outputFolder;

    // tasks
    private InitTask initTask;
    private OutcomeCollectionTask collectionTask;
    private MetaInfoTask metaTask;
    private ExtractFeaturesTask featuresTrainTask;
    private TaskBase saveModelTask;

    public ExperimentSaveModel()
    {/* needed for Groovy */
    }

    public ExperimentSaveModel(String aExperimentName, File outputFolder)
        throws TextClassificationException
    {
        setExperimentName(aExperimentName);
        // set name of overall batch task
        setType("Evaluation-" + experimentName);
        setOutputFolder(outputFolder);
    }

    @Override
    protected void init()
    {
        if (experimentName == null) {
            throw new IllegalStateException("You must set an experiment name");
        }

        // init the train part of the experiment
        initTask = new InitTask();
        initTask.setPreprocessing(getPreprocessing());
        initTask.setOperativeViews(operativeViews);
        initTask.setTesting(false);
        initTask.setType(initTask.getType() + "-Train-" + experimentName);
        initTask.setAttribute(TC_TASK_TYPE, TcTaskType.INIT_TRAIN.toString());

        collectionTask = new OutcomeCollectionTask();
        collectionTask.setType(collectionTask.getType() + "-" + experimentName);
        collectionTask.setAttribute(TC_TASK_TYPE, TcTaskType.COLLECTION.toString());
        collectionTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN);

        metaTask = new MetaInfoTask();
        metaTask.setOperativeViews(operativeViews);
        metaTask.setType(metaTask.getType() + "-" + experimentName);
        metaTask.setAttribute(TC_TASK_TYPE, TcTaskType.META.toString());

        metaTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN, MetaInfoTask.INPUT_KEY);

        // feature extraction on training data
        featuresTrainTask = new ExtractFeaturesTask();
        featuresTrainTask.setType(featuresTrainTask.getType() + "-Train-" + experimentName);
        featuresTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);
        featuresTrainTask.addImport(initTask, InitTask.OUTPUT_KEY_TRAIN,
                ExtractFeaturesTask.INPUT_KEY);
        featuresTrainTask.setAttribute(TC_TASK_TYPE,
                TcTaskType.FEATURE_EXTRACTION_TRAIN.toString());
        featuresTrainTask.addImport(collectionTask, OutcomeCollectionTask.OUTPUT_KEY,
                ExtractFeaturesTask.COLLECTION_INPUT_KEY);

        saveModelTask = new DKProTcShallowSerializationTask(metaTask, featuresTrainTask,
                collectionTask, outputFolder, experimentName);
        saveModelTask.setType(saveModelTask.getType() + "-" + experimentName);
        saveModelTask.setAttribute(TC_TASK_TYPE, TcTaskType.FACADE_TASK.toString());
        
        if (innerReports != null) {
            for (Report report : innerReports) {
                saveModelTask.addReport(report);
            }
        }
        
        // DKPro Lab issue 38: must be added as *first* task
        addTask(initTask);
        addTask(collectionTask);
        addTask(metaTask);
        addTask(featuresTrainTask);
        addTask(saveModelTask);
    }

    public void setExperimentName(String experimentName)
    {
        this.experimentName = experimentName;
    }

    public void setOperativeViews(List<String> operativeViews)
    {
        this.operativeViews = operativeViews;
    }

    public void setOutputFolder(File outputFolder)
    {
        this.outputFolder = outputFolder;
    }
}
