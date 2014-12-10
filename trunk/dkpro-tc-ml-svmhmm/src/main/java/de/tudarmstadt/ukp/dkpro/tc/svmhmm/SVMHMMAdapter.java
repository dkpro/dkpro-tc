/*
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
 */

package de.tudarmstadt.ukp.dkpro.tc.svmhmm;

import java.util.Collection;

import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.DimensionBundle;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.FoldDimensionBundle;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.report.SVMHMMBatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.report.SVMHMMClassificationReport;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.report.SVMHMMOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.task.SVMHMMTestTask;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.writer.SVMHMMDataWriter;

/**
 * @author Ivan Habernal
 */
public class SVMHMMAdapter
	implements TCMachineLearningAdapter
{

    @Override
    public ExecutableTaskBase getTestTask()
    {
        return new SVMHMMTestTask();
    }

    @Override
    public Class<? extends ReportBase> getClassificationReportClass()
    {
        return SVMHMMClassificationReport.class;
    }

    @Override
    public Class<? extends ReportBase> getOutcomeIdReportClass()
    {
        return SVMHMMOutcomeIDReport.class;
    }

    @Override
    public Class<? extends ReportBase> getBatchTrainTestReportClass()
    {
        return SVMHMMBatchCrossValidationReport.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DimensionBundle<Collection<String>> getFoldDimensionBundle(
            String[] files, int folds)
    {
        return new FoldDimensionBundle<>("files", Dimension.create("", files), folds);
    }

    @Override
    public String getFrameworkFilename(
            AdapterNameEntries adapterNameEntries)
    {

        switch (adapterNameEntries) {
        case featureVectorsFile:
            return "feature-vectors.txt";
        case predictionsFile:
            // this is where the predicted outcomes are written
            return "predicted-labels.txt";
        case evaluationFile:
            // This is where the final evaluation is usually written
            return "evaluation.txt";
        case featureSelectionFile:
            return "attributeEvaluationResults.txt";
        }
        return null;
    }

	@Override
	public Class<? extends DataWriter> getDataWriterClass() {
		return SVMHMMDataWriter.class;
	}

	@Override
	public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass() {
		// FIXME to be implemented
		throw new UnsupportedOperationException();
	}
}

