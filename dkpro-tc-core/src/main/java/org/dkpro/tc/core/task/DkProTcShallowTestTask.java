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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.engine.LifeCycleException;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.engine.TaskExecutionEngine;
import org.dkpro.lab.engine.TaskExecutionService;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.ExecutableTask;
import org.dkpro.lab.task.impl.DefaultBatchTask;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.core.util.TaskUtils;

public class DkProTcShallowTestTask extends DefaultBatchTask implements Constants {

	@Discriminator(name = DIM_CLASSIFICATION_ARGS)
	protected List<Object> classArgs;

	private ExtractFeaturesTask featuresTrainTask;

	private ExtractFeaturesTask featuresTestTask;

	private OutcomeCollectionTask collectionTask;

	private List<ReportBase> reports;

	public DkProTcShallowTestTask(ExtractFeaturesTask featuresTrainTask, ExtractFeaturesTask featuresTestTask,
			OutcomeCollectionTask collectionTask, List<ReportBase> reports) {
				this.featuresTrainTask = featuresTrainTask;
				this.featuresTestTask = featuresTestTask;
				this.collectionTask = collectionTask;
				this.reports = reports;
	}

	@Override
	public void initialize(TaskContext aContext){
		
		super.initialize(aContext);
		
		TcShallowLearningAdapter adapter = (TcShallowLearningAdapter) classArgs.get(0);
		ExecutableTaskBase testTask = adapter.getTestTask();
		
		testTask.addReport(adapter.getOutcomeIdReportClass());
		for(ReportBase b : reports){
			testTask.addReport(b);
		}
		
		testTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
				Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
		testTask.addImport(featuresTestTask, ExtractFeaturesTask.OUTPUT_KEY, Constants.TEST_TASK_INPUT_KEY_TEST_DATA);
		testTask.addImport(collectionTask, OutcomeCollectionTask.OUTPUT_KEY, Constants.OUTCOMES_INPUT_KEY);
		testTask.setAttribute(TC_TASK_TYPE, TcTaskType.MACHINE_LEARNING_ADAPTER.toString());
		
		String[] split = getType().split("-");
		testTask.setType(testTask.getClass().getName() + "-" + split[1]);
		this.addTask(testTask);
		
	}

//	@Override
//	public void execute(TaskContext aContext) throws Exception {
//
//		TcShallowLearningAdapter adapter = (TcShallowLearningAdapter) classArgs.get(0);
//		ExecutableTaskBase testTask = adapter.getTestTask();
//		
////		this.addTask(aTask);
//		
//		testTask.initialize(aContext);
//
//		configureLabTask(aContext, testTask);
//
//		testTask.addReport(adapter.getOutcomeIdReportClass());
//		for(ReportBase b : reports){
//			testTask.addReport(b);
//		}
//
//		String[] split = getType().split("-");
//		testTask.setType(testTask.getClass().getName() + "-" + split[1]);
//		
//		TaskExecutionService execService = aContext.getExecutionService();
//		TaskExecutionEngine engine = execService.createEngine(testTask);
//		String run = engine.run(testTask);
//		
//		testTask.markExecuted();
//		
//		
//		
////		announceNewlyCreatedTask(run);
//		
////		aContext.getStorageService().
//		
//	}

	private void configureLabTask(TaskContext aContext, ExecutableTaskBase testTask) throws LifeCycleException {
		
		testTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
				Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
		testTask.addImport(featuresTestTask, ExtractFeaturesTask.OUTPUT_KEY, Constants.TEST_TASK_INPUT_KEY_TEST_DATA);
		testTask.addImport(collectionTask, OutcomeCollectionTask.OUTPUT_KEY, Constants.OUTCOMES_INPUT_KEY);
		
		Map<String, Object> config = new HashMap<>();
		Map<String, String> resolvedDescriminators = getResolvedDescriminators(aContext);
		for (String key : resolvedDescriminators.keySet()) {
			String actualKey = key;
			int lastIndexOf = key.lastIndexOf("|");
			if (lastIndexOf > -1) {
				key = key.substring(lastIndexOf + 1);
			}
			config.put(key, resolvedDescriminators.get(actualKey));
		}
		aContext.getLifeCycleManager().configure(aContext, testTask, config);
		testTask.setAttribute(TC_TASK_TYPE, TcTaskType.MACHINE_LEARNING_ADAPTER.toString());
		
	}

	/**
	 * This task is just a proxy for a machine learning adapter test-task. This
	 * method returns the name of the adapter that is actually executed
	 * 
	 * @return string value with the name of the test task
	 * @throws ResourceInitializationException
	 *             in case of an exception
	 */
	public String getTrueName() {
		TcShallowLearningAdapter adapter;
		try {
			adapter = TaskUtils.getAdapter(classArgs);
		} catch (ResourceInitializationException e) {
			throw new UnsupportedOperationException(e);
		}
		return adapter.getTestTask().getClass().getName();
	}

}
