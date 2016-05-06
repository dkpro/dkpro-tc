/*
 * Copyright 2015
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

package org.dkpro.tc.svmhmm;

import java.util.Collection;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.impl.DimensionBundle;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.lab.task.impl.FoldDimensionBundle;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.ml.report.InnerBatchUsingTCEvaluationReport;
import org.dkpro.tc.svmhmm.report.SVMHMMClassificationReport;
import org.dkpro.tc.svmhmm.report.SVMHMMOutcomeIDReport;
import org.dkpro.tc.svmhmm.task.SVMHMMTestTask;
import org.dkpro.tc.svmhmm.task.serialization.LoadModelConnectorSvmhmm;
import org.dkpro.tc.svmhmm.task.serialization.SvmhmmModelSerializationDescription;
import org.dkpro.tc.svmhmm.writer.SVMHMMDataWriter;

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
        return InnerBatchUsingTCEvaluationReport.class;
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
		return LoadModelConnectorSvmhmm.class;
	}

	@Override
	public Class<? extends ModelSerializationTask> getSaveModelTask() {
		return SvmhmmModelSerializationDescription.class;
	}
}

