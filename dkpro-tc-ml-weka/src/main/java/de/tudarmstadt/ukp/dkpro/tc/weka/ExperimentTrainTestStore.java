/**
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.tc.weka;

import java.io.File;
import java.util.logging.Logger;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.task.Task;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.ml.ExperimentTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.serialization.ModelSerializationTask;

/**
 * A subclass of the ExperiementTrainTest batch task, which
 * will store the trained classifier to the output directory
 * that is provided in the constructor or via setter.
 * 
 * @author Martin Wunderlich (martin@wunderlich.com)
 *
 */
public class ExperimentTrainTestStore extends ExperimentTrainTest {

	File outputDirectory = null;

	public ExperimentTrainTestStore() {
		/* needed for Groovy */
	}

	public ExperimentTrainTestStore(String aExperimentName, Class<? extends TCMachineLearningAdapter> mlAdapter,
            AnalysisEngineDescription preprocessing, File outputDirectory)
            throws TextClassificationException {

		super(aExperimentName, mlAdapter, preprocessing);

		this.outputDirectory = outputDirectory;
    }

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	/**
     * Initializes the experiment. This is called automatically before execution. It's not done
     * directly in the constructor, because we want to be able to use setters instead of the
     * four-argument constructor.
     *
     * @throws IllegalStateException
     *             if not all necessary arguments have been set.
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
	@Override
    protected void init() {
    	super.init();

    	if (outputDirectory == null)
            throw new IllegalStateException("You must set the outputdirectory.");

        ModelSerializationTask saveModelTask = new ModelSerializationTask();
    	String type = saveModelTask.getType() + "-" + experimentName;
    	saveModelTask.setType(type);
    	saveModelTask.setOutputFolder(outputDirectory.getAbsoluteFile());

    	saveModelTask.addImport(this.getMetaTask(), MetaInfoTask.META_KEY);
        saveModelTask.addImport(this.getFeatureExtractionTask(), ExtractFeaturesTask.OUTPUT_KEY, Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);

        this.addTask(saveModelTask);
    }

	@Override
	public void initialize(TaskContext aContext)
	{
        try {
            init();
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).severe("Error while trying to initialise: " + e);
        }
	}

	/**
	 * Private helper function that returns the ExtractFeaturesTask of
	 * the super class. Required, because all tasks are private in
	 * ExperimentTrainTest.
	 *
	 * @return The ExtractFeaturesTask
	 */
	private ExtractFeaturesTask getFeatureExtractionTask() {
		for(Task task : this.getTasks())
			if(task instanceof ExtractFeaturesTask)
                return (ExtractFeaturesTask) task;

		return null;
	}

	/**
	 * Private helper function that returns the MetaTask of
	 * the super class. Required, because all tasks are private in
	 * ExperimentTrainTest.
	 *
	 * @return The MetaTask
	 */
	private MetaInfoTask getMetaTask() {
		for(Task task : this.getTasks())
			if(task instanceof MetaInfoTask)
                return (MetaInfoTask) task;

		return null;
	}
}
