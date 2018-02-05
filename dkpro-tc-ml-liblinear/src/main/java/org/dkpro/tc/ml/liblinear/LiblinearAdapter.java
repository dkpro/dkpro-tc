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
package org.dkpro.tc.ml.liblinear;

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
import org.dkpro.tc.ml.liblinear.serialization.LiblinearSerializeModelConnector;
import org.dkpro.tc.ml.liblinear.serialization.LiblinearLoadModelConnector;
import org.dkpro.tc.ml.report.InnerBatchReport;

/**
 * <pre>
{@literal
  -s type : set type of solver (default 1)
      0 -- L2-regularized logistic regression (primal)
      1 -- L2-regularized L2-loss support vector classification (dual)
      2 -- L2-regularized L2-loss support vector classification (primal)
      3 -- L2-regularized L1-loss support vector classification (dual)
      4 -- multi-class support vector classification by Crammer and Singer
      5 -- L1-regularized L2-loss support vector classification
      6 -- L1-regularized logistic regression
      7 -- L2-regularized logistic regression (dual)
     11 -- L2-regularized L2-loss support vector regression (dual)
     12 -- L2-regularized L1-loss support vector regression (dual)
     13 -- L2-regularized L2-loss support vector regression (primal)
  -c cost : set the parameter C (default 1)
  -e epsilon : set tolerance of termination criterion
    -s 0 and 2
        |f'(w)|_2 &lt;= eps*min(pos,neg)/l*|f'(w0)|_2,
        where f is the primal function and pos/neg are # of
        positive/negative data (default 0.01)
    -s 1, 3, 4 and 7
       Dual maximal violation <= eps; similar to libsvm (default 0.1)
    -s 5 and 6
       |f'(w)|_inf &le; eps*min(pos,neg)/l*|f'(w0)|_inf,
       where f is the primal function (default 0.01)
  -B bias : if bias &ge 0, instance x becomes [x; bias]; if &lt; 0, no bias term added (default -1)
  -wi weight: weights adjust the parameter C of different classes (see README for details)
  }
 * </pre>
 */
public class LiblinearAdapter 
	implements TcShallowLearningAdapter
{

	public static TcShallowLearningAdapter getInstance() {
		return new LiblinearAdapter();
	}
	
	@Override
	public ExecutableTaskBase getTestTask() {
		return new LiblinearTestTask();
	}

	@Override
	public Class<? extends ReportBase> getOutcomeIdReportClass() {
		return LibsvmDataFormatOutcomeIdReport.class;
	}

	@Override
	public Class<? extends ReportBase> getBatchTrainTestReportClass() {
		return InnerBatchReport.class;
	}

	@SuppressWarnings("unchecked")
    @Override
	public DimensionBundle<Collection<String>> getFoldDimensionBundle(
			String[] files, int folds) {
		return  new FoldDimensionBundle<String>("files", Dimension.create("", files), folds);
	}
	
	@Override
	public Class<? extends DataWriter> getDataWriterClass() {
		return LibsvmDataFormatWriter.class;
	}
	
	@Override
	public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass() {
		return LiblinearLoadModelConnector.class;
	}

	@Override
	public Class<? extends ModelSerializationTask> getSaveModelTask() {
	    return LiblinearSerializeModelConnector.class;
	}

	@Override
	public boolean useSparseFeatures() {
		return true;
	}
}
