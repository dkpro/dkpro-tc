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
package org.dkpro.tc.ml.experiment.builder;

import org.dkpro.tc.core.Constants;

public enum ExperimentType implements Constants {
	/**
	 * Runs a train test experiment in which a single train set is evaluated against
	 * a single test set.
	 */
	TRAIN_TEST,

	/**
	 * Runs a cross-validation experiment in which the training data is split into N
	 * folds. Each fold will be in the test set once while the remaining N-1 folds
	 * will be in the training set.
	 */
	CROSS_VALIDATION,

	/**
	 * Runs a learning curve experiment that splits the data into N folds. Each fold
	 * will be in the test set and all fold combinations will be used as training
	 * set from which averaged performance results are computed. If you have a fixed
	 * test set against which a learning curve shall be run, then use
	 * {@link ExperimentType#LEARNING_CURVE_FIXED_TEST_SET}
	 */
	LEARNING_CURVE,

	/**
	 * Runs a model training and persists the model to an external file for later usage
	 */
	SAVE_MODEL,

	/**
	 * Runs a learning curve experiment, which uses a fixed test set. The training
	 * data is split into N folds and all fold-variations will be used to test
	 * against the fixed test set.This experiment type should be used if testing
	 * against a fixed development or test set. If no fixed test set is required use
	 * {@link ExperimentType#LEARNING_CURVE} instead.
	 */
	LEARNING_CURVE_FIXED_TEST_SET
}
