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
package org.dkpro.tc.ml.xgboost;

import java.util.Collection;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.impl.DimensionBundle;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.lab.task.impl.FoldDimensionBundle;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatWriter;
import org.dkpro.tc.io.libsvm.reports.LibsvmDataFormatBaselineMajorityClassIdReport;
import org.dkpro.tc.io.libsvm.reports.LibsvmDataFormatOutcomeIdReport;
import org.dkpro.tc.io.libsvm.reports.LibsvmDataFormatBaselineRandomIdReport;

public class XgboostAdapter
    implements TcShallowLearningAdapter
{

    public static TcShallowLearningAdapter getInstance() {
        return new XgboostAdapter();
    }

    public static String getOutcomeMappingFilename() {
        return "outcome-mapping.txt";
    }
    
	public static String getFeatureNameMappingFilename() {
		return "feature-name-mapping.txt";
	}

    public static String getFeatureNames() {
        return "featurenames.txt";
    }

    @Override
    public ExecutableTaskBase getTestTask() {
        return new XgboostTestTask();
    }

    @Override
    public Class<? extends ReportBase> getOutcomeIdReportClass() {
        return LibsvmDataFormatOutcomeIdReport.class;
    }
    
    @Override
    public Class<? extends ReportBase> getMajorityClassBaselineIdReportClass() {
        return LibsvmDataFormatBaselineMajorityClassIdReport.class;
    }
    
    @Override
    public Class<? extends ReportBase> getRandomBaselineIdReportClass() {
        return LibsvmDataFormatBaselineRandomIdReport.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DimensionBundle<Collection<String>> getFoldDimensionBundle(String[] files, int folds) {
        return new FoldDimensionBundle<String>("files", Dimension.create("", files), folds);
    }

    @Override
    public Class<? extends DataWriter> getDataWriterClass() {
        return LibsvmDataFormatWriter.class;
    }

    @Override
    public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass() {
        return null;
    }

    @Override
    public Class<? extends ModelSerializationTask> getSaveModelTask() {
    		return null;
    }
    
    @Override
	public boolean useSparseFeatures() {
		return true;
	}
    
    @Override
    public String toString() {
    		return getClass().getSimpleName();
    }
}

