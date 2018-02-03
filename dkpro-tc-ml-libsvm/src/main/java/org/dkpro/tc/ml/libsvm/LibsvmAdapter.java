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
package org.dkpro.tc.ml.libsvm;

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
import org.dkpro.tc.io.libsvm.LibsvmDataFormatOutcomeIdReport;
import org.dkpro.tc.ml.libsvm.serialization.LibsvmModelSerializationDescription;
import org.dkpro.tc.ml.libsvm.serialization.LoadModelConnectorLibsvm;
import org.dkpro.tc.ml.report.InnerBatchReport;

/**
 * Libsvm offers various configuration switches for the SVM type and Kernel we defined constant
 * values in this class with more detail explanations.
 * 
 * <pre>
 * -s svm_type (see constants)
 * -t kernel_type (see constants) 
 * -d degree : set degree in kernel function (default 3) 
 * -g gamma : set gamma in kernel function (default 1/num_features) 
 * -r coef0 : set coef0 in kernel function (default 0)
 * -c cost : set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1)
 * -n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5) 
 * -p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1) 
 * -m cachesize : set cache memory size in MB (default 100) 
 * -e epsilon : set tolerance of termination criterion (default 0.001) 
 * -h shrinking: whether to use the shrinking heuristics, 0 or 1 (default 1) 
 * -b probability_estimates : whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0) 
 * -wi weight : set the parameter C of class i to weight*C, for C-SVC (default 1)
 * </pre>
 */
public class LibsvmAdapter
    implements TcShallowLearningAdapter
{

    public static TcShallowLearningAdapter getInstance()
    {
        return new LibsvmAdapter();
    }

    public static String getOutcomeMappingFilename()
    {
        return "outcome-mapping.txt";
    }
    
	public static String getFeatureNameMappingFilename() {
		return "feature-name-mapping.txt";
	}

    public static String getFeatureNames()
    {
        return "featurenames.txt";
    }

    @Override
    public ExecutableTaskBase getTestTask()
    {
        return new LibsvmTestTask();
    }

    @Override
    public Class<? extends ReportBase> getOutcomeIdReportClass()
    {
        return LibsvmDataFormatOutcomeIdReport.class;
    }

    @Override
    public Class<? extends ReportBase> getBatchTrainTestReportClass()
    {
        return InnerBatchReport.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DimensionBundle<Collection<String>> getFoldDimensionBundle(String[] files, int folds)
    {
        return new FoldDimensionBundle<String>("files", Dimension.create("", files), folds);
    }

    @Override
    public Class<? extends DataWriter> getDataWriterClass()
    {
        return LibsvmDataFormatWriter.class;
    }

    @Override
    public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass()
    {
        return LoadModelConnectorLibsvm.class;
    }

    @Override
    public Class<? extends ModelSerializationTask> getSaveModelTask()
    {
        return LibsvmModelSerializationDescription.class;
    }
    
    @Override
	public boolean useSparseFeatures() {
		return true;
	}

    /** SVM type is set by switch [-s] */
    public static final String PARAM_SVM_TYPE_C_SVC_MULTI_CLASS = "0";
    /** SVM type is set by switch [-s] */
    public static final String PARAM_SVM_TYPE_NU_SVC_MULTI_CLASS = "1";
    /** SVM type is set by switch [-s] */
    public static final String PARAM_SVM_TYPE_ONE_CLASS_SVM = "2";
    /** SVM type is set by switch [-s] */
    public static final String PARAM_SVM_TYPE_EPISLON_SVR_REGRESSION = "3";
    /** SVM type is set by switch [-s] */
    public static final String PARAM_SVM_TYPE_NU_SVR_REGRESSION = "4";

    /** Polynomial kernel = u'*v set by switch [-t] (default) */
    public static final String PARAM_KERNEL_LINEAR = "0";
    /** Polynomial kernel = (gamma*u'*v + coef0)^degree set by switch [-t] */
    public static final String PARAM_KERNEL_POLYNOMIAL = "1";
    /** Radial based = exp(-gamma*|u-v|^2) set by switch [-t] */
    public static final String PARAM_KERNEL_RADIAL_BASED = "2";
    /** sigmoid = tanh(gamma*u'*v + coef0) set by switch [-t] */
    public static final String PARAM_KERNEL_SIGMOID = "3";
    /** precomputed kernel (kernel values in training_set_file) set by switch [-t] */
    public static final String PARAM_KERNEL_PRE_COMPUTED = "4";
    
}

