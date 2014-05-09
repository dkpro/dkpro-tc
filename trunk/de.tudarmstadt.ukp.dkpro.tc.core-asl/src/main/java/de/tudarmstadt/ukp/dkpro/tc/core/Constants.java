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
     * Weka / Meka
     */
    /**
     * Name of the file where the weka data writer stores the instances in ARFF format
     */
    public static final String ARFF_FILENAME = "training-data.arff.gz";
    /**
     * Name of the weka data writer class. This constant can be used instead of the class reference,
     * if the tc.weka module cannot be used as a dependency
     */
    public static final String WEKA_DATA_WRITER_NAME = "de.tudarmstadt.ukp.dkpro.tc.weka.WekaDataWriter";
    /**
     * Name of the meka (multi-label weka) data writer class. This constant can be used instead of
     * the class reference, if the tc.weka module cannot be used as a dependency
     */
    public static final String MEKA_DATA_WRITER_NAME = "de.tudarmstadt.ukp.dkpro.tc.weka.MekaDataWriter";

    /*
     * Mallet
     */
    /**
     * Name of the file where the Mallet data writer stores the instances
     */
    public static final String MALLET_FILE = "training-data.txt.gz";
}