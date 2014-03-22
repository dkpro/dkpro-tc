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

    public static String INITIAL_VIEW = CAS.NAME_DEFAULT_SOFA;
    public static String PART_ONE = "PART_ONE";
    public static String PART_TWO = "PART_TWO";

    /*
     * Instance storage
     */

    public static final String CLASS_ATTRIBUTE_PREFIX = "__";
    public static final String CLASS_ATTRIBUTE_NAME = "outcome";

    public static final int LEAVE_ONE_OUT = -1;

    /*
     * Discriminators
     */

    public static final String DIM_READER_TRAIN = "readerTrain";
    public static final String DIM_READER_TRAIN_PARAMS = "readerTrainParams";
    public static final String DIM_READER_TEST = "readerTest";
    public static final String DIM_READER_TEST_PARAMS = "readerTestParams";

    public static final String DIM_FEATURE_SET = "featureSet";

    public static final String DIM_PIPELINE_PARAMS = "pipelineParameters";

    public static final String DIM_CLASSIFICATION_ARGS = "classificationArguments";

    public static final String DIM_BIPARTITION_THRESHOLD = "threshold";

    // public static final String DIM_IS_REGRESSION = "isRegressionExperiment";
    // public static final String DIM_IS_PAIR_CLASSIFICATION = "isPairClassification";
    // public static final String DIM_MULTI_LABEL = "multiLabel";

    // public static final String DIM_LOWER_CASE = "lowerCase";
    // public static final String DIM_IS_UNIT_CLASSIFICATION = "isUnitClassification";

    public static final String DIM_DATA_WRITER = "dataWriter";

    /*
     * Learning modes
     */
    public static final String DIM_LEARNING_MODE = "learningMode";
    public static final String LM_SINGLE_LABEL = "singleLabel";
    public static final String LM_MULTI_LABEL = "multiLabel";
    public static final String LM_REGRESSION = "regression";
    public static final String LM_SEQUENCE = "sequence";


    /*
     * feature modes
     */
    public static final String DIM_FEATURE_MODE = "featureMode";
    public static final String FM_DOCUMENT = "document";
    public static final String FM_UNIT = "unit";
    public static final String FM_PAIR = "pair";

    /*
     * Mainly for reports
     */
    public static final String EVAL_FILE_NAME = "evaluation_results";
    public static final String SUFFIX_EXCEL = ".xls";
    public static final String SUFFIX_CSV = ".csv";
    public static final String CONFUSIONMATRIX_KEY = "confusionMatrix.csv";
    public static final String PR_CURVE_KEY = "PR_curve.svg";
    public static final String CM_ACTUAL = "(act.)";
    public static final String CM_PREDICTED = "(pred.)";

    /*
     * Weka
     */
    public static final String ARFF_FILENAME = "training-data.arff.gz";
    public static final String MEKA_DATA_WRITER_NAME = "de.tudarmstadt.ukp.dkpro.tc.weka.writer.MekaDataWriter";
    public static final String WEKA_DATA_WRITER_NAME = "de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter";

}