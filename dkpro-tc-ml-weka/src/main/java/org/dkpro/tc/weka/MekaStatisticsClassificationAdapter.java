/**
 * Copyright 2016
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
package org.dkpro.tc.weka;

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
import org.dkpro.tc.ml.report.BatchStatisticsTrainTestReport;
import org.dkpro.tc.weka.report.WekaClassificationReport;
import org.dkpro.tc.weka.report.WekaOutcomeIDReport;
import org.dkpro.tc.weka.task.WekaTestTask;
import org.dkpro.tc.weka.task.serialization.LoadModelConnectorWeka;
import org.dkpro.tc.weka.task.serialization.WekaModelSerializationDescription;
import org.dkpro.tc.weka.writer.MekaDataWriter;

public class MekaStatisticsClassificationAdapter 
	implements TCMachineLearningAdapter
{

	public static TCMachineLearningAdapter getInstance() {
		return new MekaStatisticsClassificationAdapter();
	}
	
	@Override
	public ExecutableTaskBase getTestTask() {
		return new WekaTestTask();
	}

	@Override
	public Class<? extends ReportBase> getOutcomeIdReportClass() {
		return WekaOutcomeIDReport.class;
	}

	@Override
	public Class<? extends ReportBase> getBatchTrainTestReportClass() {
		return BatchStatisticsTrainTestReport.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DimensionBundle<Collection<String>> getFoldDimensionBundle(
			String[] files, int folds) {
		return  new FoldDimensionBundle<String>("files", Dimension.create("", files), folds);
	}

	@Override
	public String getFrameworkFilename(AdapterNameEntries name) {

        switch (name) {
            case featureVectorsFile:  return "training-data.arff.gz";
            case predictionsFile      :  return "predictions.arff";
            case featureSelectionFile :  return "attributeEvaluationResults.txt";
        }
        
        return null;
	}
	
	@Override
	public Class<? extends DataWriter> getDataWriterClass() {
		return MekaDataWriter.class;
	}

	@Override
	public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass() {
		return LoadModelConnectorWeka.class;
	}
	
	@Override
	public Class<? extends ModelSerializationTask> getSaveModelTask() {
	    return WekaModelSerializationDescription.class;
	}
}
