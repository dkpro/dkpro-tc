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
package org.dkpro.tc.ml;

import org.dkpro.lab.reporting.Report;
import org.dkpro.lab.task.impl.TaskBase;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcDeepLearningAdapter;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.task.InitTaskDeep;
import org.dkpro.tc.core.task.deep.EmbeddingTask;
import org.dkpro.tc.core.task.deep.PreparationTask;
import org.dkpro.tc.core.task.deep.VectorizationTask;
import org.dkpro.tc.ml.base.DeepLearningExperiment_ImplBase;
import org.dkpro.tc.ml.report.DeeplearningBasicResultReport;
import org.dkpro.tc.ml.report.TcTaskType;

/**
 * Train-Test setup
 */
public class DeepLearningExperimentTrainTest extends DeepLearningExperimentTrainTestBase {

	protected TaskBase learningTask;

	public DeepLearningExperimentTrainTest() {/* needed for Groovy */
	}

	/**
	 * Preconfigured train-test setup.
	 */
	public DeepLearningExperimentTrainTest(String aExperimentName, Class<? extends TcDeepLearningAdapter> mlAdapter)
			throws TextClassificationException {
		super(aExperimentName, mlAdapter);
	}

	/**
	 * Initializes the experiment. This is called automatically before
	 * execution. It's not done directly in the constructor, because we want to
	 * be able to use setters instead of the arguments in the constructor.
	 * 
	 */
	@Override
	protected void init() {
		
		super.init();
		
		learningTask = mlAdapter.getTestTask();
		learningTask.setType(learningTask.getType() + "-" + experimentName);
		learningTask.setAttribute(TC_TASK_TYPE, TcTaskType.MACHINE_LEARNING_ADAPTER.toString());

		if (innerReports != null) {
			for (Class<? extends Report> report : innerReports) {
				learningTask.addReport(report);
			}
		}

		// // always add OutcomeIdReport
		learningTask.addReport(mlAdapter.getOutcomeIdReportClass());
		learningTask.addReport(mlAdapter.getMetaCollectionReport());
		learningTask.addReport(DeeplearningBasicResultReport.class);
		learningTask.addImport(preparationTask, PreparationTask.OUTPUT_KEY, TcDeepLearningAdapter.PREPARATION_FOLDER);
		learningTask.addImport(vectorizationTrainTask, VectorizationTask.OUTPUT_KEY,
				Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
		learningTask.addImport(vectorizationTestTask, VectorizationTask.OUTPUT_KEY,
				Constants.TEST_TASK_INPUT_KEY_TEST_DATA);
		learningTask.addImport(initTaskTest, InitTaskDeep.OUTPUT_KEY_TEST, TcDeepLearningAdapter.TARGET_ID_MAPPING);
		learningTask.addImport(embeddingTask, EmbeddingTask.OUTPUT_KEY, TcDeepLearningAdapter.EMBEDDING_FOLDER);

		addTask(learningTask);
	}
}
