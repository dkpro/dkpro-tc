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
package de.tudarmstadt.ukp.dkpro.tc.core.ml;

import java.util.Collection;

import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.DimensionBundle;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;

/**
 * Interface for machine learning frameworks in TC
 */
public interface TCMachineLearningAdapter
{

    public enum AdapterNameEntries
    {
        /**
         * Intended for storing feature vectors (both in training and testing phases)
         */
        featureVectorsFile,

        /**
         * Intended for actual predictions on test data, i.e. label sequence
         */
        predictionsFile,

        /**
         * Intended for storing evaluation of the task
         */
        evaluationFile,

        featureSelectionFile
    }

    /**
     * @return The task that reads the ML feature store format, trains the classifier and stores the test results.
     */
    public ExecutableTaskBase getTestTask();

    /**
     * This report is always added to {@code testTask} reports by default in
     * {@linkplain de.tudarmstadt.ukp.dkpro.tc.ml.task.CrossValidationExperiment}
     * and {@link de.tudarmstadt.ukp.dkpro.tc.ml.task.TrainTestExperiment}.
     *
     * @return The report that computes the classification results.
     */
    public Class<? extends ReportBase> getClassificationReportClass();

	/**
	 * @param learningMode which learning mode (single/multi-label or regression) should be used
     * @return The data writer class that needs to be used with the corresponding ML framework
	 */
	public Class<? extends DataWriter> getDataWriterClass(String learningMode);

	/**
     * @return The class of the save model connector to be used with this ML framework
   	 */
	public Class<? extends SaveModelConnector_ImplBase> getSaveModelConnectorClass();

	
    /**
     * This report is always added to {@code testTask} reports by default in
     * {@linkplain de.tudarmstadt.ukp.dkpro.tc.ml.task.CrossValidationExperiment}
     * and {@link de.tudarmstadt.ukp.dkpro.tc.ml.task.TrainTestExperiment}.
     *
     * @return The report that collects the outcomeId to prediction values.
     */
    public Class<? extends ReportBase> getOutcomeIdReportClass();

    /**
     * This report is always added to {@code crossValidationTask} reports
     * by default in {@linkplain de.tudarmstadt.ukp.dkpro.tc.ml.task.CrossValidationExperiment}.
     *
     * @return The report that collects the results from the different folds.
     */
    public Class<? extends ReportBase> getBatchTrainTestReportClass();

    /**
     * @return The fold dimension bundle for CV
     */
    public <T extends DimensionBundle<Collection<String>>> T getFoldDimensionBundle(String[] files,
            int folds);

    /**
     * @param name Which name should be returned
     * @return Returns the filename that is used for specific files by the framework
     */
    public String getFrameworkFilename(AdapterNameEntries name);
}