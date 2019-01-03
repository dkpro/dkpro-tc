/*******************************************************************************
 * Copyright 2019
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
package org.dkpro.tc.ml.crfsuite.core;

public enum CrfSuiteAlgo 
{
    
    /** Maximize the logarithm of the likelihood of the training data with L1 and/or L2
    * regularization term(s) using the Limited-memory Broyden-Fletcher-Goldfarb-Shanno (L-BFGS)
    * method. When a non-zero coefficient for L1 regularization term is specified, the algorithm
    * switches to the Orthant-Wise Limited-memory Quasi-Newton (OWL-QN) method. In practice, this
    * algorithm improves feature weights very slowly at the beginning of a training process, but
    * converges to the optimal feature weights quickly in the end.
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
    LBFGS("lbfgs"), //Limited-memory Broyden-Fletcher-Goldfarb-Shanno
    
    /**
     * Given an item sequence (x, y) in the training data, the algorithm computes the loss: s(x, y')
     * - s(x, y) + sqrt(d(y', y)), where s(x, y') is the score of the Viterbi label sequence, s(x,
     * y) is the score of the label sequence of the training data, and d(y', y) measures the
     * distance between the Viterbi label sequence (y') and the reference label sequence (y). If the
     * item suffers from a non-negative loss, the algorithm updates the model based on the loss.
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
    AVERAGED_PERCEPTRON("ap"),
    
    /**
     * Given an item sequence (x, y) in the training data, the algorithm computes the loss: s(x, y')
     * - s(x, y), where s(x, y') is the score of the Viterbi label sequence, and s(x, y) is the
     * score of the label sequence of the training data.
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
    ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTORS("arow"), // adaptive regularization of weight vector
    
    /**
     * Maximize the logarithm of the likelihood of the training data with L2 regularization term(s)
     * using Stochastic Gradient Descent (SGD) with batch size 1. This algorithm usually approaches
     * to the optimal feature weights quite rapidly, but shows slow convergences at the end.
     * 
     * <pre>
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
     * </pre>
     */
    L2SGD("l2sgd"); // Stochastic Gradient Descent 

    private String crfName;

    private CrfSuiteAlgo(String name){this.crfName = name;}

    @Override
    public String toString() {
        return crfName;
    }
}
