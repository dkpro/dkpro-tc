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
package org.dkpro.tc.core;

import org.apache.uima.cas.CAS;

/**
 * Basic constants that are used throughout the project
 */
public interface Constants
{
    /*
     * Pairwise classification
     */

    /**
     * Name of the initial view
     */
    String INITIAL_VIEW = CAS.NAME_DEFAULT_SOFA;
    /**
     * Name of the first view in a pair classification setup
     */
    String PART_ONE = "PART_ONE";
    /**
     * Name of the second view in a pair classification setup
     */
    String PART_TWO = "PART_TWO";

    /*
     * Instance storage
     */

    /**
     * This prefix is used to make sure that class label names do not match names of features
     */
    String CLASS_ATTRIBUTE_PREFIX = "__";
    /**
     * The name of the attribute that encodes the known classification outcome
     */
    String CLASS_ATTRIBUTE_NAME = "outcome";

    /**
     * Special value for the number of folds, that is used to indicate leave-one-out setups
     */
    int LEAVE_ONE_OUT = -1;

    /*
     * Readers
     */
    /**
     * Name of the outcome value for instances in prediction mode
     */
    String UNKNOWN_OUTCOME = "UNKNOWN_OUTCOME";

    /*
     * Discriminators
     */

    /**
     * Name of the discriminator that stores a map containing either both (train and test) readers
     * or only the train reader (i.e. cross-validation mode)
     */
    String DIM_READERS = "readers";

    /**
     * Name of the discriminator that stores the reader for training data
     */
    String DIM_READER_TRAIN = "readerTrain";

    /**
     * Name of the discriminator that stores the reader for test data
     */
    String DIM_READER_TEST = "readerTest";

    /**
     * Name of the discriminator that stores the set of feature extractors
     */
    String DIM_FEATURE_SET = "featureSet";

    /**
     * Name of the discriminator that stores the additional argument passed to the classification
     * algorithms
     */
    String DIM_CLASSIFICATION_ARGS = "classificationArguments";
    
    /**
     * Name of the discriminator that stores all machine learning adapter configurations that are being used in an experiment
     */
    String DIM_MLA_CONFIGURATIONS = "machineLearningAdapterConfiguration";

    /**
     * Name of the discriminator that stores the feature selection class and a list of arguments to
     * parametrize it
     */
    String DIM_ATTRIBUTE_EVALUATOR_ARGS = "attributeEvaluator";

    /**
     * Name of the discriminator that stores the feature selection search class and a list of
     * arguments to parametrize it (single-label learning)
     */
    String DIM_FEATURE_SEARCHER_ARGS = "featureSearcher";

    /**
     * Name of the discriminator that stores a Mulan label transformation method (multi-label
     * learning)
     */
    String DIM_LABEL_TRANSFORMATION_METHOD = "labelTransformationMethod";

    /**
     * Name of the discriminator that stores the number of features to be selected (multi-label
     * learning)
     */
    String DIM_NUM_LABELS_TO_KEEP = "numLabelsToKeep";

    /**
     * Name of the discriminator that stores the feature filters that are applied on the feature
     * store
     */
    String DIM_FEATURE_FILTERS = "featureFilters";

    /**
     * Name of the discriminator that stores whether the feature selection should be applied to
     * learning task or not
     */
    String DIM_APPLY_FEATURE_SELECTION = "applySelection";

    /**
     * Name of the discriminator that stores the bipartition threshold used in multi-label
     * classification
     */
    String DIM_BIPARTITION_THRESHOLD = "threshold";

    /**
     * Flag to use sparse features during feature extraction
     */
    String DIM_FEATURE_USE_SPARSE = "useSparseFeatures";

    /**
     * Name of the class that implements the data writer for a machine learning adapter
     */
    String DIM_DATA_WRITER = "mlaDataWriter";

    /**
     * Name of the discriminator that stores whether the instance weighting should be applied to
     * learning task or not
     */
    String DIM_APPLY_INSTANCE_WEIGHTING = "applyWeighting";

    /**
     * Developer mode enables it to use unit feature extractors in documents
     */
    String DIM_DEVELOPER_MODE = "developerMode";

    /**
     * Records the context of a unit/sequence in either unit mode or sequence mode as debugging help
     */
    String DIM_RECORD_CONTEXT = "recordContext";

    String GENERIC_FEATURE_FILE = "JSON.txt";

    /**
     * File name for storing the predictions in the classifier specific output data format
     */
    String FILENAME_PREDICTIONS = "predictions.txt";

    /**
     * File name for storing the training / testing data file
     */
    String FILENAME_DATA_IN_CLASSIFIER_FORMAT = "featureFile.txt";

    String DIM_FILES_ROOT = "filesRoot";

    String DIM_FILES_TRAINING = "files_training";

    String DIM_FILES_VALIDATION = "files_validation";
    
    /**
     * Enforces that during writing of the test feature file only features are written that did
     * occur during training. This enforcement is computational extremely expensive and will
     * increase testing time considerably.
     */
    String DIM_ENFORCE_MATCHING_FEATURES_DURING_TESTING = "enforceMatchingFeatures";

    /*
     * Learning modes
     */
    /**
     * Name of the discriminator that stores the learning mode
     */
    String DIM_LEARNING_MODE = "learningMode";
    /**
     * Learning mode: single label
     */
    String LM_SINGLE_LABEL = "singleLabel";
    /**
     * Learning mode: multi label
     */
    String LM_MULTI_LABEL = "multiLabel";
    /**
     * Learning mode: regression
     */
    String LM_REGRESSION = "regression";

    /*
     * feature modes
     */
    /**
     * Name of the discriminator that stores the learning mode
     */
    String DIM_FEATURE_MODE = "featureMode";
    /**
     * Feature mode: document classification
     */
    String FM_DOCUMENT = "document";
    /**
     * Feature mode: unit classification
     */
    String FM_UNIT = "unit";
    /**
     * Feature mode: sequence classification
     */
    String FM_SEQUENCE = "sequence";
    /**
     * Feature mode: unit classification
     */
    String FM_PAIR = "pair";

    /*
     * Mainly for reports
     */
    /**
     * Name of the file that holds the evaluation results
     */
    String EVAL_FILE_NAME = "evaluation_results";

    /**
     * Name of the file that holds the evaluation results
     */
    String EVAL_FILE_NAME_PER_FOLD = "evaluation_results_fold";

    /**
     * File suffix for LaTeX files
     */
    String SUFFIX_LATEX = ".tex";
    /**
     * Name of the file that holds the confusion matrix
     */
    String CONFUSIONMATRIX_KEY = "confusionMatrix.csv";
    /**
     * Name of the file that holds the precision-recall graph
     */
    String PR_CURVE_KEY = "PR_curve.svg";
    /**
     * Name of the confusion matrix dimension showing the actual values
     */
    String CM_ACTUAL = " (act.)";
    /**
     * Name of the confusion matrix dimension showing the predicted values
     */
    String CM_PREDICTED = " (pred.)";
    /**
     * Name of the file that holds information for the R connect report on test task level
     */
    String STATISTICS_REPORT_TEST_TASK_FILENAME = "statistics_eval_task.txt";
    /**
     * Name of the file that holds information for the R connect report on cv level
     */
    String STATISTICS_REPORT_FILENAME = "statistics_eval.csv";
    /**
     * Name of the file which folds the classifier predictions and gold standard for all test
     * instances.
     */
    String ID_OUTCOME_KEY = "id2outcome.txt";
    /**
     * Name of the file which assigns the majority class as prediction or the mean floating point
     * value in case of regression, as baseline
     */
    String BASELINE_MAJORITIY_ID_OUTCOME_KEY = "baselineMajority2outcome.txt";
    /**
     * Name of the file which assigns for each instance a randomly predicted class or value, as
     * baseline
     */
    String BASELINE_RANDOM_ID_OUTCOME_KEY = "baselineRandom2outcome.txt";
    /**
     * Name of the results file which stores detailed outcome results with id and tc unit text
     */
    String ID_DETAILED_OUTCOME_KEY = "id2detailedOutcome.csv";
    /**
     * Name of the global file which contains the classifier predictions and gold standard for all
     * test instances.
     */
    String FILE_COMBINED_ID_OUTCOME_KEY = "combinedId2Outcome.txt";
    /**
     * Name of the global file which contains a prediction with the majority class for all test
     * instances.
     */
    String FILE_COMBINED_BASELINE_MAJORITY_OUTCOME_KEY = "combinedBaselineMajorityId2Outcome.txt";
    /**
     * Name of the global file which contains a prediction with a random class for all test
     * instances.
     */
    String FILE_COMBINED_BASELINE_RANDOM_OUTCOME_KEY = "combinedBaselineRandomId2Outcome.txt";
    /**
     * Name of the meta task file which holds the tc unit text and context for all test instances.
     */
    String ID_CONTEXT_KEY = "id2context.txt";

    /**
     * This is the character for joining strings for pair ngrams.
     */
    String NGRAM_GLUE = "_";

    /*
     * Machine Learning (General)
     */
    /**
     * Name of the file which holds the names of all used features
     */
    String FILENAME_FEATURES = "featureNames.txt";

    String FILENAME_OUTCOMES = "outcomes.txt";

    /**
     * Name of the attribute/label which stores the prediction values
     */
    String PREDICTION_CLASS_LABEL_NAME = "prediction";
    /**
     * Name of the training data input key in the TestTask
     */
    String TEST_TASK_INPUT_KEY_TRAINING_DATA = "input.train";
    /**
     * Name of the test data input key in the TestTask
     */
    String TEST_TASK_INPUT_KEY_TEST_DATA = "input.test";
    /**
     * Name of the output input key in the TestTask
     */
    String TEST_TASK_OUTPUT_KEY = "output";
    /**
     * Name of the instance ID feature
     */
    String ID_FEATURE_NAME = "DKProTCInstanceID";
    /**
     * For empty predictions (no label value above the bipartition threshold)
     */
    String EMPTY_PREDICTION = "$NO_PREDICTION$";

    /**
     * Alias for the training folder output location used during wiring of experiments to inform the
     * extraction test-task about the output location of the extraction train-task
     */
    String TRAIN_OUTPUT = "TRAIN_OUTPUT";
    String OUTCOMES_INPUT_KEY = "outcomesFolder";
    String FILENAME_FEATURES_DESCRIPTION = "featureDescription.txt";
    String FILENAME_DOCUMENT_META_DATA_LOG = "documentMetaData.txt";

    String TC_TASK_TYPE = "TcTaskType";

    /**
     * Dummy value for text classification outcomes as placeholder for the real outcome
     */
    String TC_OUTCOME_DUMMY_VALUE = "dummyValue";

    /*
     * Machine Learning (Model)
     */
    /**
     * Name of the file which holds the model meta data
     */
    String MODEL_META = "meta.txt";

    String META_COLLECTOR_OVERRIDE = "metaCollectorOverrides.txt";
    String META_EXTRACTOR_OVERRIDE = "metaExtractorOverrides.txt";

    /**
     * Tc version under which this model has been trained
     */
    String MODEL_TC_VERSION = "tcVersion.txt";
    /**
     * Name of the file which holds the feature names
     */
    String MODEL_FEATURE_NAMES = "featureNames.txt";
    /**
     * Name of the file which holds the feature names (non-human-readable)
     */
    String MODEL_FEATURE_NAMES_SERIALIZED = "featureNames.ser";
    /**
     * Name of the file which holds the class labels
     */
    String MODEL_CLASS_LABELS = "classLabels.txt";

    /**
     * Name of the file which holds the feature extractors
     */
    String MODEL_FEATURE_CLASS_FOLDER = "featureClassFolder";

    /**
     * Name of the file which holds the global UIMA parameters
     */
    String MODEL_FEATURE_EXTRACTOR_CONFIGURATION = "featureExtractorConfiguration.txt";

    /**
     * Name of the file which holds the classifier
     */
    String MODEL_CLASSIFIER = "classifier.ser";

    /**
     * Name of the file which holds the feature mode
     */
    String MODEL_FEATURE_MODE = "featureMode.txt";
    /**
     * Name of the file which holds the feature mode
     */
    String MODEL_LEARNING_MODE = "learningMode.txt";
    /**
     * Name of the file which holds the bipartition threshold
     */
    String MODEL_BIPARTITION_THRESHOLD = "bipartitionThreshold.txt";
    
    String FILE_CONFUSION_MATRIX = "confusionMatrix.txt";
    
    String FILE_SCORE_PER_CATEGORY = "scorePerCategory";

    /*
     * Misc
     */
    /**
     * Used for cross validation setups to enforce that the folds are created from the CAS as
     * written by the reader i.e. no attempts will be made to create more CAS objects this offers
     * the opportunity to write as many CAS files as folds are requested and take direct influence
     * on how the cross validation will be performed
     */
    String DIM_CROSS_VALIDATION_MANUAL_FOLDS = "useCrossValidationManualFolds";

    /**
     * Allows to skip the sanity checks that ensures that each classification target corresponds to
     * an outcome. Setting this flag should speed up experiment execution considerably for larger
     * amounts of data.
     */
    String DIM_SKIP_SANITY_CHECKS = "skipSanityChecks";

}