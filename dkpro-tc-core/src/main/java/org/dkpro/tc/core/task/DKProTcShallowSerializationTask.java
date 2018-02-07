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
import java.util.List;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.DefaultBatchTask;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;

public class DKProTcShallowSerializationTask extends DefaultBatchTask implements Constants {

	@Discriminator(name = DIM_CLASSIFICATION_ARGS)
	protected List<Object> classArgs;

	private ExtractFeaturesTask featuresTrainTask;

	private OutcomeCollectionTask collectionTask;

	private MetaInfoTask metaInfoTask;

	private File outputFolder;

	public DKProTcShallowSerializationTask(MetaInfoTask metaInfoTask, ExtractFeaturesTask featuresTrainTask, 
			OutcomeCollectionTask collectionTask, File outputFolder) {
				this.metaInfoTask = metaInfoTask;
				this.featuresTrainTask = featuresTrainTask;
				this.collectionTask = collectionTask;
				this.outputFolder = outputFolder;
	}

	@Override
	public void initialize(TaskContext aContext){
		
		super.initialize(aContext);
		
		TcShallowLearningAdapter adapter = (TcShallowLearningAdapter) classArgs.get(0);
		ModelSerializationTask serializationTask;
		try {
			serializationTask = adapter.getSaveModelTask().newInstance();
		} catch (Exception e) {
			throw new UnsupportedOperationException("Error when instantiating model serialization task");
		}
		
		serializationTask.addImport(metaInfoTask, MetaInfoTask.META_KEY);
		serializationTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
				Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
		serializationTask.addImport(collectionTask, OutcomeCollectionTask.OUTPUT_KEY, Constants.OUTCOMES_INPUT_KEY);
		serializationTask.setOutputFolder(outputFolder);
		
		String[] split = getType().split("-");
		serializationTask.setType(serializationTask.getClass().getName() + "-" + split[1]);
		this.addTask(serializationTask);
		
	} 
}
