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
package org.dkpro.tc.ml.vowpalwabbit;

import java.util.Collection;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.impl.DimensionBundle;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.lab.task.impl.FoldDimensionBundle;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.ml.vowpalwabbit.report.VowpalWabbitBaselineMajorityClassIdReport;
import org.dkpro.tc.ml.vowpalwabbit.report.VowpalWabbitBaselineRandomIdReport;
import org.dkpro.tc.ml.vowpalwabbit.report.VowpalWabbitOutcomeIDReport;
import org.dkpro.tc.ml.vowpalwabbit.serialization.VowpalWabbitLoadModelConnector;
import org.dkpro.tc.ml.vowpalwabbit.serialization.VowpalWabbitSerializeModelConnector;
import org.dkpro.tc.ml.vowpalwabbit.writer.VowpalWabbitDataWriter;

/**
 * Using VowpalWabbit in DKPro TC: Depending on the learning and feature mode, a default
 * configuration is used for regression, classification and sequence-classification, which can be
 * overwritten by providing other parameters.
 * 
 * All full list of all parameters that can be provided is here:
 * <pre>
 *      https://github.com/VowpalWabbit/vowpal_wabbit/wiki/Command-line-arguments
 * </pre>
 * 
 * Following default configurations are used:
 * 
 * <pre>
 * Classification (non-sequential): 
 *      --oaa = Use one-against-all multiclass learning
 * 
 * Classification (sequential):
 *      --search_task = sequential
 *      --search_passes_per_policy = 2
 *      --cache = (a cache file is used)
 *      --search_history_length = 1
 *
 * the number of labels that have to predicted are determined in the backend automatically by DKPro TC.
 * 
 * </pre>
 *
 */
public class VowpalWabbitAdapter
    implements TcShallowLearningAdapter
{

    public static TcShallowLearningAdapter getInstance()
    {
        return new VowpalWabbitAdapter();
    }

    @Override
    public ExecutableTaskBase getTestTask()
    {
        return new VowpalWabbitTestTask();
    }

    @Override
    public Class<? extends ReportBase> getOutcomeIdReportClass()
    {
        return VowpalWabbitOutcomeIDReport.class;
    }

    @Override
    public Class<? extends ReportBase> getMajorityClassBaselineIdReportClass()
    {
        return VowpalWabbitBaselineMajorityClassIdReport.class;
    }

    @Override
    public Class<? extends ReportBase> getRandomBaselineIdReportClass()
    {
        return VowpalWabbitBaselineRandomIdReport.class;
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
        return VowpalWabbitDataWriter.class.getName();
    }

    @Override
    public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass()
    {
        return VowpalWabbitLoadModelConnector.class;
    }

    @Override
    public ModelSerializationTask getSaveModelTask()
    {
        return new VowpalWabbitSerializeModelConnector();
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
}
