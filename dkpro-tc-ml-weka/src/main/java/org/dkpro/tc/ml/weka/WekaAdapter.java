/**
 * Copyright 2019
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
package org.dkpro.tc.ml.weka;

import java.util.Collection;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.impl.DimensionBundle;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.lab.task.impl.FoldDimensionBundle;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.ml.weka.report.WekaBaselineMajorityClassIdReport;
import org.dkpro.tc.ml.weka.report.WekaBaselineRandomIdReport;
import org.dkpro.tc.ml.weka.report.WekaOutcomeIDReport;
import org.dkpro.tc.ml.weka.task.WekaTestTask;
import org.dkpro.tc.ml.weka.task.serialization.WekaLoadModelConnector;
import org.dkpro.tc.ml.weka.task.serialization.WekaSerliazeModelConnector;
import org.dkpro.tc.ml.weka.writer.WekaDataWriter;

public class WekaAdapter
    implements TcShallowLearningAdapter
{

    public static TcShallowLearningAdapter getInstance()
    {
        return new WekaAdapter();
    }

    @Override
    public ExecutableTaskBase getTestTask()
    {
        return new WekaTestTask();
    }

    @Override
    public Class<? extends ReportBase> getOutcomeIdReportClass()
    {
        return WekaOutcomeIDReport.class;
    }

    @Override
    public Class<? extends ReportBase> getMajorityClassBaselineIdReportClass()
    {
        return WekaBaselineMajorityClassIdReport.class;
    }

    @Override
    public Class<? extends ReportBase> getRandomBaselineIdReportClass()
    {
        return WekaBaselineRandomIdReport.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DimensionBundle<Collection<String>> getFoldDimensionBundle(String[] files, int folds)
    {
        return new FoldDimensionBundle<String>("files", Dimension.create("", files), folds);
    }

    @Override
    public String getDataWriterClass()
    {
        return WekaDataWriter.class.getName();
    }

    @Override
    public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass()
    {
        return WekaLoadModelConnector.class;
    }

    @Override
    public ModelSerializationTask getSaveModelTask()
    {
        return new WekaSerliazeModelConnector();
    }

    @Override
    public boolean useSparseFeatures()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName();
    }
    
	@Override
	public String getName() {
		return "Weka";
	}
}
