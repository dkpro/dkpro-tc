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

/**
 * 
 * <pre>
 * 
 * General Parameters:
 * 	booster={gbtree, gblinear, gbree}  (default booster=gbtree)
		which booster to use, can be gbtree, gblinear or dart. gbtree and dart use tree based model while gblinear uses linear function.
	nthread={NUM} (defaults to all available threads) 
		number of parallel threads used to run xgboost
 * objective={} (default objective=reg:linear)
 * 	reg:linear -- linear regression
 * 	reg:logistic -- logistic regression
 * 	binary:logistic -- logistic regression for binary classification, output probability
 *	binary:logitraw -- logistic regression for binary classification, output score before logistic transformation
 *	count:poisson -- poisson regression for count data, output mean of poisson distribution
 *					   max_delta_step is set to 0.7 by default in poisson regression (used to safeguard optimization)
 *	survival:cox -- Cox regression for right censored survival time data (negative values are considered right censored). Note that predictions are returned on the hazard ratio scale (i.e., as HR = exp(marginal_prediction) in the proportional hazard function h(t) = h0(t) * HR).
 *	multi:softmax -- set XGBoost to do multiclass classification using the softmax objective, you also need to set num_class(number of classes)
 *	multi:softprob -- same as softmax, but output a vector of ndata * nclass, which can be further reshaped to ndata, nclass matrix. The result contains predicted probability of each data point belonging to each class.
 *	rank:pairwise -- set XGBoost to do ranking task by minimizing the pairwise loss
 *	reg:gamma -- gamma regression with log-link. Output is a mean of gamma distribution. It might be useful, e.g., for modeling insurance claims severity, or for any outcome that might be gamma-distributed
 *	reg:tweedie -- Tweedie regression with log-link. It might be useful, e.g., for modeling total loss in insurance, or for any outcome that might be Tweedie-distributed.
 * </pre>
 */
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

