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
     * Name of the discriminator that stores the feature filters that are applied on the feature store
     */
    public static final String DIM_FEATURE_FILTERS = "featureFilters";
    
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
     * Name of the class that implements {@link de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore}
     */
    public static final String DIM_FEATURE_STORE = "featureStore";
    
    /**
     * Name of the discriminator which holds the classifier and arguments which serve as baseline
     */
    public static final String DIM_BASELINE_CLASSIFICATION_ARGS = "baselineClassificationArgs";
   
    /**
     * Name of the discriminator which holds the feature set which serves as base
     */
    public static final String DIM_BASELINE_FEATURE_SET = "baselineFeatureSet";
    
    /**
     * Name of the discriminator which holds the feature parameters which serve as base
     */
    public static final String DIM_BASELINE_PIPELINE_PARAMS = "baselinePipelineParams";

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
    /**
     * Name of the file that holds information for the R connect report on test task level
     */
    public static final String R_CONNECT_REPORT_TEST_TASK_FILENAME = "r_connect_report.txt";
    /**
     * Name of the file that holds information for the R connect report on cv level
     */
    public static final String CV_R_CONNECT_REPORT_FILE = "r_connect_cv.csv";

    
    
    /*
     * Machine Learning (General)
     */
    /**
     * Name of the file which holds evaluation results from the machine learning framework
     */
    public static final String RESULTS_FILENAME = "results.prop";
    /**
     * Name of the file which holds evaluation results from the machine learning framework
     */
    public static final String FILENAME_FEATURES = "feature.names";
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
    /**
     * Name of the instance ID feature
     */    
    public static final String ID_FEATURE_NAME = "DKProTCInstanceID";

    
    /*
     * Machine Learning (Model)
     */
    /**
     * Name of the file which holds the model meta data
     */
    public static final String MODEL_META = "model.meta";
    /**
     * Name of the file which holds the feature names
     */
    public static final String MODEL_FEATURE_NAMES = "featureNames.txt";
    /**
     * Name of the file which holds the class labels
     */
    public static final String MODEL_CLASS_LABELS = "classLabels.txt";
    /**
     * Name of the file which holds the feature extractors
     */
    public static final String MODEL_FEATURE_EXTRACTORS = "featureExtractors.txt";
    /**
     * Name of the file which holds the global UIMA parameters
     */
    public static final String MODEL_PARAMETERS = "parameters.txt";
    /**
     * Name of the file which holds the classifier
     */
    public static final String MODEL_CLASSIFIER = "classifier.ser";

}