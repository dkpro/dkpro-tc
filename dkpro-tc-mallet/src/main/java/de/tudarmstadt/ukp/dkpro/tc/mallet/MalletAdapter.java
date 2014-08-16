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
package de.tudarmstadt.ukp.dkpro.tc.mallet;

import java.util.Collection;

import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.DimensionBundle;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.mallet.report.MalletBatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.mallet.report.MalletClassificationReport;
import de.tudarmstadt.ukp.dkpro.tc.mallet.report.MalletOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.mallet.task.MalletTestTask;
import de.tudarmstadt.ukp.dkpro.tc.mallet.util.MalletFoldDimensionBundle;
import de.tudarmstadt.ukp.dkpro.tc.ml.TCMachineLearningAdapter;

public class MalletAdapter 
	implements TCMachineLearningAdapter
{

	public static TCMachineLearningAdapter getInstance() {
		return new MalletAdapter();
	}
	
	@Override
	public ExecutableTaskBase getTestTask() {
		return new MalletTestTask();
	}

	@Override
	public Class<? extends ReportBase> getClassificationReportClass() {
		return MalletClassificationReport.class;
	}

	@Override
	public Class<? extends ReportBase> getOutcomeIdReportClass() {
		return MalletOutcomeIDReport.class;
	}

	@Override
	public Class<? extends ReportBase> getBatchTrainTestReportClass() {
		return MalletBatchTrainTestReport.class;
	}

	@Override
	public DimensionBundle<Collection<String>> getFoldDimensionBundle(
			String[] files, int folds) {
		return new MalletFoldDimensionBundle("files", Dimension.create("", files), folds);
	}
}
