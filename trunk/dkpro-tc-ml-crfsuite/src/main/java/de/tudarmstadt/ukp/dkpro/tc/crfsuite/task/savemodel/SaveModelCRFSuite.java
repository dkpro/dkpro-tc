/**
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.savemodel;

import java.io.File;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ValidityCheckTask;

/**
 * Save model batch
 * 
 */
public class SaveModelCRFSuite
    extends BatchTask
{

    private String experimentName;
    private AnalysisEngineDescription preprocessingPipeline;
    private List<String> operativeViews;
    private TCMachineLearningAdapter mlAdapter;
    private File outputFolder;

    // tasks
    private ValidityCheckTask checkTask;
    private PreprocessTask preprocessTaskTrain;
    private MetaInfoTask metaTask;
    private ExtractFeaturesTask featuresTrainTask;
    private CRFSuiteModelSerializationDescription saveModelTask;

    public SaveModelCRFSuite()
    {/* needed for Groovy */
    }

    public SaveModelCRFSuite(String aExperimentName, File outputFolder, Class<? extends TCMachineLearningAdapter> mlAdapter,
            AnalysisEngineDescription preprocessingPipeline)
            throws TextClassificationException
    {
        setExperimentName(aExperimentName);
        setPreprocessingPipeline(preprocessingPipeline);
        // set name of overall batch task
        setType("Evaluation-" + experimentName);
        setTcMachineLearningAdapter(mlAdapter);
        setOutputFolder(outputFolder);
    }

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        init();
        super.execute(aContext);
    }

    /**
     * Initializes the experiment. This is called automatically before execution. It's not done
     * directly in the constructor, because we want to be able to use setters instead of the
     * three-argument constructor.
     * 
     * @throws IllegalStateException
     *             if not all necessary arguments have been set.
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void init()
    {
        if (experimentName == null || preprocessingPipeline == null)

        {
            throw new IllegalStateException(
                    "You must set Experiment Name and Aggregate.");
        }

        // check the validity of the experiment setup first
        checkTask = new ValidityCheckTask();
        checkTask.setMlAdapter(mlAdapter);

        // preprocessing on training data
        preprocessTaskTrain = new PreprocessTask();
        preprocessTaskTrain.setPreprocessing(preprocessingPipeline);
        preprocessTaskTrain.setOperativeViews(operativeViews);
        preprocessTaskTrain.setTesting(false);
        preprocessTaskTrain.setType(preprocessTaskTrain.getType() + "-Train-" + experimentName);

        // get some meta data depending on the whole document collection that we need for training
        metaTask = new MetaInfoTask();
        metaTask.setOperativeViews(operativeViews);
        metaTask.setType(metaTask.getType() + "-" + experimentName);

        metaTask.addImport(preprocessTaskTrain, PreprocessTask.OUTPUT_KEY_TRAIN,
                MetaInfoTask.INPUT_KEY);

        // feature extraction on training data
        featuresTrainTask = new ExtractFeaturesTask();
        featuresTrainTask.setType(featuresTrainTask.getType() + "-Train-" + experimentName);
        featuresTrainTask.setMlAdapter(mlAdapter);
        featuresTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);
        featuresTrainTask.addImport(preprocessTaskTrain, PreprocessTask.OUTPUT_KEY_TRAIN,
                ExtractFeaturesTask.INPUT_KEY);

        // feature extraction and prediction on test data
        saveModelTask = new CRFSuiteModelSerializationDescription();
        saveModelTask.setType(saveModelTask.getType() + "-" + experimentName);
        saveModelTask.addImport(metaTask, MetaInfoTask.META_KEY);
        saveModelTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY, Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
        saveModelTask.setOutputFolder(outputFolder);

        // DKPro Lab issue 38: must be added as *first* task
        addTask(checkTask);
        addTask(preprocessTaskTrain);
        addTask(metaTask);
        addTask(featuresTrainTask);
        addTask(saveModelTask);
    }

    public void setExperimentName(String experimentName)
    {
        this.experimentName = experimentName;
    }

    public void setPreprocessingPipeline(AnalysisEngineDescription preprocessingPipeline)
    {
        this.preprocessingPipeline = preprocessingPipeline;
    }

    public void setOperativeViews(List<String> operativeViews)
    {
        this.operativeViews = operativeViews;
    }
    
    public void setTcMachineLearningAdapter(Class<? extends TCMachineLearningAdapter> mlAdapter) 
    	throws TextClassificationException
    {
        try {
			this.mlAdapter = mlAdapter.newInstance();
		} catch (InstantiationException e) {
			throw new TextClassificationException(e);
		} catch (IllegalAccessException e) {
			throw new TextClassificationException(e);
		}
    }
    
    
    public void setOutputFolder(File outputFolder) {
    	this.outputFolder = outputFolder;
    }
}