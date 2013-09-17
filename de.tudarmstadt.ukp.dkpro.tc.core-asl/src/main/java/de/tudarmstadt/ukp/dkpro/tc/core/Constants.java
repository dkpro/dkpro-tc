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
    
    public static final String DIM_IS_REGRESSION = "isRegressionExperiment";
    public static final String DIM_MULTI_LABEL = "multiLabel";
    public static final String DIM_LOWER_CASE = "lowerCase";

    
    /*
     * Mainly for reports
     */
    public static final String EVAL_FILE_NAME = "evalulation_results";
    public static final String SUFFIX_EXCEL = ".xls";
    public static final String SUFFIX_CSV = ".csv";
}