/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.ml;

import java.util.Collection;

import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.DimensionBundle;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;

/**
 * Interface for machine learning frameworks in TC
 */
public interface TCMachineLearningAdapter {

	/**
	 * @return The task that reads the ML feature store format, trains the classifier and stores the test results.
	 */
	public ExecutableTaskBase getTestTask();
	
	/**
	 * @return The report that computes the classification results.
	 */
	public Class<? extends ReportBase> getClassificationReportClass();
	
	/**
	 * @return The report that collects the outcomeId to prediction values.
	 */
	public Class<? extends ReportBase> getOutcomeIdReportClass();
	
	/**
	 * @return The report that collects the results from the different folds.
	 */
	public Class<? extends ReportBase> getBatchTrainTestReportClass();
	
	/**
	 * @return The fold dimension bundle for CV
	 */
	public<T extends DimensionBundle<Collection<String>>> T getFoldDimensionBundle(String[] files, int folds);
}