/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.crfsuite;

import java.util.Collection;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.impl.DimensionBundle;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.lab.task.impl.FoldDimensionBundle;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.crfsuite.task.CRFSuiteTestTask;
import org.dkpro.tc.crfsuite.task.serialization.CRFSuiteModelSerializationDescription;
import org.dkpro.tc.crfsuite.task.serialization.LoadModelConnectorCRFSuite;
import org.dkpro.tc.crfsuite.writer.CRFSuiteDataWriter;
import org.dkpro.tc.fstore.simple.SparseFeatureStore;
import org.dkpro.tc.ml.report.InnerBatchUsingTCEvaluationReport;

/**
 * CRFSuite machine learning adapter. Details about available algorithm and their configuration is
 * provided in this class see constants prefixed with ALGORITHM.
 */
public class CRFSuiteAdapter
    implements TCMachineLearningAdapter
{

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
    public Class<? extends ReportBase> getOutcomeIdReportClass()
    {
        return CRFSuiteOutcomeIDReport.class;
    }

    @Override
    public Class<? extends ReportBase> getBatchTrainTestReportClass()
    {
        return InnerBatchUsingTCEvaluationReport.class;
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
        case featureSelectionFile:
            return "attributeEvaluationResults.txt";
        }
        return null;
    }

    @Override
    public Class<? extends DataWriter> getDataWriterClass()
    {
        return CRFSuiteDataWriter.class;
    }

    @Override
    public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass()
    {
        return LoadModelConnectorCRFSuite.class;
    }

    @Override
    public Class<? extends ModelSerializationTask> getSaveModelTask()
    {
        return CRFSuiteModelSerializationDescription.class;
    }

    @Override
    public String getFeatureStore()
    {
        return SparseFeatureStore.class.getName();
    }

    /**
     * Maximize the logarithm of the likelihood of the training data with L1 and/or L2
     * regularization term(s) using the Limited-memory Broyden-Fletcher-Goldfarb-Shanno (L-BFGS)
     * method. When a non-zero coefficient for L1 regularization term is specified, the algorithm
     * switches to the Orthant-Wise Limited-memory Quasi-Newton (OWL-QN) method. In practice, this
     * algorithm improves feature weights very slowly at the beginning of a training process, but
     * converges to the optimal feature weights quickly in the end.
     * 
     * Parameters (provided each individually by [-p] switch):
     * 
     * <pre>
         float feature.minfreq = 0.000000;
         The minimum frequency of features.
    
        int feature.possible_states = 0;
        Force to generate possible state features.
    
        int feature.possible_transitions = 0;
        Force to generate possible transition features.
    
        float c1 = 0.000000;
        Coefficient for L1 regularization.
    
        float c2 = 1.000000;
        Coefficient for L2 regularization.
    
        int max_iterations = 2147483647;
        The maximum number of iterations for L-BFGS optimization.
    
        int num_memories = 6;
        The number of limited memories for approximating the inverse hessian matrix.
    
        float epsilon = 0.000010;
        Epsilon for testing the convergence of the objective.
    
        int period = 10;
        The duration of iterations to test the stopping criterion.
    
        float delta = 0.000010;
        The threshold for the stopping criterion; an L-BFGS iteration stops when the
        improvement of the log likelihood over the last ${period} iterations is no
        greater than this threshold.
    
        string linesearch = MoreThuente;
        The line search algorithm used in L-BFGS updates:
        {   'MoreThuente': More and Thuente's method,
            'Backtracking': Backtracking method with regular Wolfe condition,
            'StrongBacktracking': Backtracking method with strong Wolfe condition
        }
    
        int max_linesearch = 20;
        The maximum number of trials for the line search algorithm.
     * </pre>
     * 
     */
    public static final String ALGORITHM_LBFGS = "lbfgs";

    /**
     * Given an item sequence (x, y) in the training data, the algorithm computes the loss: s(x, y')
     * - s(x, y), where s(x, y') is the score of the Viterbi label sequence, and s(x, y) is the
     * score of the label sequence of the training data.
     * 
     * Parameters (provided each individually by [-p] switch):
     * 
     * <pre>
         float feature.minfreq = 0.000000;
         The minimum frequency of features.
    
         int feature.possible_states = 0;
         Force to generate possible state features.
    
         int feature.possible_transitions = 0;
         Force to generate possible transition features.
    
         float variance = 1.000000;
         The initial variance of every feature weight.
    
         float gamma = 1.000000;
         Tradeoff parameter.
    
         int max_iterations = 100;
         The maximum number of iterations.
    
         float epsilon = 0.000000;
         The stopping criterion (the mean loss).
     * </pre>
     */
    public static final String ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR = "arow";

    /**
     * Given an item sequence (x, y) in the training data, the algorithm computes the loss: s(x, y')
     * - s(x, y) + sqrt(d(y', y)), where s(x, y') is the score of the Viterbi label sequence, s(x,
     * y) is the score of the label sequence of the training data, and d(y', y) measures the
     * distance between the Viterbi label sequence (y') and the reference label sequence (y). If the
     * item suffers from a non-negative loss, the algorithm updates the model based on the loss.
     * 
     * Parameters (provided each individually by [-p] switch):
     * 
     * <pre>
         float feature.minfreq = 0.000000;
         The minimum frequency of features.
    
         int feature.possible_states = 0;
         Force to generate possible state features.
    
         int feature.possible_transitions = 0;
         Force to generate possible transition features.
    
         int max_iterations = 100;
         The maximum number of iterations.
    
         float epsilon = 0.000000;
         The stopping criterion (the ratio of incorrect label predictions).
     * </pre>
     */
    public static final String ALGORITHM_AVERAGED_PERCEPTRON = "ap";
    
    /**
     * Maximize the logarithm of the likelihood of the training data with L2 regularization term(s)
     * using Stochastic Gradient Descent (SGD) with batch size 1. This algorithm usually approaches
     * to the optimal feature weights quite rapidly, but shows slow convergences at the end.
     * 
     * Parameters (provided each individually by [-p] switch):
     * 
     <pre>
        float feature.minfreq = 0.000000;
        The minimum frequency of features.

        int feature.possible_states = 0;
        Force to generate possible state features.

        int feature.possible_transitions = 0;
        Force to generate possible transition features.

        float c2 = 1.000000;
        Coefficient for L2 regularization.

        int max_iterations = 1000;
        The maximum number of iterations (epochs) for SGD optimization.

        int period = 10;
        The duration of iterations to test the stopping criterion.

        float delta = 0.000001;
        The threshold for the stopping criterion; an optimization process stops when
        the improvement of the log likelihood over the last ${period} iterations is no
        greater than this threshold.

        float calibration.eta = 0.100000;
        The initial value of learning rate (eta) used for calibration.

        float calibration.rate = 2.000000;
        The rate of increase/decrease of learning rate for calibration.

        int calibration.samples = 1000;
        The number of instances used for calibration.

        int calibration.candidates = 10;
        The number of candidates of learning rate.

        int calibration.max_trials = 20;
        The maximum number of trials of learning rates for calibration.
        </pre>
     */
    public static final String ALGORITHM_L2_STOCHASTIC_GRADIENT_DESCENT = "l2sgd";
}
