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
package de.tudarmstadt.ukp.dkpro.tc.core;

import org.apache.uima.cas.CAS;

/**
 * Basic constants that are used throughout the project
 * 
 * @author zesch
 * 
 */
public interface Constants
{
    /*
     * Pairwise classification
     */

    /**
     * Name of the initial view
     */
    public static String INITIAL_VIEW = CAS.NAME_DEFAULT_SOFA;
    /**
     * Name of the first view in a pair classification setup
     */
    public static String PART_ONE = "PART_ONE";
    /**
     * Name of the second view in a pair classification setup
     */
    public static String PART_TWO = "PART_TWO";

    /*
     * Instance storage
     */

    /**
     * This prefix is used to make sure that class label names do not match names of features
     */
    public static final String CLASS_ATTRIBUTE_PREFIX = "__";
    /**
     * The name of the attribute that encodes the known classification outcome
     */
    public static final String CLASS_ATTRIBUTE_NAME = "outcome";

    /**
     * Special value for the number of folds, that is used to indicate leave-one-out setups
     */
    public static final int LEAVE_ONE_OUT = -1;

    /*
     * Readers
     */

    /**
     * Name of the outcome value for instances in prediction mode
     */
    public static String UNKNOWN_OUTCOME = "UNKNOWN_OUTCOME";

    /*
     * Discriminators
     */

    /**
     * Name of the discriminator that stores the reader for training data
     */
    public static final String DIM_READER_TRAIN = "readerTrain";
    /**
     * Name of the discriminator that stores the parameters for the training data reader
     */
    public static final String DIM_READER_TRAIN_PARAMS = "readerTrainParams";
    /**
     * Name of the discriminator that stores the reader for test data
     */
    public static final String DIM_READER_TEST = "readerTest";
    /**
     * Name of the discriminator that stores the parameters for the test data reader
     */
    public static final String DIM_READER_TEST_PARAMS = "readerTestParams";

    /**
     * Name of the discriminator that stores the set of feature extractors
     */
    public static final String DIM_FEATURE_SET = "featureSet";

    /**
     * Name of the discriminator that stores the additional pipeline parameters
     */
    public static final String DIM_PIPELINE_PARAMS = "pipelineParameters";

    /**
     * Name of the discriminator that stores the additional argument passed to the classification
     * algorithms
     */
    public static final String DIM_CLASSIFICATION_ARGS = "classificationArguments";

    /**
     * Name of the discriminator that stores the feature selection class and a list of arguments to
     * parametrize it
     */
    public static final String DIM_ATTRIBUTE_EVALUATOR_ARGS = "attributeEvaluator";

    /**
     * Name of the discriminator that stores the feature selection search class and a list of
     * arguments to parametrize it (single-label learning)
     */
    public static final String DIM_FEATURE_SEARCHER_ARGS = "featureSearcher";

    /**
     * Name of the discriminator that stores a Mulan label transformation method (multi-label
     * learning)
     */
    public static final String DIM_LABEL_TRANSFORMATION_METHOD = "labelTransformationMethod";

    /**
     * Name of the discriminator that stores the number of features to be selected (multi-label
     * learning)
     */
    public static final String DIM_NUM_LABELS_TO_KEEP = "numLabelsToKeep";

    /**
     * Name of the discriminator that stores whether the feature selection should be applied to
     * learning task or not
     */
    public static final String DIM_APPLY_FEATURE_SELECTION = "applySelection";

    /**
     * Name of the discriminator that stores the bipartition threshold used in multi-label
     * classification
     */
    public static final String DIM_BIPARTITION_THRESHOLD = "threshold";

    /**
     * Name of the discriminator that stores the data writer class
     */
    public static final String DIM_DATA_WRITER = "dataWriter";

    /*
     * Learning modes
     */
    /**
     * Name of the discriminator that stores the learning mode
     */
    public static final String DIM_LEARNING_MODE = "learningMode";
    /**
     * Learning mode: single label
     */
    public static final String LM_SINGLE_LABEL = "singleLabel";
    /**
     * Learning mode: multi label
     */
    public static final String LM_MULTI_LABEL = "multiLabel";
    /**
     * Learning mode: regression
     */
    public static final String LM_REGRESSION = "regression";

    /*
     * feature modes
     */
    /**
     * Name of the discriminator that stores the learning mode
     */
    public static final String DIM_FEATURE_MODE = "featureMode";
    /**
     * Feature mode: document classification
     */
    public static final String FM_DOCUMENT = "document";
    /**
     * Feature mode: unit classification
     */
    public static final String FM_UNIT = "unit";
    /**
     * Feature mode: sequence classification
     */
    public static final String FM_SEQUENCE = "sequence";
    /**
     * Feature mode: unit classification
     */
    public static final String FM_PAIR = "pair";

    /*
     * Mainly for reports
     */
    /**
     * Name of the file that holds the evaluation results
     */
    public static final String EVAL_FILE_NAME = "evaluation_results";
    /**
     * File suffix for EXCEL files
     */
    public static final String SUFFIX_EXCEL = ".xls";
    /**
     * File suffix for CSV files
     */
    public static final String SUFFIX_CSV = ".csv";
    /**
     * Name of the file that holds the confusion matrix
     */
    public static final String CONFUSIONMATRIX_KEY = "confusionMatrix.csv";
    /**
     * Name of the file that holds the precision-recall graph
     */
    public static final String PR_CURVE_KEY = "PR_curve.svg";
    /**
     * Name of the confusion matrix dimension showing the actual values
     */
    public static final String CM_ACTUAL = " (act.)";
    /**
     * Name of the confusion matrix dimension showing the predicted values
     */
    public static final String CM_PREDICTED = " (pred.)";

    
    /*
     * Machine Learning (General)
     */
    /**
     * Name of the file which holds evaluation results from the machine learning framework
     */
    public static final String RESULTS_FILENAME = "results.prop";
    /**
     * Name of the attribute/label which stores the prediction values
     */
    public static final String PREDICTION_CLASS_LABEL_NAME = "prediction";
    /**
     * Name of the training data input key in the TestTask
     */
    public static final String TEST_TASK_INPUT_KEY_TRAINING_DATA = "input.train";
    /**
     * Name of the test data input key in the TestTask
     */
    public static final String TEST_TASK_INPUT_KEY_TEST_DATA = "input.test";
    /**
     * Name of the output input key in the TestTask
     */
    public static final String TEST_TASK_OUTPUT_KEY = "output";

}