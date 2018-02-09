/*
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
 */

package org.dkpro.tc.ml.svmhmm;

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
import org.dkpro.tc.io.libsvm.LibsvmDataFormatOutcomeIdReport;
import org.dkpro.tc.ml.svmhmm.task.SvmHmmTestTask;
import org.dkpro.tc.ml.svmhmm.task.serialization.SvmhmmLoadModelConnector;
import org.dkpro.tc.ml.svmhmm.task.serialization.SvmhmmSerializeModelConnector;
import org.dkpro.tc.ml.svmhmm.writer.SvmHmmDataWriter;

/**
 * Wrapper for training and testing using SVM_HMM C implementation with default parameters. Consult
 * {@code http://www.cs.cornell.edu/people/tj/svm_light/svm_hmm.html} for parameter settings.
 * 
 * <pre>
 * Parameters:
 *   -c      Typical SVM parameter C trading-off slack vs. magnitude of the weight-vector. 
 *               NOTE: The default value for this parameter is unlikely to work well for your
 *               particular problem. A good value for C must be selected via cross-validation, ideally
 *               exploring values over several orders of magnitude. NOTE: Unlike in V1.01, the value of C is
 *               divided by the number of training examples. So, to get results equivalent to V1.01, multiply
 *               C by the number of training examples. Default value is set to 1.
 *   -e      Parameter "-e &lt;EPSILON&gt;": This specifies the precision to which constraints are
 *               required to be satisfied by the solution. The smaller EPSILON, the longer and the more memory
 *               training takes, but the solution is more precise. However, solutions more accurate than 0.5
 *               typically do not improve prediction accuracy.
 *   -t      Order of dependencies of transitions in HMM. Can be any number larger than 1. (default 1)
 *   -m      Order of dependencies of emissions in HMM. Can be any number
 *               larger than 0. (default 0) UPDATE: according to svm_struct_api.c: must be either 0 or 1;
 *               fails for &gt;1
 *   -b       A non-zero value turns on (approximate) beam search to replace
 *               the exact Viterbi algorithm both for finding the most violated constraint, as well as for
 *               computing predictions. The value is the width of the beam used (e.g. 100). (default 0).
 * </pre>
 */
public class SvmHmmAdapter
	implements TcShallowLearningAdapter
{

    @Override
    public ExecutableTaskBase getTestTask()
    {
        return new SvmHmmTestTask();
    }

    @Override
    public Class<? extends ReportBase> getOutcomeIdReportClass()
    {
        return LibsvmDataFormatOutcomeIdReport.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DimensionBundle<Collection<String>> getFoldDimensionBundle(
            String[] files, int folds)
    {
        return new FoldDimensionBundle<>("files", Dimension.create("", files), folds);
    }

	@Override
	public Class<? extends DataWriter> getDataWriterClass() {
		return SvmHmmDataWriter.class;
	}

	@Override
	public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass() {
		return SvmhmmLoadModelConnector.class;
	}

	@Override
	public Class<? extends ModelSerializationTask> getSaveModelTask() {
		return SvmhmmSerializeModelConnector.class;
	}

	@Override
	public boolean useSparseFeatures() {
		return true;
	}
	
	@Override
    public String toString(){
    	return getClass().getSimpleName();
    }
}

