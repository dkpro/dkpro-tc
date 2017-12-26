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
package org.dkpro.tc.core.ml;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.impl.TaskBase;

/**
 * Interface for machine learning frameworks in TC
 */
public interface TcDeepLearningAdapter
{
    public static final String PREPARATION_FOLDER = "preparationFolder";

    /*
     * Usually the init-test task to retrieve that document/instance N was document/instance
     * ABC.txt/walking
     */
    public static final String TARGET_ID_MAPPING = "targetIdFolder";

    public static final String EMBEDDING_FOLDER = "embeddingFolder";

    public static final String VECTORIZIATION_TRAIN_OUTPUT = "vectorizationTrainFolder";
    
	public static final String VECTORIZIATION_TEST_OUTPUT = "vectorizationTestFolder";

    /**
     * @return The task that reads the ML feature store format, trains the classifier and stores the
     *         test results.
     */
    public TaskBase getTestTask();

    /**
     * This report is always added to {@code testTask} reports by default in
     * ExperimentCrossValidation and ExperimentTrainTest.
     *
     * @return The report that collects the outcomeId to prediction values.
     */
    public ReportBase getOutcomeIdReportClass();

    /**
     * The lowest index from which transformation into integer values will start. Normally, this
     * value will be zero. Unless, zero values have a special meaning in the respective deep
     * learning framework. For instance, zero should not be used if vectors are zero-padded
     * otherwise distinction between padded-value and actual-value is no longer possible.
     * 
     * @return 
     * 		the lowest index that should be used to identify data values
     */
    public int lowestIndex();

    /**
     * An optional report which is intended to record the environment versions of non-Java
     * frameworks e.g. Keras version, Python version , etc.
     * 
     * @return
     * 		Meta collection report object for collecting relevant information of this adapter
     */
    public ReportBase getMetaCollectionReport();

}