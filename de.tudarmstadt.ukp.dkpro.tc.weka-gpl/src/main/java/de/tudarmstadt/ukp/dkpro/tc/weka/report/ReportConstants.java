package de.tudarmstadt.ukp.dkpro.tc.weka.report;

public interface ReportConstants
{
    // accuracy
    public static final String CORRECT = "Correctly Classified Examples";
    public static final String INCORRECT = "Incorrectly Classified Examples";
    public static final String PCT_CORRECT = "Percentage Correct";
    public static final String PCT_INCORRECT = "Percentage Incorrect";
    public static final String PCT_UNCLASSIFIED = "Percentage Unclassified";

    // P/R/F
    public static final String PRECISION = "Unweighted Precision";
    public static final String RECALL = "Unweighted Recall";
    public static final String FMEASURE = "FUnweighted F-Measure";
    public static final String WGT_PRECISION = "Weighted Precision";
    public static final String WGT_RECALL = "Weighted Recall";
    public static final String WGT_FMEASURE = "Weighted F-Measure";

    // regression
    public static final String CORRELATION = "Pearson Correlation";
    public static final String MEAN_ABSOLUTE_ERROR = "Mean absolute error";
    public static final String RELATIVE_ABSOLUTE_ERROR = "Relative absolute error";
    public static final String ROOT_MEAN_SQUARED_ERROR = "Root mean squared error";
    public static final String ROOT_RELATIVE_SQUARED_ERROR = "Root relative squared error";
    
    // multi-label classification
    public static final String AVERAGE_THRESHOLD = "Averaged Threshold";
    public static final String LABEL_CARDINALITY_REAL = "Label Cardinality real";
    public static final String LABEL_CARDINALITY_PRED = "Label Cardinality predicted";
    public static final String EMPTY_VECTORS = "Empty Vectors";
    public static final String HEMMING_ACCURACY = "Hemming Accuracy";
    public static final String ZERO_ONE_LOSS = "Zero One Loss";
    public static final String EXAMPLE_BASED_LOG_LOSS = "Example Based LogLoss";
    public static final String LABEL_BASED_LOG_LOSS = "Label Based LogLoss";
    public static final String TP_RATE = "True Positive Rate";
    public static final String FP_RATE = "False Positive Rate";
    
    public static final String NUMBER_EXAMPLES = "Absolute Number of Examples";
    public static final String NUMBER_LABELS = "Absolute Number of Labels";
}
