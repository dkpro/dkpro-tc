/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.crfsuite;

import java.util.Collection;

import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.DimensionBundle;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.FoldDimensionBundle;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.CRFSuiteTestTask;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.serialization.LoadModelConnectorCRFSuite;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.writer.CRFSuiteDataWriter;

public class CRFSuiteAdapter
    implements TCMachineLearningAdapter
{

    /**
     * Maximize the logarithm of the likelihood of the training data with L1 and/or L2
     * regularization term(s) using the Limited-memory Broyden-Fletcher-Goldfarb-Shanno (L-BFGS)
     * method. When a non-zero coefficient for L1 regularization term is specified, the algorithm
     * switches to the Orthant-Wise Limited-memory Quasi-Newton (OWL-QN) method. In practice, this
     * algorithm improves feature weights very slowly at the beginning of a training process, but
     * converges to the optimal feature weights quickly in the end.
     */
    public static final String ALGORITHM_LBFGS = "lbfgs";

    /**
     * Maximize the logarithm of the likelihood of the training data with L2 regularization term(s)
     * using Stochastic Gradient Descent (SGD) with batch size 1. This algorithm usually approaches
     * to the optimal feature weights quite rapidly, but shows slow convergences at the end.
     */
    public static final String ALGORITHM_L2_STOCHASTIC_GRADIENT_DESCENT = "l2sgd";

    /**
     * Given an item sequence (x, y) in the training data, the algorithm computes the loss: s(x, y')
     * - s(x, y) + sqrt(d(y', y)), where s(x, y') is the score of the Viterbi label sequence, s(x,
     * y) is the score of the label sequence of the training data, and d(y', y) measures the
     * distance between the Viterbi label sequence (y') and the reference label sequence (y). If the
     * item suffers from a non-negative loss, the algorithm updates the model based on the loss.
     */
    public static final String ALGORITHM_AVERAGED_PERCEPTRON = "ap";

    /**
     * Given an item sequence (x, y) in the training data, the algorithm computes the loss: s(x, y')
     * - s(x, y), where s(x, y') is the score of the Viterbi label sequence, and s(x, y) is the
     * score of the label sequence of the training data.
     */
    public static final String ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR = "arow";

    public static TCMachineLearningAdapter getInstance()
    {
        return new CRFSuiteAdapter();
    }

    public static String getOutcomeMappingFilename()
    {
        return "outcome-mapping.txt";
    }

    @Override
    public ExecutableTaskBase getTestTask()
    {
        return new CRFSuiteTestTask();
    }

    @Override
    public Class<? extends ReportBase> getClassificationReportClass()
    {
        return CRFSuiteClassificationReport.class;
    }

    @Override
    public Class<? extends ReportBase> getOutcomeIdReportClass()
    {
        return CRFSuiteOutcomeIDReport.class;
    }

    @Override
    public Class<? extends ReportBase> getBatchTrainTestReportClass()
    {
        return CRFSuiteBatchTrainTestReport.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DimensionBundle<Collection<String>> getFoldDimensionBundle(String[] files, int folds)
    {
        return new FoldDimensionBundle<String>("files", Dimension.create("", files), folds);
    }

    @Override
    public String getFrameworkFilename(AdapterNameEntries aName)
    {
        switch (aName) {
        case featureVectorsFile:
            return "training-data.txt";
        case predictionsFile:
            return "predictions.txt";
        case evaluationFile:
            return "evaluation.txt";
        case featureSelectionFile:
            return "attributeEvaluationResults.txt";
        }
        return null;
    }

	@Override
	public Class<? extends DataWriter> getDataWriterClass() {
		return CRFSuiteDataWriter.class;
	}
	
	@Override
	public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass() {
	    return LoadModelConnectorCRFSuite.class;
	}
}
