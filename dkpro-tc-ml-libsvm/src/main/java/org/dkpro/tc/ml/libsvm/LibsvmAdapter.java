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
import org.dkpro.tc.io.libsvm.LibsvmDataFormatOutcomeIdReport;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatWriter;
import org.dkpro.tc.ml.libsvm.serialization.LibsvmLoadModelConnector;
import org.dkpro.tc.ml.libsvm.serialization.LibsvmSerializeModelConnector;

/**
 * Libsvm offers various configuration switches for the SVM type and Kernel we defined constant
 * values in this class with more detail explanations.
 * 
 * <pre>
 * -s svm_type (default o)
 * 		0 -- C-SVC
 * 		1 -- nu-SVC
 *		2 -- one-class SVM
 *		3 -- epsilon-SVR
 *		4 -- nu-SVR
 * -t kernel_type (default 2)
 * 		0 -- linear: u'*v
 *		1 -- polynomial: (gamma*u'*v + coef0)^degree
 *		2 -- radial basis function: exp(-gamma*|u-v|^2)
 *		3 -- sigmoid: tanh(gamma*u'*v + coef0)
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
        return LibsvmLoadModelConnector.class;
    }

    @Override
    public Class<? extends ModelSerializationTask> getSaveModelTask()
    {
        return LibsvmSerializeModelConnector.class;
    }
    
    @Override
	public boolean useSparseFeatures() {
		return true;
	}
    
}

