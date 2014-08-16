/**
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.weka;

import java.util.Collection;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.DimensionBundle;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.FoldDimensionBundle;
import de.tudarmstadt.ukp.dkpro.tc.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaClassificationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.WekaTestTask;

public class WekaAdapter 
	implements TCMachineLearningAdapter
{

	public static TCMachineLearningAdapter getInstance() {
		return new WekaAdapter();
	}
	
	@Override
	public ExecutableTaskBase getTestTask() {
		return new WekaTestTask();
	}

	@Override
	public Class<? extends ReportBase> getClassificationReportClass() {
		return WekaClassificationReport.class;
	}

	@Override
	public Class<? extends ReportBase> getOutcomeIdReportClass() {
		return WekaOutcomeIDReport.class;
	}

	@Override
	public Class<? extends ReportBase> getBatchTrainTestReportClass() {
		return WekaBatchTrainTestReport.class;
	}

	@Override
	public DimensionBundle<Collection<String>> getFoldDimensionBundle(
			String[] files, int folds) {
		return  new FoldDimensionBundle<String>("files", Dimension.create("", files), folds);
	}
}
